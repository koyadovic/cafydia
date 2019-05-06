package org.cafydia.android.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.cafydia.android.R;
import org.cafydia.android.adapters.FoodSelectedAdapter;
import org.cafydia.android.core.Food;
import org.cafydia.android.core.FoodBundle;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyFoodArrayList;
import org.cafydia.android.util.MyRound;
import org.cafydia.android.util.OnSwipeTouchListener;
import org.cafydia.android.util.UnitChanger;

import java.util.ArrayList;


/**
 * Created by user on 28/08/14.
 */
public class FoodSelectedFragment extends Fragment {

    // related to configuration and state
    boolean mIsPhone;
    boolean mPanelVisible;
    private final int TIME_ANIMATION = C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS;

    private ImageView mIvToggleShow;
    private ListView mLvSelectedFood;
    private FrameLayout mFlNoSelection;
    private TextView tvCarbohydratesTotal;
    private UnitChanger mChanger;

    private FoodSelectedAdapter mAdapter;

    private int mWidthFragment = -1;
    private int mWidthIvToggleShow = -1;

    private OnFoodSelectedFragmentListener mCallBack;

    private FoodBundle cacheFoodBundle;

    private String mActionBarOldTitle;

    public static FoodSelectedFragment newInstance(){
        return new FoodSelectedFragment();
    }
    public static FoodSelectedFragment newInstance(String food){
        Bundle args = new Bundle();
        args.putString("food_bundle", food);

        FoodSelectedFragment fragment = new FoodSelectedFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallBack = (OnFoodSelectedFragmentListener) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_selected_food, container, false);

        // because the orientation is fixed, by default in portrait == phone.
        mIsPhone = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // because if it is a phone, the space is limited so by default the panel is hidden.
        mPanelVisible = !mIsPhone;

