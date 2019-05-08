package org.cafydia4.android.dialogfragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.cafydia4.android.R;

/**
 * Created by user on 17/03/15.
 */
public class CafydiaAlertDialog extends AlertDialog {
    private Context mContext;

    public CafydiaAlertDialog(Context context) {
        super(context);

        mContext = context;
    }


    public static class Builder extends AlertDialog.Builder {
        private Context mContext;

        private ViewGroup mCustomTitle;
        private ImageView mIconImageView;
        private TextView mTitleTextView;
        private View mLine;


        public Builder(Context context, Integer color, Integer icon, Object title) {
            super(context);
            mContext = context;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mCustomTitle = (ViewGroup) inflater.inflate(R.layout.cafydia_alert_dialog_custom_title, null);

            mIconImageView = (ImageView) mCustomTitle.findViewById(R.id.ivIcon);
            mTitleTextView = (TextView) mCustomTitle.findViewById(R.id.tvTitle);
            mLine = mCustomTitle.findViewById(R.id.line);

            if(color != null) {
                setHeaderColor(color);
            }

            if(icon != null) {
                mIconImageView.setVisibility(View.VISIBLE);
                setIcon(icon);
            }

            if(title != null) {
                if(title instanceof Integer) {
                    setTitle((Integer)title);
                }
                else if(title instanceof String) {
                    setTitle((String) title);
                }
            }



            setCustomTitle(mCustomTitle);

        }

        @Override
        public Builder setTitle(int titleId) {
            mTitleTextView.setText(mContext.getString(titleId));
            return this;
        }

        @Override
        public Builder setTitle(CharSequence title) {
            mTitleTextView.setText(title);
            return this;
        }

        @Override
        public Builder setIcon(int iconDrawableId) {
            mIconImageView.setImageDrawable(mContext.getResources().getDrawable(iconDrawableId));
            return this;
        }

        public Builder setHeaderColor(int color) {
            mCustomTitle.setBackgroundColor(0x0D000000);

            mTitleTextView.setTextColor(color);
            mLine.setBackgroundColor(color);
            return this;
        }

        @Override
        public android.app.AlertDialog create() {
            final android.app.AlertDialog alertDialog = super.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    int titleDividerId = getContext().getResources()
                            .getIdentifier("titleDivider", "id", "android");

                    View titleDivider = alertDialog.findViewById(titleDividerId);
                    if (titleDivider != null) {
                        titleDivider.setBackgroundColor(0x00000000);
                    }
                }
            });

            return alertDialog;
        }


    }

}
