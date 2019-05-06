package org.cafydia.android.initialconfiguration;

import android.support.v4.app.Fragment;

/**
 * Created by user on 2/05/15.
 */
public abstract class FragmentOneStep extends Fragment {

    public abstract boolean canAdvance();


    public InitialConfigurationActivity getParentActivity(){
        return (InitialConfigurationActivity) super.getActivity();
    }

}
