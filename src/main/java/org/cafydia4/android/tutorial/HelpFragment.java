package org.cafydia4.android.tutorial;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import org.cafydia4.android.R;

/**
 * Created by user on 4/05/15.
 */
public class HelpFragment extends Fragment {

    public static final String HELP_FRAGMENTS_TAG = "help_fragment";

    private static int mLayoutResource;
    private static String mFragmentTag;

    private static int mContainer;
    private static Activity mHostActivity;

    private OnHelpFragmentListener mCallback = null;

    private Handler mHandler = new Handler();
    private int mClicksCounter = 0;

    private boolean mAnimateOnStart = false;

    public void setAnimateOnStart(){
        mAnimateOnStart = true;
    }

    private Runnable resetCounter = new Runnable() {
        @Override
        public void run() {
            mClicksCounter = 0;
        }
    };


    /**
     *
     * @param a The host activity
     * @param container The id of the element that will act as container for the fragment
     * @param fragmentTag The tag that identify this fragment to memorize if it will be opened in the future
     * @param layoutResource The layout resource id of the fragment
     */
    public void show(Activity a, int container, String fragmentTag, int layoutResource) {
        show(a, container, fragmentTag, layoutResource, false);
    }

    /**
     *
     * @param a The host activity
     * @param container The id of the element that will act as container for the fragment
     * @param fragmentTag The tag that identify this fragment to memorize if it will be opened in the future
     * @param layoutResource The layout resource id of the fragment
     * @param showAlways If set to true the fragment always will be shown
     */
    public void show(final Activity a, final int container, String fragmentTag, int layoutResource, boolean showAlways) {

        mLayoutResource = layoutResource;
        mFragmentTag = fragmentTag;
        mContainer = container;
        mHostActivity = a;

        SharedPreferences sp = mHostActivity.getSharedPreferences(HELP_FRAGMENTS_TAG, Context.MODE_PRIVATE);
        boolean showSaved = sp.getBoolean(fragmentTag, true);

        if(showAlways || showSaved) {
            FragmentTransaction transaction = mHostActivity.getFragmentManager().beginTransaction();

            transaction.setCustomAnimations(R.animator.help_fragment_enter, R.animator.help_fragment_exit);
            transaction.replace(mContainer, this, "help_fragment");
            transaction.commit();

        } else {
            if(mCallback != null) {
                mCallback.onHelpFragmentClosed(fragmentTag);
            }

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View layout = inflater.inflate(mLayoutResource, container, false);

        if(layout instanceof ScrollView){
            if(((ScrollView) layout).getChildCount() > 0) {
                View child = ((ScrollView) layout).getChildAt(0);

                child.setClickable(true);
                child.setBackgroundColor(0x0);
                child.setVisibility(View.VISIBLE);

                layout.setBackgroundColor(0x0);
                layout.setVisibility(View.VISIBLE);

                /*
                if(mAnimateOnStart){
                    ViewUtil.makeViewVisibleAnimatedly(child, 1500);
                } else {
                    child.setVisibility(View.VISIBLE);
                }
                */

                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHandler.removeCallbacks(resetCounter);
                        mClicksCounter ++;
                        if(mClicksCounter > 1) {

                            String fragmentTag = mFragmentTag;

                            SharedPreferences sp = getActivity().getSharedPreferences(HELP_FRAGMENTS_TAG, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();

                            editor.putBoolean(fragmentTag, false);
                            editor.apply();

                            if(mCallback != null) {
                                mCallback.onHelpFragmentClosed(fragmentTag);
                            }

                        }
                        mHandler.postDelayed(resetCounter, 500);
                    }
                });
            }
        } else {

            layout.setClickable(true);
            layout.setBackgroundColor(0x0);
            layout.setVisibility(View.VISIBLE);

/*
            if(mAnimateOnStart){
                ViewUtil.makeViewVisibleAnimatedly(layout, 1500);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
*/
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.removeCallbacks(resetCounter);
                    mClicksCounter ++;
                    if(mClicksCounter > 1) {
                        String fragmentTag = mFragmentTag;

                        SharedPreferences sp = getActivity().getSharedPreferences(HELP_FRAGMENTS_TAG, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();

                        editor.putBoolean(fragmentTag, false);
                        editor.apply();

                        if(mCallback != null) {
                            mCallback.onHelpFragmentClosed(fragmentTag);
                        }


                    }
                    mHandler.postDelayed(resetCounter, 500);

                }
            });

        }

        return layout;
    }

    public void setOnHelpFragmentListener(OnHelpFragmentListener callback) {
        mCallback = callback;
    }

    public interface OnHelpFragmentListener {
        void onHelpFragmentClosed(String fragmentTag);
    }
}
