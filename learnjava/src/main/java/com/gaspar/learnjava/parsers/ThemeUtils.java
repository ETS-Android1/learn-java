package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;

/**
 * Gets runtime color values.
 */
public abstract class ThemeUtils {

    /**
     * The currently selected theme.
     */
    @Themes
    private static int selectedTheme;

    private static final String THEME_PREF_NAME = "selected_theme";

    /**
     * Called on start of the application, initialized the selected theme from preferences.
     */
    public static void initSelectedTheme(final @NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(prefs.contains(THEME_PREF_NAME)) {
            selectedTheme = prefs.getInt(THEME_PREF_NAME, Themes.ORANGE);
        } else { //default theme is orange
            prefs.edit().putInt(THEME_PREF_NAME, Themes.ORANGE).apply();
            selectedTheme = Themes.ORANGE;
        }
    }

    /**
     * Constants of the possible themes.
     */
    @IntDef
    public @interface Themes {
        int ORANGE = 0; //the default orange theme
        int BLUE = 1;
        int DARK = 2;
    }

    /**
     * Gets the primary color.
     *
     * @return The XML code of the primary color.
     */
    @ColorRes
    public static int getPrimaryColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.colorPrimaryOrange;
        } else if(selectedTheme == Themes.BLUE) {
            //TODO
            throw new RuntimeException();
        } else if(selectedTheme == Themes.DARK) { //dark
            //TODO
            throw new RuntimeException();
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * Gets the window background color.
     *
     * @return The XML code of the window background color.
     */
    @ColorRes
    public static int getBackgroundColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.windowBackgroundOrange;
        } else if(selectedTheme == Themes.BLUE) {
            //TODO
            throw new RuntimeException();
        } else if(selectedTheme == Themes.DARK) { //dark
            //TODO
            throw new RuntimeException();
        }
        throw new RuntimeException("Theme error!");
    }

}
