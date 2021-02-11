package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter);
        if(getIntent().getExtras() == null) { //should not happen
            Log.d("LearnJava", "Incorrect behaviour: No extras passed!");
            finish();
        }
        //this chapter object has no components, only id and name
        passedChapter = (Chapter)getIntent().getExtras().getSerializable(Chapter.CHAPTER_PREFERENCE_STRING);
        if(passedChapter != null) {
            setTitle(passedChapter.getName());
        } else {
            Log.d("LearnJava", "Incorrect behaviour: No chapter passed in extras!");
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
                    Log.d("LearnJava", "Interstitial Ad loaded!");
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
    }

    /**
     * Settings button (in the toolbar) click handler.
     */
    public void settingsOnClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

    /**
     * Called when the uses confirms reading the chapter.
     */
    public void chapterConfirmedOnClick(View v) {
        passedChapter.markChapterAsCompleted(ChapterActivity.this); //update database, check is all chapters are confirmed
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ClipSyncActivity.REQUEST_ENABLE_BT) { //the user has decided about bluetooth
            if(resultCode == RESULT_OK) { //the user chose to turn on bluetooth
                LearnJavaBluetooth.getInstance().turnOnBluetooth();
            } else { //complain
                Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.clip_sync_bluetooth_cancelled),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
