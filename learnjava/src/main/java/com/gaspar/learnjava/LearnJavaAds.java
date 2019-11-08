package com.gaspar.learnjava;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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
     * Loads a banner ad, then adds it to the given parent view.
     *
     * @param realId The non-test id of the ad unit. Only used in non debug mode.
     * @param parent The parent view to which the ad view will be added.
     * @return The loaded ad view.
     */
    static AdView loadBannerAd(@StringRes int realId, ViewGroup parent) {
        Context context = parent.getContext();
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        if(!LearnJavaActivity.DEBUG) { //load real ad only when not in debug
            adView.setAdUnitId(context.getString(realId));
            adView.loadAd(new AdRequest.Builder().build());
        } else { //load test ad in debug, but only when debug ads are enabled
            adView.setAdUnitId(context.getString(R.string.ad_unit_id_banner_test));
            if(LearnJavaActivity.LOAD_DEBUG_ADS) {
                adView.loadAd(new AdRequest.Builder().build());
            } else {
                parent.getLayoutParams().height = 0;
            }
        }
        parent.addView(adView); //add to parent
        return adView;
    }

    /**
     * Starts the loading of an interstitial ad, according to debug preferences:
     * {@value LearnJavaActivity#DEBUG}, {@value LearnJavaActivity#LOAD_DEBUG_ADS}.
     *
     * @param realId The non-test id of the ad unit. Only used in non debug mode.
     * @return The interstitial ad that started loading.
     */
    static InterstitialAd loadInterstitialAd(Context context, @StringRes int realId) {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        if(!LearnJavaActivity.DEBUG) { //only show real ad in non debug mode
            interstitialAd.setAdUnitId(context.getString(realId));
            interstitialAd.loadAd(new AdRequest.Builder().build());
        } else { //only load test ad in debug, and only if it's enabled
            interstitialAd.setAdUnitId(context.getString(R.string.ad_unit_id_interstitial_test));
            if (LearnJavaActivity.LOAD_DEBUG_ADS)
                interstitialAd.loadAd(new AdRequest.Builder().build());
        }
        return interstitialAd;
    }

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
