package org.cafydia4.android.adapters;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.cafydia4.android.fragments.FoodFragmentSingleView;

import java.util.ArrayList;

/**
 * Created by user on 22/08/14.
 */
public class FoodFragmentPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<FoodFragmentSingleView> mFragments;

    public FoodFragmentPagerAdapter(FragmentManager fm){
        super(fm);
        mFragments = new ArrayList<>();
    }

    public void addFragment(FoodFragmentSingleView f){
        mFragments.add(f);
    }

    public FoodFragmentSingleView getFragment (int position){
        return mFragments.get(position);
    }

    @Override
    public FoodFragmentSingleView getItem(int pos){
        return mFragments.get(pos);
    }

    @Override
    public int getCount(){
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle (int position) {
        return getFragment(position).getTitle();
    }

}
