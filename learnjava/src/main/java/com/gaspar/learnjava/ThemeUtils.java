package com.gaspar.learnjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

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
    static void initSelectedTheme(final @NonNull Context context) {
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
     * Saves a new selected theme in {@link #selectedTheme} and in the preferences as well.
     */
    static void updateSelectedTheme(final @NonNull Context context, @Themes int newTheme) {
        selectedTheme = newTheme;
        SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(THEME_PREF_NAME, newTheme).apply();
    }

    /**
     * Gets the primary color.
     *
     * @return The XML code of the primary color.
     */
    @ColorRes
    static int getPrimaryColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.colorPrimaryOrange;
        } else if(selectedTheme == Themes.BLUE) {
            return R.color.colorPrimaryBlue;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.color.colorPrimary_Dark;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * Gets the window background color.
     *
     * @return The XML code of the window background color.
     */
    @ColorRes
    static int getBackgroundColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.windowBackgroundOrange;
        } else if(selectedTheme == Themes.BLUE) {
            return R.color.windowBackgroundBlue;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.color.windowBackground_Dark;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * @return The style resource of the currently selected theme.
     */
    @StyleRes
    public static int getTheme() {
        if (selectedTheme == Themes.ORANGE) {
            return R.style.AppTheme;
        } else if(selectedTheme == Themes.BLUE) {
            return R.style.BlueTheme;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.style.DarkTheme;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * @return The color id of the text color of the selected theme.
     */
    @ColorRes
    static int getTextColor() {
        if (selectedTheme == Themes.ORANGE) {
            return android.R.color.black;
        } else if(selectedTheme == Themes.BLUE) {
            return android.R.color.black;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.color.darkThemeTextColor;
        }
        throw new RuntimeException("Theme error!");
    }

    public static boolean isDarkTheme() {
        return selectedTheme == Themes.DARK;
    }

    /**
     * @return A color list that can be applied to an image button for tint.
     */
    static ColorStateList getImageButtonTintList(@NonNull final Context context) {
        if (selectedTheme == Themes.ORANGE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_orange_tint);
        } else if(selectedTheme == Themes.BLUE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_blue_tint);
        } else if(selectedTheme == Themes.DARK) { //dark
            return ContextCompat.getColorStateList(context, R.color.image_button_dark_tint);
        }
        throw new RuntimeException("Theme error!");

    }

    /**
     * @return A color list that can be applied to an image button for background tint.
     */
    static ColorStateList getImageButtonBackgroundTintList(@NonNull final Context context) {
        if (selectedTheme == Themes.ORANGE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_orange_background_tint);
        } else if(selectedTheme == Themes.BLUE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_blue_background_tint);
        } else if(selectedTheme == Themes.DARK) { //dark
            return ContextCompat.getColorStateList(context, R.color.image_button_dark_background_tint);
        }
        throw new RuntimeException("Theme error!");

    }
}
