package org.cafydia4.android.initialconfiguration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cafydia4.android.R;

/**
 * Created by user on 2/05/15.
 */
public class FragmentFinish extends FragmentOneStep {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.initial_config_fragment_finish, container, false);

        return layout;
    }


    public static FragmentFinish newInstance(){
        return new FragmentFinish();
    }

    public boolean canAdvance(){
        return true;
    }
}
