package org.cafydia4.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cafydia4.android.R;
import org.cafydia4.android.util.C;

/**
 * Created by user on 28/03/15.
 */
public class MetabolicBeginningsList extends Fragment {
    private BeginningsListListener mCallback;

    public static MetabolicBeginningsList newInstance(int metabolicId){
        MetabolicBeginningsList fragment = new MetabolicBeginningsList();
        Bundle args = new Bundle();
        args.putInt("metabolic_id", metabolicId);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (BeginningsListListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_metabolic_beginnings_list, container, false);

        LinearLayout bPreprandial = (LinearLayout) layout.findViewById(R.id.bPreprandial);
        LinearLayout bBasal = (LinearLayout) layout.findViewById(R.id.bBasal);

        bPreprandial.setOnClickListener(buttonListener);
        bBasal.setOnClickListener(buttonListener);

        return layout;
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bPreprandial:
                    if(mCallback != null) {
                        mCallback.onBeginningListElementClicked(C.METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_PREPRANDIAL);
                    }
                    break;

                case R.id.bBasal:
                    if(mCallback != null) {
                        mCallback.onBeginningListElementClicked(C.METABOLIC_BEGINNINGS_LIST_ELEMENT_CLICKED_BASAL);
                    }
                    break;
            }
        }
    };

    public interface BeginningsListListener {
        void onBeginningListElementClicked(int elementClicked);
    }
}
