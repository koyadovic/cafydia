package org.cafydia4.android.tutorial;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import org.cafydia4.android.R;
import org.cafydia4.android.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by user on 5/05/15.
 */
public class HelpFragmentBundle {
    private Activity mHostActivity;

    private int mContainer;
    private View mContainerView;
    private ArrayList<String> mFragmentTags;
    private ArrayList<Integer> mFragmentLayouts;

    private int mCurrentPosition;

    private HelpFragmentBundleListener mCallback = null;

    public HelpFragmentBundle(Activity host, int containerId){
        mHostActivity = host;

        mContainer = containerId;
        mFragmentTags = new ArrayList<>();
        mFragmentLayouts = new ArrayList<>();

        mContainerView = mHostActivity.findViewById(mContainer);
        mContainerView.setVisibility(View.GONE);
        mContainerView.setBackgroundColor(mHostActivity.getResources().getColor(R.color.colorHelpFragmentBackground));

        mCurrentPosition = 0;
    }

    public boolean isEmpty(){
        return mFragmentTags.size() == 0;
    }

    public void addHelpFragment(String fragmentTag, int layoutId){
        mFragmentTags.add(fragmentTag);
        mFragmentLayouts.add(layoutId);
    }

    public boolean userKnowsAboutTag(String tag){
        SharedPreferences sp = mHostActivity.getSharedPreferences(HelpFragment.HELP_FRAGMENTS_TAG, Context.MODE_PRIVATE);
        return !sp.getBoolean(tag, true);
    }

    public void start(HelpFragmentBundleListener callback){
        mCallback = callback;

        if(isEmpty()) {
            finish();

        } else {
            HelpFragment f = (HelpFragment) mHostActivity.getFragmentManager().findFragmentByTag("help_fragment");
            if(f != null) {
                FragmentTransaction tr = mHostActivity.getFragmentManager().beginTransaction();
                tr.remove(f);
                tr.commit();
            }

            ViewUtil.makeViewVisibleAnimatedly(mContainerView, 1000);
            startHelpFragment(0);

        }

    }

    public void start(){
        start(null);
    }

    private void finish(){
        mCurrentPosition = 0;
        mFragmentTags.clear();
        mFragmentLayouts.clear();

        ViewUtil.makeViewInvisibleAnimatedly(mContainerView);

        if(mCallback != null) {
            mCallback.onHelpFragmentBundleFinished();
        }

        mCallback = null;
    }

    private HelpFragment.OnHelpFragmentListener helpFragmentListener = new HelpFragment.OnHelpFragmentListener() {
        @Override
        public void onHelpFragmentClosed(String fragmentTag) {
            mCurrentPosition++;

            if(mFragmentTags.size() - 1 >= mCurrentPosition){
                startHelpFragment(mCurrentPosition);
            } else {
                finish();
            }
        }
    };

    private void startHelpFragment(int pos){
        HelpFragment helpFragment = new HelpFragment();
        helpFragment.setOnHelpFragmentListener(helpFragmentListener);

        if(pos == 0)
            helpFragment.setAnimateOnStart();

        helpFragment.show(mHostActivity, mContainer, mFragmentTags.get(pos), mFragmentLayouts.get(pos));
    }


    public interface HelpFragmentBundleListener {
        void onHelpFragmentBundleFinished();
    }

}
