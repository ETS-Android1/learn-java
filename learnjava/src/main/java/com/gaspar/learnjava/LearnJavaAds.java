package com.gaspar.learnjava;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;

/**
 * Contains methods that are related to ad loading. These methods must be called on UI thread,
 * as they contain calls to mobile ads SDK.
 */
@UiThread
abstract class LearnJavaAds {

    /**
     * The possibility of an ad appearing.
     */
    private static final float AD_POSSIBILITY = 0.5f;

    /**
     * Random number generator.
     */
    private static final Random random = new Random();

    /**
     * Shows an ad using this (already initialized and loaded) interstitial ad object.
     */
    static void showInterstitialAd(@NonNull final InterstitialAd interstitialAd) {
        if(interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d("LearnJava", "Attempted to show unloaded ad...");
        }
    }

    /**
     * Randomly decides if an ad should appear. Uses {@value AD_POSSIBILITY}.
     */
    static boolean rollForAd() {
        float generatedNumber = 0.0f + random.nextFloat() * (1.0f - 0.0f);
        return generatedNumber <= AD_POSSIBILITY;
    }

}
