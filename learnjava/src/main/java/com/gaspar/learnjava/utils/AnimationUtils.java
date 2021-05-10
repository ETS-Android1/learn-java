package com.gaspar.learnjava.utils;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.gaspar.learnjava.R;

/**
 * Contains methods that animate views.
 */
@UiThread
public abstract class AnimationUtils {

    /**
     * Direction constants.
     */
    @IntDef({Direction.LEFT, Direction.RIGHT})
    public @interface Direction {
        int LEFT = 1;
        int RIGHT = 2;
    }

    /**
     * Animation duration time, in milliseconds.
     */
    public static final int DURATION = 800;

    /**
     * Slides in a previously 'GONE' view. Up/Down animation.
     *
     * @author neoteknic, StackOverflow.com
     */
    public static void slideIn(final View v) {
        v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? WindowManager.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(DURATION);
        //a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) { }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        v.startAnimation(a);
    }

    /**
     * Slides out a previously visible view. Up/Down animation.
     *
     * @author neoteknic, StackOverflow.com
     */
    public static void slideOut(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(DURATION);
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        v.startAnimation(a);
    }

    /**
     * Slides a view in to the given direction.
     */
    public static void slideInHorizontal(@NonNull final View v, @Direction int direction, final Context context) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context ,direction == Direction.LEFT ?
                R.anim.slide_in_left : R.anim.slide_in_right);
        v.setVisibility(View.VISIBLE);
        v.startAnimation(animation);
    }

    /**
     * Slides a view out to the given direction.
     */
    public static void slideOutHorizontal(@NonNull final View v, @Direction int direction, final Context context) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context ,direction == Direction.LEFT ?
                R.anim.slide_out_left : R.anim.slide_out_right);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        v.startAnimation(animation);
    }


}
