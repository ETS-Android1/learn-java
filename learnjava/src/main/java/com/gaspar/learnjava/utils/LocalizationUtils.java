package com.gaspar.learnjava.utils;

import java.util.Locale;

/**
 * Contains utility methods about the app's localisation. Resources have built
 * in support for localisation, but the asset folder does not. To get around this,
 * I create top level folders in the assets for each supported language, and in these
 * are the translated assets.
 *
 * If there is a localized asset folder, then all assets must be present in that
 * folder as well.
 */
public abstract class LocalizationUtils {

    /**
     * Language code for english.
     */
    private static final String ENGLISH = "en";

    /**
     * Language code for hungarian.
     */
    private static final String HUNGARIAN = "hu";

    /**
     * Finds the path to the root of the localized asset folder.
     * @return The path to the root of the localized assets. Path to the english
     * assets if the current language is not supported.
     */
    public static String getLocalizedAssetPath() {
        String lang = Locale.getDefault().getLanguage(); //en, hu, etc...
        //Log.d("LearnJava", "Detected language is: " + lang);
        if (HUNGARIAN.equals(lang)) {
            return HUNGARIAN;
        }
        //if there are other supported languages, they must return their code here.
        return ENGLISH;
    }
}
