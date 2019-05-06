package org.cafydia.android.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.cafydia.android.R;

/**
 * Created by user on 20/08/14.
 */
public class MyToast {
    private int mDuration = Toast.LENGTH_LONG;
    private Context mContext;
    private Toast mToast;

    public MyToast(Context context, String message, int duration){
        Toast toast = Toast.makeText(context, message, duration);
        mContext = context;
        mToast = toast;
        showToast();
    }

    public MyToast(Context context, String message){
        Toast toast = Toast.makeText(context, message, mDuration);
        mContext = context;
        mToast = toast;
        showToast();
    }

    public MyToast(Context context, int stringResource){
        Toast toast = Toast.makeText(context, context.getText(stringResource), mDuration);
        mContext = context;
        mToast = toast;
        showToast();
    }

    private void showToast(){
        View view = mToast.getView();

        // background
        view.setBackgroundColor(mContext.getResources().getColor(R.color.colorCafydiaDefault));

        TextView textView = (TextView) mToast.getView().findViewById(android.R.id.message);
        textView.setTextAppearance(mContext, R.style.MyTextAppearance_Toast);
        textView.setGravity(Gravity.CENTER);

        mToast.show();

    }
}