        mIvToggleShow = (ImageView) layout.findViewById(R.id.ivToggleShow);
        if(mIvToggleShow != null) {
            mIvToggleShow.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

                @Override
                public void onSwipeRight() {
                    mCallBack.onRequestedHideFoodSelectedPanel();
                }

                @Override
                public void onSwipeLeft() {
                    mCallBack.onRequestedShowFoodSelectedPanel();
                }
            });
        }

        mAdapter = new FoodSelectedAdapter(this);
        if(getArguments() != null) {
            String foodBundleJson = getArguments().getString("food_bundle", "");

            if (!foodBundleJson.equals("")) {
                FoodBundle bundle = new Gson().fromJson(foodBundleJson, C.TYPE_TOKEN_TYPE_FOOD_BUNDLE);
                mAdapter.setFoodSelected(bundle.getFoods());
            }
        }

        mFlNoSelection = (FrameLayout) layout.findViewById(R.id.flNoSelection);
        mLvSelectedFood = (ListView) layout.findViewById(R.id.lvSelectedFood);
        mLvSelectedFood.setAdapter(mAdapter);

        if(cacheFoodBundle != null){
            ((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).setFoodSelected(cacheFoodBundle.getFoods());
        }

        mChanger = new UnitChanger(getActivity());
        tvCarbohydratesTotal = (TextView) layout.findViewById(R.id.tvCarbohydratesTotal);

        restoreInstanceState(savedInstanceState);

        updateMessagingFrameLayout();

        registerForContextMenu(mLvSelectedFood);

        return layout;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.fragment_selected_food_context_menu, menu);

    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            Food contextFood = mAdapter.getItem(info.position);
            switch(item.getItemId()){
                case R.id.edit_selection:
                    actionSelected(C.FOOD_SELECTED_ACTION_TYPE_EDIT_SELECTION, contextFood);
                    return true;
                case R.id.remove_selection:
                    actionSelected(C.FOOD_SELECTED_ACTION_TYPE_REMOVE_SELECTION, contextFood);
                    return true;
            }

        }

        return super.onContextItemSelected(item);
    }

    /*
     * Dialog to agree with contextual action selected
     */
    private void actionSelected(final int type, final Food food){
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                null,
                getResources().getStringArray(R.array.selected_food_dialog_agree_titles)[type]
        );

        builder.setMessage(getResources().getStringArray(R.array.selected_food_dialog_agree_messages)[type]);

        builder.setPositiveButton(getResources().getString(R.string.food_selected_dialog_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch(type){
                    case C.FOOD_SELECTED_ACTION_TYPE_EDIT_SELECTION:
                        mAdapter.removeFood(food);

                        updateMessagingFrameLayout();
                        updateTotalCarbohydratesTextView();

                        mCallBack.onSelectedFoodEdited(food);
                        break;
                    case C.FOOD_SELECTED_ACTION_TYPE_REMOVE_SELECTION:
                        mAdapter.removeFood(food);

                        updateMessagingFrameLayout();
                        updateTotalCarbohydratesTextView();

                        mCallBack.onSelectedFoodRemoved(food);
                        break;
                }

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.food_selected_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }


    @Override
    public void onResume(){
        super.onResume();

        // this code is to measure the width for show and hide operations
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mWidthFragment = metrics.widthPixels;
        mWidthIvToggleShow = (int) (metrics.density * 48);

        // if is a phone, the panel start hidden
        if(mIsPhone && !mPanelVisible) {

            final RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                params.leftMargin = mWidthFragment - mWidthIvToggleShow;
                params.rightMargin = -mWidthFragment + mWidthIvToggleShow;

                getView().setLayoutParams(params);
                getView().invalidate();
            }
        }

        if(mCallBack != null) {
            mCallBack.onFoodSelectedFragmentResumed();
        }
    }

    // to save the state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Gson gson = new GsonBuilder().serializeNulls().create();

        savedInstanceState.putString("food_bundle", gson.toJson(
                ((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).getFoodSelected().getFoodArrayList()
        ));
    }

    private void restoreInstanceState(Bundle savedInstanceState){
        // Cafydia objects
        if(savedInstanceState != null){
            Gson gson = new GsonBuilder().serializeNulls().create();
            MyFoodArrayList foods = new MyFoodArrayList((ArrayList<Food>)gson.fromJson(savedInstanceState.getString("food_bundle"), C.TYPE_TOKEN_TYPE_ARRAY_LIST_FOOD));
            ((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).setFoodSelected(foods);
            updateMessagingFrameLayout();
        }
    }



    public void addFoodSelected(Food food){
        ((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).addFood(food);
        updateMessagingFrameLayout();

        updateTotalCarbohydratesTextView();

    }

    private void updateTotalCarbohydratesTextView(){
        FoodBundle bundle = new FoodBundle(((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).getFoodSelected());
        tvCarbohydratesTotal.setText("Total: " + MyRound.round(mChanger.toUIFromInternalWeight(bundle.getTotalCarbohydratesInGrams()), mChanger.getDecimalsForWeight()) + mChanger.getStringUnitForWeightShort());
    }

    protected void updateMessagingFrameLayout(){
        if(mLvSelectedFood.getAdapter() != null && mLvSelectedFood.getAdapter().getCount() == 0){
            mFlNoSelection.setVisibility(View.VISIBLE);
        } else {
            mFlNoSelection.setVisibility(View.GONE);
        }
    }

    public FoodBundle getFoodBundle(){
        return new FoodBundle(((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).getFoodSelected());
    }
    public void setFoodBundle(FoodBundle foodBundle){
        if(mLvSelectedFood == null) {
            cacheFoodBundle = foodBundle;
        } else {
            ((FoodSelectedAdapter) mLvSelectedFood.getAdapter()).setFoodSelected(foodBundle.getFoods());
        }
    }


    /*
     * ANIMATIONS TO OPEN AND CLOSE FOOD PANEL
     */

    public void showPanel(){
        if(!mPanelVisible && mIvToggleShow != null) {
            mPanelVisible = true;

            ActionBar actionBar = getActivity().getActionBar();
            if(actionBar != null) {
                mActionBarOldTitle = actionBar.getTitle().toString();
                actionBar.setTitle(getString(R.string.food_selected_actionbar_title));

            }
            Animation showAnimation = new Animation() {

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {
                        params.leftMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * (1.0 - interpolatedTime));
                        params.rightMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * (1.0 - interpolatedTime));

                        getView().setLayoutParams(params);
                        getView().bringToFront();
                    }
                }
            };

            RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                showAnimation.setDuration(TIME_ANIMATION);

                getView().startAnimation(showAnimation);
            }
        }
    }

    public void hidePanel(){
        if(mPanelVisible && mIvToggleShow != null) {
            mPanelVisible = false;

            ActionBar actionBar = getActivity().getActionBar();
            if(actionBar != null && mActionBarOldTitle != null && !mActionBarOldTitle.equals("")) {
                actionBar.setTitle(mActionBarOldTitle);
            }

            Animation hideAnimation = new Animation() {
                int ivWidth = mIvToggleShow.getWidth();

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);

                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {


                        params.leftMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * interpolatedTime);
                        params.rightMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * interpolatedTime);

                        getView().setLayoutParams(params);
                    }
                }
            };

            RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {
                hideAnimation.setDuration(TIME_ANIMATION);
                getView().startAnimation(hideAnimation);
            }
        }
    }

    public interface OnFoodSelectedFragmentListener {
        void onRequestedHideFoodSelectedPanel();
        void onRequestedShowFoodSelectedPanel();
        void onSelectedFoodEdited(Food food);
        void onSelectedFoodRemoved(Food food);
        void onFoodSelectedFragmentResumed();
    }
}
