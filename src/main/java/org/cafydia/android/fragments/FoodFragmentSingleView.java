package org.cafydia.android.fragments;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.cafydia.android.R;
import org.cafydia.android.adapters.FoodAdapter;
import org.cafydia.android.configdatabase.ConfigurationDatabase;
import org.cafydia.android.core.Food;
import org.cafydia.android.dialogfragments.CafydiaAlertDialog;
import org.cafydia.android.dialogfragments.DialogFoodEditor;
import org.cafydia.android.interfaces.OnFoodEdited;
import org.cafydia.android.interfaces.OnFoodModifiedInterface;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyFoodArrayList;

/**
 * Created by user on 21/08/14.
 */
public class FoodFragmentSingleView extends Fragment implements OnFoodEdited {
    private static final String POS = "position";
    private ListView mLvFood;

    private FrameLayout mFlNoFoodAdded;
    private FrameLayout mFlNoResults;
    private FrameLayout mFlSearching;
    private FrameLayout mFlNoSearchMade;
    private FrameLayout mFlNoInternet;
    private FrameLayout mFlNoBedca;

    private static String[] mTitles = new String[5];

    private FoodAdapter mAdapter;

    private OnFoodModifiedInterface mCallBack;

    private static int pest = -1;
    private Integer mCurrentPos;

    public static FoodFragmentSingleView newInstance(int currentPos, String title){

        FoodFragmentSingleView f = new FoodFragmentSingleView();
        Bundle args = new Bundle();
        args.putInt(POS, currentPos);

        mTitles[currentPos] = title;

        f.setArguments(args);

        return f;
    }

    public void onAttachFragment(Fragment fragment) {
        if(fragment != null) {
            mCallBack = (OnFoodModifiedInterface) fragment;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_food_pager_single_view, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.textViewTitle);
        mLvFood = (ListView) rootView.findViewById(R.id.listViewFood);

        mFlNoFoodAdded = (FrameLayout) rootView.findViewById(R.id.flNoFoodAdded);
        mFlNoResults = (FrameLayout) rootView.findViewById(R.id.flNoResults);
        mFlSearching = (FrameLayout) rootView.findViewById(R.id.flSearching);
        mFlNoSearchMade = (FrameLayout) rootView.findViewById(R.id.flNoSearchMade);
        mFlNoInternet = (FrameLayout) rootView.findViewById(R.id.flNoInternet);
        mFlNoBedca = (FrameLayout) rootView.findViewById(R.id.flNoBedca);

        ImageView ivLeftArrow = (ImageView) rootView.findViewById(R.id.ivLeftArrow);
        ImageView ivRightArrow = (ImageView) rootView.findViewById(R.id.ivRightArrow);

        mCurrentPos = getArguments().getInt(POS);

        restoreInstanceState(savedInstanceState);

        switch(mCurrentPos){
            case C.FOOD_FRAGMENT_POSITION_SEARCH:
                title.setText(R.string.food_fragment_single_view_title_search);
                mAdapter = new FoodAdapter((FoodFragment) getParentFragment());
                mLvFood.setAdapter(mAdapter);
                updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_SEARCH_MADE);
                ivLeftArrow.setVisibility(View.INVISIBLE);
                break;

            case C.FOOD_FRAGMENT_POSITION_FOOD:
                title.setText(R.string.food_fragment_single_view_title_food);
                new LoadFood().execute(mCurrentPos);
                break;

            case C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD:
                title.setText(R.string.food_fragment_single_view_title_favorite_food);
                new LoadFood().execute(mCurrentPos);
                break;

            case C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD:
                title.setText(R.string.food_fragment_single_view_title_complex_food);
                new LoadFood().execute(mCurrentPos);
                ivRightArrow.setVisibility(View.INVISIBLE);
                break;
        }

        mLvFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((FoodFragment) getParentFragment()).mCallBack.onFoodClicked(mAdapter.getItem(position));
            }
        });

        registerForContextMenu(mLvFood);
        onAttachFragment(getParentFragment());

        FoodFragmentSingleView.pest = -1;

        return rootView;
    }

    // to save and restore the state
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArray("titles", mTitles);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            mTitles = savedInstanceState.getStringArray("titles");
        }
    }

    public String getTitle() {

        if(mCurrentPos == null){
            if(pest < 4) {
                mCurrentPos = ++FoodFragmentSingleView.pest;
            } else {
                mCurrentPos = FoodFragmentSingleView.pest;
            }
        }

        return mTitles[mCurrentPos];
    }

    /*
         * Contextual menu
         */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.getId() == R.id.listViewFood) {
            MenuInflater inflater = getActivity().getMenuInflater();

            switch (mCurrentPos) {
                case C.FOOD_FRAGMENT_POSITION_SEARCH:
                    inflater.inflate(R.menu.fragment_food_context_menu_search, menu);
                    break;

                case C.FOOD_FRAGMENT_POSITION_FOOD:
                    inflater.inflate(R.menu.fragment_food_context_menu_food, menu);
                    break;

                case C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD:
                    inflater.inflate(R.menu.fragment_food_context_menu_favorite_food, menu);
                    break;

                case C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD:
                    inflater.inflate(R.menu.fragment_food_context_menu_complex, menu);
                    break;

            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(getUserVisibleHint()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            Food contextFood = mAdapter.getItem(info.position);
            ConfigurationDatabase db = new ConfigurationDatabase(getActivity());


            switch (mCurrentPos) {
                case C.FOOD_FRAGMENT_POSITION_SEARCH:
                    switch (item.getItemId()) {

                        case R.id.add_food:
                            contextFood.save(db);
                            mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_ADD_FOOD, mCurrentPos, contextFood);
                            return true;
                    }
                    break;

                case C.FOOD_FRAGMENT_POSITION_FOOD:
                    switch (item.getItemId()) {

                        case R.id.mark_as_favorite:
                            contextFood.setFavorite(C.FOOD_FAVORITE_YES);
                            contextFood.save(db);
                            mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_FAVORITE_YES, mCurrentPos, contextFood);

                            return true;

                        case R.id.edit_annotation:
                            FragmentManager fm = getChildFragmentManager();
                            Gson gson = new Gson();
                            DialogFoodEditor.newInstance(gson.toJson(contextFood), mCurrentPos).show(fm, "food_editor_dialog");

                            return true;

                        case R.id.delete_annotation:
                            actionFoodDialogAgree(C.FOOD_ACTION_TYPE_DELETE, contextFood);
                            return true;
                    }
                    break;

                case C.FOOD_FRAGMENT_POSITION_FAVORITE_FOOD:
                    switch (item.getItemId()) {

                        case R.id.mark_as_no_favorite:
                            contextFood.setFavorite(C.FOOD_FAVORITE_NO);
                            contextFood.save(db);
                            mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_FAVORITE_NO, mCurrentPos, contextFood);
                            return true;

                        case R.id.edit_annotation:
                            FragmentManager fm = getChildFragmentManager();
                            Gson gson = new Gson();
                            DialogFoodEditor.newInstance(gson.toJson(contextFood), mCurrentPos).show(fm, "food_editor_dialog");
                            return true;

                        case R.id.delete_annotation:
                            actionFoodDialogAgree(C.FOOD_ACTION_TYPE_DELETE, contextFood);
                            return true;
                    }

                    break;

                case C.FOOD_FRAGMENT_POSITION_COMPLEX_FOOD:
                    switch (item.getItemId()) {

                        case R.id.edit_annotation:
                            FragmentManager fm = getChildFragmentManager();
                            Gson gson = new Gson();
                            DialogFoodEditor.newInstance(gson.toJson(contextFood), mCurrentPos).show(fm, "food_editor_dialog");
                            return true;

                        case R.id.delete_annotation:
                            actionFoodDialogAgree(C.FOOD_ACTION_TYPE_DELETE, contextFood);
                            return true;
                    }
                    break;
            }
        }

        return super.onContextItemSelected(item);

    }

    private void actionFoodDialogAgree(final int type, final Food food){
        CafydiaAlertDialog.Builder builder = new CafydiaAlertDialog.Builder(
                getActivity(),
                getResources().getColor(R.color.colorCafydiaDefault),
                R.drawable.ic_action_warning,
                getResources().getStringArray(R.array.action_food_dialog_agree_titles)[type]
        );
        builder.setMessage(getResources().getStringArray(R.array.action_food_dialog_agree_messages)[type]);

        builder.setPositiveButton(getResources().getString(R.string.action_dialog_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
                switch(type){
                    // no se usa
                    case C.FOOD_ACTION_TYPE_FAVORITE_YES:
                        food.setFavorite(C.FOOD_FAVORITE_YES);
                        food.save(db);
                        mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_FAVORITE_YES, mCurrentPos, food);
                        break;

                    // no se usa
                    case C.FOOD_ACTION_TYPE_FAVORITE_NO:
                        food.setFavorite(C.FOOD_FAVORITE_NO);
                        food.save(db);
                        mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_FAVORITE_NO, mCurrentPos, food);
                        break;

                    case C.FOOD_ACTION_TYPE_DELETE:
                        food.delete(db);
                        mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_DELETE, mCurrentPos, food);
                        break;

                    // no se usa
                    case C.FOOD_ACTION_TYPE_ADD_FOOD:
                        food.save(db);
                        mCallBack.onFoodModified(C.FOOD_ACTION_TYPE_ADD_FOOD, mCurrentPos, food);
                        break;

                }

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.action_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    protected void updateMessagingFrameLayout(int p){
        switch(p){
            case C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_FOOD_ADDED:
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlSearching.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoInternet.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.VISIBLE);
                break;
            case C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_RESULTS:
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlSearching.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);
                mFlNoInternet.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.VISIBLE);
                break;
            case C.FOOD_FRAGMENT_FRAME_LAYOUT_SEARCHING:
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);
                mFlNoInternet.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.GONE);
                mFlSearching.setVisibility(View.VISIBLE);
                break;
            case C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_SEARCH_MADE:
                mFlSearching.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);
                mFlNoInternet.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.GONE);
                mFlNoSearchMade.setVisibility(View.VISIBLE);
                break;
            case C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_INTERNET_CONNECTION:
                mFlSearching.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.GONE);
                mFlNoInternet.setVisibility(View.VISIBLE);
                break;

            case C.FOOD_FRAGMENT_FRAME_LAYOUT_BEDCA_DOWN:
                mFlSearching.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlNoBedca.setVisibility(View.VISIBLE);
                mFlNoInternet.setVisibility(View.GONE);
                break;

            default:
                mFlNoSearchMade.setVisibility(View.GONE);
                mFlSearching.setVisibility(View.GONE);
                mFlNoResults.setVisibility(View.GONE);
                mFlNoFoodAdded.setVisibility(View.GONE);

        }


    }

    public void addOrUpdateFood(Food food){
        ((FoodAdapter) mLvFood.getAdapter()).addOrUpdateFood(food);
        refreshMessage();
    }
    public void removeFood(Food food){
        ((FoodAdapter) mLvFood.getAdapter()).removeFood(food);
        refreshMessage();
    }

    private void refreshMessage(){
        if(mLvFood.getAdapter().getCount() > 0){
            updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NONE);
        } else {
            if(mCurrentPos.equals(C.FOOD_FRAGMENT_POSITION_SEARCH)) {
                updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_SEARCH_MADE);
            } else {
                updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_FOOD_ADDED);
            }

        }

    }

    private class LoadFood extends AsyncTask<Integer, Integer, Boolean> {
        private MyFoodArrayList privateFoods;

        protected Boolean doInBackground(Integer... params){
            int fragmentPosition = params[0];
            ConfigurationDatabase db = new ConfigurationDatabase(getActivity());
            privateFoods = db.getFoodByFoodFragmentPosition(fragmentPosition);

            if(getActivity() != null)
                return true;
            else
                return false;
        }

        protected void onPostExecute(Boolean result){
            if (result){
                mAdapter = new FoodAdapter((FoodFragment) getParentFragment(), privateFoods.getFoodArrayList());
                mLvFood.setAdapter(mAdapter);
                if(mAdapter.getCount() == 0){
                    updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NO_FOOD_ADDED);
                } else {
                    updateMessagingFrameLayout(C.FOOD_FRAGMENT_FRAME_LAYOUT_NONE);
                }
            }
        }
    }

    public void onFoodEdited(int callerFragmentPosition, Food food) {
        //((FoodAdapter) mLvFood.getAdapter()).addOrUpdateFood(food);

        ((FoodFragment) getParentFragment()).onFoodEdited(callerFragmentPosition, food);
    }

    public FoodAdapter getAdapter() {
        return mAdapter;
    }

}
