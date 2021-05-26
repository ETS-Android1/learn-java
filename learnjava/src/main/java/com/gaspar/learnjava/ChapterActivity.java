package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillChapterActivityTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.LogUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * An activity that displays all components of one chapter.
 */
public class ChapterActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The chapter that was passed with the intent. It has no components.
     */
    private Chapter passedChapter;

    /**
     * The exam of this chapter. This may be needed, if the completion of this chapter unlocks the exam.
     */
    private Exam passedExam;

    /**
     * Ad object that is used to display interstitial (full screen) ad on activity close.
     */
    private InterstitialAd interstitialAd;

    /**
     * Stores if the chapter was "confirmed" by the user scrolling to the bottom. In these cases the
     * confirm method ({@link Chapter#markChapterAsCompleted(Context)} will not be called again.
     */
    private boolean confirmedWithScrolling;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter);
        confirmedWithScrolling = false;
        if(getIntent().getExtras() == null) { //should not happen
            LogUtils.logError("Incorrect behaviour: No extras passed!");
            finish();
        }
        //this chapter object has no components, only id and name
        passedChapter = (Chapter)getIntent().getExtras().getSerializable(Chapter.CHAPTER_PREFERENCE_STRING);
        if(passedChapter != null) {
            setTitle(passedChapter.getName());
        } else {
            LogUtils.logError("Incorrect behaviour: No chapter passed in extras!");
            finish();
        }
        if(getIntent().getExtras().containsKey(Exam.EXAM_PREFERENCE_STRING)) { //get passed exam, if included
            passedExam = (Exam) getIntent().getExtras().getSerializable(Exam.EXAM_PREFERENCE_STRING);
        }
        setUpUI(passedChapter);
        //load interstitial ad
        if(LearnJavaAds.LOAD_ADS) {
            int adId = LearnJavaAds.DEBUG_ADS ? R.string.ad_unit_id_interstitial_test : R.string.ad_unit_id_interstitial_chapter;
            InterstitialAd.load(this, getString(adId), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd iad) {
                    interstitialAd = iad;
                    LogUtils.log("Interstitial Ad loaded!");
                }
            });
        }
    }

    private void setUpUI(Chapter receivedChapter) {
        new FillChapterActivityTask(receivedChapter).execute(this); //show component views

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //make it so that when the bottom of the chapter is reached (in the scroll view), it acts like pressing the completed button
        final ScrollView scrollView = findViewById(R.id.chapterComponentsLayout);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if(!scrollView.canScrollVertically(1) && !confirmedWithScrolling) {
                //can't scroll down, so we are at the bottom
                confirmedWithScrolling = true;
                LogUtils.log("Chapter confirmed with scroll to bottom!");
                passedChapter.markChapterAsCompleted(this); //mark as completed
            }
        });
    }

    /**
     * Settings button (in the toolbar) click handler.
     */
    public void settingsOnClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the uses confirms reading the chapter. Marks this chapter as completed, and closes
     * the activity. If the {@link #passedExam} was given to this activity, then it is returned in the
     * result intent.
     */
    public void chapterConfirmedOnClick(View v) {
        if(!confirmedWithScrolling) {
            passedChapter.markChapterAsCompleted(ChapterActivity.this); //update database, check if all chapters are confirmed
        }
        Intent result = new Intent();
        result.putExtra(Chapter.CHAPTER_PREFERENCE_STRING, passedChapter); //return the chapter object
        if(passedExam != null) { //return the exam object, if it was passed
            result.putExtra(Exam.EXAM_PREFERENCE_STRING, passedExam);
        }
        setResult(Activity.RESULT_OK, result);
        //show ad with some possibility
        if(interstitialAd != null && LearnJavaAds.rollForAd()) interstitialAd.show(this);
        ChapterActivity.this.finish(); //close itself
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //set the result to completed, IF the end was reached with scrolling
            if(confirmedWithScrolling) {
                Intent result = new Intent();
                result.putExtra(Chapter.CHAPTER_PREFERENCE_STRING, passedChapter); //return the chapter object
                if(passedExam != null) { //return the exam object, if it was passed
                    result.putExtra(Exam.EXAM_PREFERENCE_STRING, passedExam);
                }
                setResult(Activity.RESULT_OK, result);
            }
            //show ad with some possibility
            if(interstitialAd != null && LearnJavaAds.rollForAd()) interstitialAd.show(this);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item);
        return true;
    }

    /**
     * Called when an activity started from here finished. For example the user deciding about
     * bluetooth is handled here.
     * @param requestCode Identifies what activity finished.
     * @param resultCode Stores the result.
     * @param data Extra information.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ClipSyncActivity.REQUEST_ENABLE_BT) { //the user has decided about bluetooth
            if(resultCode == RESULT_OK) { //the user chose to turn on bluetooth
                LearnJavaBluetooth.getInstance().turnOnBluetooth();

                //show the user that he should try again now
                Snackbar.make(findViewById(R.id.chapterComponentsLayout), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            } else { //complain
                Snackbar.make(findViewById(R.id.chapterComponentsLayout), getString(R.string.clip_sync_bluetooth_cancelled),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This is the result of the user deciding to allow location permission or not. On rare cases it is possible
     * that the user has to enable location permission in this activity for bluetooth.
     * @param requestCode The identifier of this request.
     * @param permissions Array of requested permissions, only contains locations.
     * @param grantResults Array of user decisions, only the location decision here.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ClipSyncActivity.REQUEST_ALLOW_LOCATION) {
            //the user denied location permission
            if(grantResults.length==0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.chapterComponentsLayout), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_LONG).show();
            } else { //granted, ask to try again now
                Snackbar.make(findViewById(R.id.chapterComponentsLayout), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
