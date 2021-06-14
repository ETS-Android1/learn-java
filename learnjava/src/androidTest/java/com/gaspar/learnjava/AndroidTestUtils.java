package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

/**
 * Utility methods for instrumentation testing.
 */
public abstract class AndroidTestUtils {

    /**
     * Utility method to quickly update a shared preference value using an {@link ActivityScenario}. Can be
     * used to remove the preference, by passing null.
     * @param scenario The scenario.
     * @param key The key of the preference.
     * @param value The value of the preference. Pass in null to remove this preference.
     * @param <T> Type of the value.
     */
    public static <T> void modifySharedPreferenceValue(ActivityScenario<? extends Activity> scenario, String key, T value) {
        scenario.onActivity(activity -> {
            final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = preferences.edit();
            if(value == null) {
                editor.remove(key);
            } else if(value instanceof String) {
                editor.putString(key, (String)value);
            } else if(value instanceof Integer) {
                editor.putInt(key, (Integer)value);
            } else if(value instanceof Boolean) {
                editor.putBoolean(key, (Boolean)value);
            } else {
                throw new UnsupportedOperationException("Unsupported value type!");
            }
            editor.apply();
        });
    }

    /**
     * This enum can be used to work with dialog buttons. By default, the android dialog buttons have
     * id's like android.R.id.button1, and so. This enum converts them to more readable format, such as
     * positive and negative buttons.
     */
    enum DialogButtonId {

        POSITIVE(android.R.id.button1),
        NEGATIVE(android.R.id.button2),
        NEUTRAL(android.R.id.button3);

        @IdRes //actual id used by android
        private final int id;

        DialogButtonId(@IdRes int id) {
            this.id = id;
        }

        /**
         * @return The actual id used by android alert dialogs.
         */
        public int getId() {
            return id;
        }
    }

    /**
     * Custom idling resource which can be used to check if a loading finished.
     */
    public static class LoadingIdlingResource implements IdlingResource {

        private ResourceCallback resourceCallback;

        //stores if the resource is idle or not
        private boolean isIdle;

        //predicate used to test if idle state is achieved
        private final Predicate<ActivityScenarioRule<? extends Activity>> idlePredicate;

        //rule that comes from the test class
        private final ActivityScenarioRule<?> rule;

        /**
         * Create loading idle resource.
         * @param idlePredicate A predicate which determines when the resource is idle (if it returns true). Works on the 'rule' parameter.
         * @param rule Activity scenario rule, which comes from the UI test class.
         */
        public LoadingIdlingResource(@NonNull final Predicate<ActivityScenarioRule<? extends Activity>> idlePredicate, @NonNull final ActivityScenarioRule<?> rule) {
            this.idlePredicate = idlePredicate;
            this.rule = rule;
        }

        @Override
        public String getName() {
            return LoadingIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            if (isIdle) return true;
            //only idle if the predicate returns true
            isIdle = idlePredicate.test(rule);
            //important to call this if idle
            if (isIdle) {
                resourceCallback.onTransitionToIdle();
            }
            return isIdle;
        }

        @Override
        public void registerIdleTransitionCallback(
                ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

    /**
     * Original source from Espresso library, modified to handle spanned fields.
     * Returns a matcher that matches a descendant of {@link TextView} that is
     * displaying the string.
     * @param text The string the text view is expected to hold. No exact match needed, only substring.
     */
    public static Matcher<View> withSpannableText(@NonNull final String text) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with spannable string: ");
                description.appendText(" value: ");
                description.appendText(text);
            }
            @Override
            public boolean matchesSafely(TextView textView) {
                return textView.getText().toString().contains(text);
            }
        };
    }

    /**
     * Custom matcher that matches text views with a given font size. Size is in pixels.
     * @param expectedSize The expected size, in pixels.
     * @return The matcher.
     */
    public static Matcher<View> withFontSize(final int expectedSize) {
        return new BoundedMatcher<View, View>(View.class) {

            @Override
            public boolean matchesSafely(View target) {
                if (!(target instanceof TextView)) {
                    return false;
                }
                TextView targetEditText = (TextView) target;
                System.out.println("Actual text size in pixels: " + (int)targetEditText.getTextSize());
                return targetEditText.getTextSize() == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with fontSize: ");
                description.appendValue(expectedSize);
            }
        };
    }
}

