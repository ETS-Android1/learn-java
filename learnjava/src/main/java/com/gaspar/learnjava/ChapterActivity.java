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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillChapterActivityTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;

public class ChapterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The chapter that was passed with the intent. It has no components.
     */
    private Chapter passedChapter;

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
        setUpUI(passedChapter);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ad_unit_id_interstitial_test)); //TODO: change to real id
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void setUpUI(Chapter receivedChapter) {
        new FillChapterActivityTask(receivedChapter).execute(this); //show component views

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
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
        setResult(Activity.RESULT_OK, result);
        //show ad with some possibility
        if(LearnJavaAds.rollForAd()) LearnJavaAds.showInterstitialAd(interstitialAd);
        ChapterActivity.this.finish(); //close itself
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //show ad with some possibility
            if(LearnJavaAds.rollForAd()) LearnJavaAds.showInterstitialAd(interstitialAd);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Handle navigation view item clicks here.
        Intent intent = null;
        if(id == R.id.nav_tasks) {
            intent = new Intent(this, TasksActivity.class);
        } else if(id == R.id.nav_exams) {
            intent = new Intent(this, ExamsActivity.class);
        } else if(id == R.id.nav_guide) {
            intent = new Intent(this, GuideActivity.class);
        } else if(id == R.id.nav_starter_screen) {
            intent = new Intent(this, LearnJavaActivity.class);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(intent == null) return true;
        startActivity(intent); //start selected activity
        return true;
    }
}
