package org.cafydia4.android.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.adapters.FoodFragmentPagerAdapter;
import org.cafydia4.android.bedcasearch.BedcaSearch;
import org.cafydia4.android.core.Food;
import org.cafydia4.android.dialogfragments.DialogFoodEditor;
import org.cafydia4.android.interfaces.OnFoodEdited;
import org.cafydia4.android.interfaces.OnFoodModifiedInterface;
import org.cafydia4.android.util.C;
import org.cafydia4.android.util.CheckInternetConnection;
import org.cafydia4.android.util.MyFoodArrayList;
import org.cafydia4.android.util.OnSwipeTouchListener;

/**
 * Created by user on 20/08/14.
 */
public class FoodFragment extends Fragment implements OnFoodModifiedInterface,
        OnFoodEdited {

    // related to configuration and state
    boolean mIsPhone;
    boolean mPanelVisible;
    private final int TIME_ANIMATION = C.PANEL_SPEED_OF_HIDE_AND_SHOW_OPERATIONS;

    //private static final String preferencePos = "shared_preferences_food_fragment_position";
    public static FoodFragment newInstance(){
        return new FoodFragment();
    }

    private ViewPager mPager;
    private EditText mSearch;

    private Handler mHandler;

    private FoodFragmentPagerAdapter mAdapter;
    public FoodFragmentInterface mCallBack;
    private int mCurrentPos;

    private ImageView ivToggleShow;
    private int mWidthFragment = -1;
    private int mWidthIvToggleShow = -1;


    private String mActionBarOldTitle = "";

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallBack = (FoodFragmentInterface) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View layout = inflater.inflate(R.layout.fragment_food, container, false);

        mHandler = new Handler();

        // because the orientation is fixed, by default in portrait == phone.
        mIsPhone = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // because if it is a phone, the space is limited so by default the panel is hidden.
        mPanelVisible = !mIsPhone;

        mPager = (ViewPager) layout.findViewById(R.id.pager);
        mSearch = (EditText) layout.findViewById(R.id.editTextSearch);
        ivToggleShow = (ImageView) layout.findViewById(R.id.ivToggleShow);

        ImageView ivAddFood = (ImageView) layout.findViewById(R.id.ivAddFood);

        ivAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getChildFragmentManager();
                DialogFoodEditor.newInstance(null, C.FOOD_FRAGMENT_POSITION_NONE)
                        .show(fm, "food_editor_dialog");
            }
        });

        if(ivToggleShow != null) {
            ivToggleShow.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

                @Override
                public void onSwipeRight() {
                    mCallBack.onRequestedShowFoodPanel();
                }

                @Override
                public void onSwipeLeft() {
                    mCallBack.onRequestedHideFoodPanel();
                }
            });
        }

        mCurrentPos = C.FOOD_FRAGMENT_POSITION_FOOD;

        mAdapter = new FoodFragmentPagerAdapter(getChildFragmentManager());

        mAdapter.addFragment(FoodFragmentSingleView.newInstance(C.FOOD_FRAGMENT_POSITION_FOOD, getString(R.string.food_fragment_single_view_title_food)));
        mAdapter.addFragment(FoodFragmentSingleView.newInstance(C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD, getString(R.string.food_fragment_single_view_title_favorite_food)));
        mAdapter.addFragment(FoodFragmentSingleView.newInstance(C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD, getString(R.string.food_fragment_single_view_title_complex_food)));
        mAdapter.addFragment(FoodFragmentSingleView.newInstance(C.FOOD_FRAGMENT_POSITION_SEARCH, getString(R.string.food_fragment_single_view_title_search)));

        mPager.setOffscreenPageLimit(4);
        mPager.setAdapter(mAdapter);

        PagerTabStrip tabStrip = (PagerTabStrip)layout.findViewById(R.id.pager_tab_strip);
        tabStrip.setTabIndicatorColorResource(R.color.colorCafydiaDefault);
        tabStrip.setDrawFullUnderline(false);

        updateUI();
        mHandler.post(setSearchTextToNothing);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(final int i) {
                mCurrentPos = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                switch (i) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mHandler.removeCallbacks(setSearchTextToNothing);
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        mHandler.post(setSearchTextToNothing);
                        updateUI();
                        break;
                }
            }

        });

        mSearch.setCursorVisible(false);

        // para búsquedas
        mSearch.setOnEditorActionListener(mActionListener);

        // para filtrados
        mSearch.addTextChangedListener(mTextWatcher);

        return layout;
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
        if(!mPanelVisible) {
            final RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
            if (params != null) {

                params.leftMargin = -mWidthFragment + mWidthIvToggleShow;
                params.rightMargin = mWidthFragment - mWidthIvToggleShow;

                getView().setLayoutParams(params);
                getView().invalidate();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    private Runnable setSearchTextToNothing = new Runnable() {
        @Override
        public void run() {
            if(mPager != null && mSearch != null) {
                mSearch.setText("");
            }
        }
    };


    /*
     * LISTENERS
     */

    TextView.OnEditorActionListener mActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (mCurrentPos == C.FOOD_FRAGMENT_POSITION_SEARCH) {
                    hideKeyboard(mSearch);
                    if(CheckInternetConnection.isConnected(getActivity().getBaseContext())){
                        if (!mSearch.getText().toString().equals("")) {
                            new SearchFoodAsyncTask().execute(mSearch.getText().toString());
                        }
                        return true;
                    } else {
                        mAdapter.getFragment(mCurrentPos).updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_INTERNET_CONNECTION);
                    }

                }
            }
            return false;
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mCurrentPos != C.FOOD_FRAGMENT_POSITION_SEARCH){
                if(mAdapter != null && mAdapter.getFragment(mCurrentPos).getAdapter() != null) {
                    mAdapter.getFragment(mCurrentPos).getAdapter().getFilter().filter(s);
                }
            }
        }
    };


    private void updateUI(){
        if(mSearch != null) {
            switch (mCurrentPos) {
                case C.FOOD_FRAGMENT_POSITION_SEARCH:
                    if(!mSearch.getText().toString().equals(getResources().getString(R.string.food_fragment_search_hint_search))) {
                        mSearch.setHint(R.string.food_fragment_search_hint_search);
                    }
                    break;
                case C.FOOD_FRAGMENT_POSITION_FOOD:
                case C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD:
                case C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD:
                    if(!mSearch.getText().toString().equals(getResources().getString(R.string.food_fragment_search_hint))) {
                        mSearch.setHint(R.string.food_fragment_search_hint);
                    }
                    break;

            }
        }
    }

    public void onFoodModified(int action, int currentPos, Food food){
        switch (action){

            case C.FOOD_ACTION_TYPE_EDIT_FOOD:
                mAdapter.getFragment(currentPos).addOrUpdateFood(food);
                break;

            case C.FOOD_ACTION_TYPE_DELETE:
                mAdapter.getFragment(currentPos).removeFood(food);
                break;

            case C.FOOD_ACTION_TYPE_ADD_FOOD:
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FOOD).addOrUpdateFood(food);
                break;

            case C.FOOD_ACTION_TYPE_FAVORITE_YES:
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FOOD).removeFood(food);
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD).addOrUpdateFood(food);
                break;

            case C.FOOD_ACTION_TYPE_FAVORITE_NO:
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD).removeFood(food);
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FOOD).addOrUpdateFood(food);
                break;

        }
        reapplyFilter();
    }

    private void reapplyFilter(){
        if(mAdapter != null && mAdapter.getFragment(mCurrentPos).getAdapter() != null) {
            mAdapter.getFragment(mCurrentPos).getAdapter().getFilter().filter(mSearch.getText().toString());
        }
    }

    public void onFoodEdited(int callerFragmentPosition, Food food) {

        if(callerFragmentPosition == C.FOOD_FRAGMENT_POSITION_NONE) {
            mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_FOOD).addOrUpdateFood(food);

        } else {
            mAdapter.getFragment(callerFragmentPosition).addOrUpdateFood(food);
        }
        reapplyFilter();

    }

    // busca en segundo plano alimentos en la red.
    private class SearchFoodAsyncTask extends AsyncTask<String, Integer, Boolean> {
        BedcaSearch search;
        MyFoodArrayList results;

        protected Boolean doInBackground(String... params) {
            String query = TextUtils.join(" ", params);

            search = new BedcaSearch(getActivity());
            results = search.workAndGetResults(query, true);

            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPreExecute() {
            mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_SEARCH).updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_SEARCHING);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                if(results.getFoodArrayList().size() == 0){
                    mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_SEARCH).updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_RESULTS);
                } else {
                    mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_SEARCH).updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NONE);
                }

                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_SEARCH).getAdapter().setAllFood(results.getFoodArrayList());
            } else {
                mAdapter.getFragment(C.FOOD_FRAGMENT_POSITION_SEARCH).updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_BEDCA_DOWN);
            }
        }

        protected void onCanceled() {

        }
    }



    /*
     * ANIMATIONS TO OPEN AND CLOSE FOOD PANEL
     */

    public void showPanel(){
        if(!mPanelVisible && ivToggleShow != null) {
            mPanelVisible = true;

            ActionBar actionBar = getActivity().getActionBar();
            if(actionBar != null) {
                mActionBarOldTitle = actionBar.getTitle().toString();
                actionBar.setTitle(getString(R.string.food_fragment_actionbar_title));

            }

            Animation showAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {
                        params.leftMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * (1.0 - interpolatedTime));
                        params.rightMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * (1.0 - interpolatedTime));

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


    public boolean isPanelVisible() {
        return mPanelVisible;
    }

    public void hidePanel(){
        if(mPanelVisible && ivToggleShow != null) {
            mPanelVisible = false;

            ActionBar actionBar = getActivity().getActionBar();

            if(actionBar != null && !mActionBarOldTitle.equals("")) {
                actionBar.setTitle(mActionBarOldTitle);

            }

            Animation hideAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);

                    RelativeLayout.LayoutParams params = getView() != null ? (RelativeLayout.LayoutParams) getView().getLayoutParams() : null;
                    if (params != null) {
                        params.leftMargin = (int) ((-mWidthFragment + mWidthIvToggleShow) * interpolatedTime);
                        params.rightMargin = (int) ((mWidthFragment - mWidthIvToggleShow) * interpolatedTime);

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


    private void hideKeyboard(EditText et){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    public interface FoodFragmentInterface {
        void onFoodClicked(Food food);
        void onRequestedHideFoodPanel();
        void onRequestedShowFoodPanel();
    }


}
