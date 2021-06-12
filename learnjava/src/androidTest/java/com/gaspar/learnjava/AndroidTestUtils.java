package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.IdRes;
import androidx.test.core.app.ActivityScenario;

/**
 * Utility methods for instrumentation testing.
 */
public abstract class AndroidTestUtils {

    /**
     * Utility method to quickly update a shared preference value using an {@link ActivityScenario}.
     * @param scenario The scenario.
     * @param key The key of the preference.
     * @param value The value of the preference.
     * @param <T> Type of the value.
     */
    public static <T> void modifySharedPreferenceValue(ActivityScenario<? extends Activity> scenario, String key, T value) {
        scenario.onActivity(activity -> {
            final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = preferences.edit();
            if(value instanceof String) {
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
}
