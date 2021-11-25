package com.gaspar.learnjava.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

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

    /**
     * Fade animation.
     */
    public static final int FADE = 0;

    /**
     * Slide down animation.
     */
    public static final int SLIDE_BOTTOM = 1;

    /**
     * Slide up animation.
     */
    public static final int SLIDE_TOP = 2;

    /**
     * Uses the selected animation to show or hide a view.
     * @param show Set to true to animate in, set to false to hide a view and animate out.
     * @param view The view.
     * @param parent The parent of the view.
     * @param animation One of the animation constants, {@link #FADE}, {@link #SLIDE_BOTTOM},
     *                  {@link #SLIDE_TOP}.
     */
    public static void animateViewVisibility(boolean show, View view, ViewGroup parent, int animation) {
        Transition transition;
        switch (animation) {
            case FADE:
                transition = new Fade();
                break;
            case SLIDE_BOTTOM:
                transition = new Slide(Gravity.BOTTOM);
                break;
            case SLIDE_TOP:
                transition = new Slide(Gravity.TOP);
                break;
            default:
                throw new RuntimeException("Invalid animation constant!");
        }
        transition.setDuration(600);
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition(parent, transition);
        view.setVisibility(show ? View.VISIBLE: View.GONE);
    }
}
