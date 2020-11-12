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
     * The possibility of an ad appearing.
     */
    private static final float AD_POSSIBILITY = 0.5f;

    /**
     * Random number generator.
     */
    private static final Random random = new Random();

    /**
     * Loads a banner ad, then adds it to the given parent view, according to debug preferences: {@link LearnJavaActivity#DEBUG_ADS}
     * and {@value LearnJavaActivity#LOAD_ADS}.
     *
     * @param realId The non-test id of the ad unit. Only used in non debug mode.
     * @param parent The parent view to which the ad view will be added. It is displaying some text
     *               that indicates the ad is loading.
     * @return The loaded ad view.
     */
    static AdView loadBannerAd(@StringRes int realId, ViewGroup parent) {
        Context context = parent.getContext();
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        if(!LearnJavaActivity.DEBUG_ADS) { //load real ad only when not in debug
            adView.setAdUnitId(context.getString(realId));
            if(LearnJavaActivity.LOAD_ADS) adView.loadAd(new AdRequest.Builder().build());
        } else { //load test ad in debug, but only when ad loading is enabled
            adView.setAdUnitId(context.getString(R.string.ad_unit_id_banner_test));
            if (LearnJavaActivity.LOAD_ADS) {
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
        }
        return adView;
    }

    /**
     * Starts the loading of an interstitial ad, according to debug preferences: {@link LearnJavaActivity#DEBUG_ADS}
     * and {@value LearnJavaActivity#LOAD_ADS}.
     *
     * @param realId The non-test id of the ad unit. Only used in non debug mode.
     * @return The interstitial ad that may or may not have started loading.
     */
    static InterstitialAd loadInterstitialAd(Context context, @StringRes int realId) {
        InterstitialAd interstitialAd = new InterstitialAd(context);
        if(!LearnJavaActivity.DEBUG_ADS) { //only show real ad if non debug mode is enabled for ads
            interstitialAd.setAdUnitId(context.getString(realId));
        } else { //only load test ad in debug ad mode
            interstitialAd.setAdUnitId(context.getString(R.string.ad_unit_id_interstitial_test));
        }
        if(LearnJavaActivity.LOAD_ADS) { //only initiate loading if its enabled
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
