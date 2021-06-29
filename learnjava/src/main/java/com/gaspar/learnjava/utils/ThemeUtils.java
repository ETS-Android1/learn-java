package com.gaspar.learnjava.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Gets runtime color values.
 */
public abstract class ThemeUtils {

    /**
     * The currently selected theme.
     */
    @Themes
    private static int selectedTheme;

    /**
     * Name of the selected theme preference.
     */
    private static final String THEME_PREF_NAME = "selected_theme";

    /**
     * Stores if the theme initialization already happened or not.
     */
    private static boolean initialized = false;

    /**
     * Called on the creation of a {@link com.gaspar.learnjava.ThemedActivity}, initializes the selected theme from preferences.
     * It will only do real work on the first activity start, which happens when the application starts.
     */
    public static void initSelectedTheme(final @NonNull Context context) {
        if(!initialized) { //only if it is not initialized
            SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            if(prefs.contains(THEME_PREF_NAME)) {
                selectedTheme = prefs.getInt(THEME_PREF_NAME, Themes.ORANGE);
            } else { //default theme is orange
                prefs.edit().putInt(THEME_PREF_NAME, Themes.ORANGE).apply();
                selectedTheme = Themes.ORANGE;
            }
            initialized = true; //save that initialization happened
        }
    }

    /**
     * Constants of the possible themes.
     */
    @IntDef
    public @interface Themes {
        int ORANGE = 0; //the default orange theme
        int DARK = 2; //dark theme
    }

    /**
     * Saves a new selected theme in {@link #selectedTheme} and in the preferences as well.
     */
    public static void updateSelectedTheme(final @NonNull Context context, @Themes int newTheme) {
        selectedTheme = newTheme;
        SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(THEME_PREF_NAME, newTheme).apply();
    }

    /**
     * Gets the primary color.
     * @return The XML code of the primary color.
     */
    @ColorRes
    public static int getPrimaryColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.colorPrimaryOrange;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.color.colorPrimary_Dark;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * Gets the window background color.
     * @return The XML code of the window background color.
     */
    @ColorRes
    public static int getBackgroundColor() {
        if (selectedTheme == Themes.ORANGE) {
            return R.color.windowBackgroundOrange;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.color.windowBackground_Dark;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * @return The style resource of the currently selected theme.
     */
    @StyleRes
    public static int getThemeStyleRes() {
        if (selectedTheme == Themes.ORANGE) {
            return R.style.AppTheme;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.style.DarkTheme;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * @return The color id of the text color of the selected theme.
     */
    @ColorRes
    public static int getTextColor() {
        if (selectedTheme == Themes.ORANGE) {
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
    public static ColorStateList getImageButtonTintList(@NonNull final Context context) {
        if (selectedTheme == Themes.ORANGE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_orange_tint);
        } else if(selectedTheme == Themes.DARK) { //dark
            return ContextCompat.getColorStateList(context, R.color.image_button_dark_tint);
        }
        throw new RuntimeException("Theme error!");

    }

    /**
     * @return A color list that can be applied to an image button for background tint.
     */
    public static ColorStateList getImageButtonBackgroundTintList(@NonNull final Context context) {
        if (selectedTheme == Themes.ORANGE) {
            return ContextCompat.getColorStateList(context, R.color.image_button_orange_background_tint);
        } else if(selectedTheme == Themes.DARK) { //dark
            return ContextCompat.getColorStateList(context, R.color.image_button_dark_background_tint);
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * Finds the style of the material alert dialogs, based on the current style.
     * @return A style ID.
     */
    public static @StyleRes int getThemedDialogStyle() {
        if (selectedTheme == Themes.ORANGE) {
            return R.style.AppDialogStyle;
        } else if(selectedTheme == Themes.DARK) { //dark
            return R.style.AppDialogStyleDark;
        }
        throw new RuntimeException("Theme error!");
    }

    /**
     * Constant in the preferences, which is used to find out if the
     * dark theme prompt needs to be shown.
     */
    @VisibleForTesting
    public static final String SHOW_DARK_THEME_PROMPT = "show_dark_theme_prompt";

    @VisibleForTesting
    public static boolean showDarkThemeDialog = true;

    /**
     * Shows a dialog to the user which informs them that a dark mode is available.
     */
    public static void showDarkThemePromptIfNeeded(@NonNull final AppCompatActivity activity) {
        if(!showDarkThemeDialog) { //eases some tests
            return;
        }
        final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(isDarkTheme()) {
            //the user already switched to dark theme!
            preferences.edit().putBoolean(SHOW_DARK_THEME_PROMPT, true).apply();
            return;
        }
        if(!preferences.contains(SHOW_DARK_THEME_PROMPT)) {
            //save that it was shown
            preferences.edit().putBoolean(SHOW_DARK_THEME_PROMPT, true).apply();
            //never been shown before, show it now
            new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle())
                    .setTitle(R.string.dark_theme_title)
                    .setMessage(R.string.dark_theme_info)
                    .setPositiveButton(R.string.dark_theme_accepted, (dialog, which) -> {
                        dialog.dismiss();
                        ThemeUtils.updateSelectedTheme(activity, Themes.DARK); //update
                        activity.setTheme(ThemeUtils.getThemeStyleRes());
                        activity.recreate();
                    })
                    .setNegativeButton(R.string.dark_theme_denied, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
        //otherwise the prompt was already shown
    }

    private static final String SYSTEM_DARK_THEME_NOTICED = "system_dark_theme_noticed";

    /**
     * If the system dark theme is on, it will inform the user that the application has a dark theme as well,
     * and offer to switch to dark theme. Will not show if it was already shown, or if the system is not in
     * dark mode, or if the user already changed the app to dark mode.
     * @param activity Activity.
     */
    public static void showSystemDarkModeDialogIfNeeded(@NonNull final AppCompatActivity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int systemNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(systemNightMode == Configuration.UI_MODE_NIGHT_YES) {
            //the system is in dark mode
            if(!preferences.contains(SYSTEM_DARK_THEME_NOTICED)) {
                //the app has not asked about this before
                if(!isDarkTheme()) {
                    //the app is not in dark theme already
                    new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle())
                            .setTitle(R.string.dark_theme_title)
                            .setMessage(R.string.dark_theme_system)
                            .setPositiveButton(R.string.dark_theme_accepted, (dialog, which) -> {
                                //the user wants to turn on application dark mode
                                dialog.dismiss();
                                ThemeUtils.updateSelectedTheme(activity, Themes.DARK); //update
                                activity.setTheme(ThemeUtils.getThemeStyleRes());
                                activity.recreate();
                            })
                            .setNegativeButton(R.string.dark_theme_denied, (dialog, which) -> {
                                //the user does not want the application dark mode
                                dialog.dismiss();
                            })
                            .show();
                }
                //save that we asked about this, so it won't happen again
                preferences.edit().putBoolean(SYSTEM_DARK_THEME_NOTICED, true).apply();
            }
        }
    }
}
