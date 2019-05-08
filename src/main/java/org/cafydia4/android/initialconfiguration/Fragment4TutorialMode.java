package org.cafydia4.android.initialconfiguration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.cafydia4.android.R;
import org.cafydia4.android.tutorial.Tutorial;

/**
 * Created by user on 2/05/15.
 */
public class Fragment4TutorialMode extends FragmentOneStep {
    private TextView tvMessage2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.initial_config_fragment_4_tutorial_mode, container, false);

        tvMessage2 = (TextView) layout.findViewById(R.id.tvMessage2);

        CheckBox cbActivated = (CheckBox) layout.findViewById(R.id.cbAboveActivated);

        cbActivated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Tutorial.tutorialOn(getActivity());
                    tvMessage2.setVisibility(View.INVISIBLE);
                } else {
                    Tutorial.tutorialOff(getActivity());
                    tvMessage2.setVisibility(View.VISIBLE);
                }
            }
        });

        cbActivated.setChecked(true);

        return layout;

    }


    public static Fragment4TutorialMode newInstance(){
        return new Fragment4TutorialMode();
    }

    public boolean canAdvance(){
        return true;
    }
}
