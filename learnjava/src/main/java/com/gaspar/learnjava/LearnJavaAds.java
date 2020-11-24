package com.gaspar.learnjava;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;

import java.util.Random;

/**
 * Contains methods that are related to ad loading. These methods must be called on UI thread,
 * as they contain calls to mobile ads SDK.
 */
@UiThread
abstract class LearnJavaAds {

    /**
     * Constant that determines if ads will or won't be test(debug) ads. This only has a real effect if {@link #LOAD_ADS}
     * is true.
     * <p>
     * DO NOT SET THIS HERE, it's set automatically from the build variant.
     */
    public static boolean DEBUG_ADS;

    /**
     * A constant that determines if ads will be loaded or not.
     * <p>
     * DO NOT SET THIS HERE, it's set automatically from the build variant.
     */
    public static boolean LOAD_ADS;

    /**
     * Sets the {@link #DEBUG_ADS} and {@link #LOAD_ADS} booleans from the build variant.
     */
    static void initAdConstants(@NonNull final Context context) {
        DEBUG_ADS = context.getResources().getBoolean(R.bool.is_debug);
        LOAD_ADS = context.getResources().getBoolean(R.bool.load_ads);
    }

    /**
     * The possibility of an ad appearing.
     */
    private static final float AD_POSSIBILITY = 0.5f;

    /**
     * Random number generator.
     */
    private static final Random random = new Random();

    /**
     * Loads a banner ad, then adds it to the given parent view, according to debug preferences: {@link #DEBUG_ADS}
     * and {@link #LOAD_ADS}.
     *
     * @param id The id of the ad unit.
     * @param parent The parent view to which the ad view will be added. It is displaying some text
     *               that indicates the ad is loading.
     * @return The loaded ad view.
     */
    static AdView loadBannerAd(@StringRes int id, ViewGroup parent) {
        Context context = parent.getContext();
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(context.getString(id));
        if (LOAD_ADS) {
            adView.loadAd(new AdRequest.Builder().build());
            adView.setAdListener(new AdListener() { //changes will happen when it's loaded
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    parent.removeAllViews(); //clear the loading indicator text
                    parent.addView(adView); //add to parent
                }
                @Override
                public void onAdFailedToLoad(LoadAdError e) {
                    super.onAdFailedToLoad(e);
                    final TextView statusView = parent.findViewById(R.id.adStatusLabel);
                    statusView.setText(R.string.ad_load_fail);
                }
            });
        } else {
            parent.getLayoutParams().height = 0;
        }
        return adView;
    }

    /**
     * Starts the loading of an interstitial ad, according to debug preferences: {@link #DEBUG_ADS}
     * and {@link #LOAD_ADS}.
     *
     * @param id The id of the ad unit.
     * @return The interstitial ad that may or may not have started loading.
     */
    static InterstitialAd loadInterstitialAd(Context context, @StringRes int id) {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(id));
        if(LOAD_ADS) { //only initiate loading if its enabled
            interstitialAd.setImmersiveMode(true);
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
