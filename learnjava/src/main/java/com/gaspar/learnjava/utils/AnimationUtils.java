package com.gaspar.learnjava.utils;

import android.content.Context;
import android.view.View;
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
     * Makes the given view appear. Animations are handled by the system, so only
     * the visibility is set here.
     */
    public static void showView(final View v) {
        v.setVisibility(View.VISIBLE);
    }

    /**
     * Makes the given view disappear.
     * @param view The view that will disappear.
     * @param clickedView The click on this view triggered the event.
     */
    public static void hideView(final View view, @NonNull final View clickedView) {
        final int initialHeight = view.getLayoutParams().height;
        final int initialMesHeight = view.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialMesHeight - (int)(initialMesHeight * interpolatedTime);
                    view.requestLayout();
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
            public void onAnimationStart(Animation animation) {
                clickedView.setEnabled(false); //don't allow interaction until it slides out
                view.setEnabled(false);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                clickedView.setEnabled(true);
                view.setEnabled(true); //allow interaction
                view.setVisibility(View.GONE); //hide after animation concludes
                view.getLayoutParams().height = initialHeight; //IMPORTANT: give back initial height
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        view.startAnimation(a);
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
