package org.cafydia4.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import org.cafydia4.android.R;
import org.cafydia4.android.util.C;

/**
 * Created by user on 17/09/14.
 */
public class HbA1cImageView extends ImageView {

    private int mDrawableNoData = R.drawable.hba1c_0_no_data;
    private int mDrawableVeryGood = R.drawable.hba1c_1_very_good;
    private int mDrawableGood = R.drawable.hba1c_2_good;
    private int mDrawableRegular = R.drawable.hba1c_3_regular;
    private int mDrawableBad = R.drawable.hba1c_4_bad;
    private int mDrawableVeryBad = R.drawable.hba1c_5_very_bad;

    private AnimationSet mStartAnimation;


    public HbA1cImageView(Context c, AttributeSet a){
        super(c, a);

        mStartAnimation = new AnimationSet(c, a);

        Animation scale = new ScaleAnimation(10f, 1f, 10f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        Animation alpha = new AlphaAnimation(0f, 1f);

        scale.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());


        mStartAnimation.setDuration(1000);

        mStartAnimation.addAnimation(scale);
        mStartAnimation.addAnimation(alpha);

    }

    public void setHbA1cPercentage(float h){
        if(h == 0.0f) {
            // no data
            setImageDrawable(getResources().getDrawable(mDrawableNoData));
        } else {
            if(h <= C.HBA1C_TOP_VERY_GOOD) {
                setImageDrawable(getResources().getDrawable(mDrawableVeryGood));
            }
            else if(h <= C.HBA1C_TOP_GOOD) {
                setImageDrawable(getResources().getDrawable(mDrawableGood));
            }
            else if(h <= C.HBA1C_TOP_REGULAR) {
                setImageDrawable(getResources().getDrawable(mDrawableRegular));

            }
            else if(h <= C.HBA1C_TOP_BAD) {
                setImageDrawable(getResources().getDrawable(mDrawableBad));

            }
            else {
                setImageDrawable(getResources().getDrawable(mDrawableVeryBad));

            }

        }


    }

    public void refreshAnimatedly(){
        startAnimation(mStartAnimation);
    }

    public void little(){
        mDrawableNoData = R.drawable.hba1c_0_no_data_little;
        mDrawableVeryGood = R.drawable.hba1c_1_very_good_little;
        mDrawableGood = R.drawable.hba1c_2_good_little;
        mDrawableRegular = R.drawable.hba1c_3_regular_little;
        mDrawableBad = R.drawable.hba1c_4_bad_little;
        mDrawableVeryBad = R.drawable.hba1c_5_very_bad_little;

    }


}
