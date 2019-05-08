package org.cafydia4.android.util;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by user on 2/07/14.
 */
public class ViewUtil {
    private static final int ANIMATION_MILISECONDS = 300;

    // without animation
    public static void makeViewVisible(final View view){
        view.setVisibility(View.VISIBLE);
    }

    public static void makeViewInvisible(final View view){
        view.setVisibility(View.INVISIBLE);
    }

    public static void makeViewGone(final View view){
        view.setVisibility(View.GONE);
    }

    // with animation
    public static void makeViewVisibleAnimatedly(final View view) {
        makeViewVisibleAnimatedly(view, ANIMATION_MILISECONDS);
    }

    public static void makeViewInvisibleAnimatedly(final View view){
        makeViewInvisibleAnimatedly(view, ANIMATION_MILISECONDS);
    }

    public static void makeViewVisibleAnimatedly(final View view, int miliseconds) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(miliseconds)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        // todo Algunas veces los charts no se ven
                        // todo Esto se añade para ver si pudiese solucionarlo. Aunque suena rarísimo.
                        if(view.getVisibility() != View.VISIBLE) {
                            view.setVisibility(View.VISIBLE);
                        }

                        if(view.getAlpha() != 1f){
                            view.setAlpha(1f);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public static void makeViewInvisibleAnimatedly(final View view, int miliseconds){

        view.animate()
                .alpha(0f)
                .setDuration(miliseconds)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
            });

    }

    public static void hideKeyboard(Context c, EditText et){

        InputMethodManager imm = (InputMethodManager)c.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
    public static void showKeyboard(Context c, final EditText et){

        final InputMethodManager imm = (InputMethodManager)c.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        et.requestFocus();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

    }


}
