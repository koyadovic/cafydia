package org.cafydia4.android.initialconfiguration;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.cafydia4.android.R;

/**
 * Created by user on 8/05/15.
 */
public class Fragment0Eula extends FragmentOneStep {
    private boolean mCanGoForward = false;

    public static Fragment0Eula newInstance(){
        return new Fragment0Eula();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.initial_config_fragment_0_eula, container, false);

        CheckBox cbAccept = (CheckBox) layout.findViewById(R.id.cbAccept);
        cbAccept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCanGoForward = isChecked;
                getParentActivity().updateButtons();
            }
        });

        TextView tvContent = (TextView) layout.findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());
        tvContent.setText(Html.fromHtml(getString(R.string.eula)));

        return layout;

    }

    public boolean canAdvance(){
        return mCanGoForward;
    }

}
