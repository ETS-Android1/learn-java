package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.asynctask.FillChapterActivityTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.components.CodeHostingActivity;
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
public class ChapterActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, CodeHostingActivity {

    /**
     * The chapter that was passed with the intent. It has no components when extracted from the intent (because the
     * loading happens here). AFTER the loading is finished, this chapter will have its components.
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

    /**
     * This object can start bluetooth enable requests, and is prepared to handle the result.
     */
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
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
        //define what to happen when a bluetooth enable request happens
        bluetoothEnableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) { //the user chose to turn on bluetooth
                    LearnJavaBluetooth.getInstance().turnOnBluetooth();

                    //show the user that he should try again now
                    Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_bluetooth_try_again),
                            Snackbar.LENGTH_LONG).show();
                } else { //complain
                    Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_bluetooth_cancelled),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        );
    }

    private void setUpUI(Chapter receivedChapter) {
        new FillChapterActivityTask(receivedChapter).execute(this); //show component views

        toolbar = findViewById(R.id.toolbarChapter);
        if(passedChapter != null) toolbar.setTitle(passedChapter.getName());
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_chapter_root);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //make it so that when the bottom of the chapter is reached (in the recycler view), it acts like pressing the completed button
        final RecyclerView componentsView = findViewById(R.id.chapterComponents);
        componentsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    if(!confirmedWithScrolling) {
                        confirmedWithScrolling = true;
                        LogUtils.log("Chapter confirmed with scroll to bottom!");
                        //System.out.println("Chapter confirmed with scroll to bottom!");
                        passedChapter.markChapterAsCompleted(ChapterActivity.this); //mark as completed
                    }
                }
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_chapter_root);
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
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_chapter_root);
        return true;
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
                Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_LONG).show();
            } else { //granted, ask to try again now
                Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public Chapter getPassedChapter() {
        return passedChapter;
    }

    public void setPassedChapter(Chapter passedChapter) {
        this.passedChapter = passedChapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResultLauncher<Intent> getBluetoothEnableLauncher() {
        return bluetoothEnableLauncher;
    }

    /**
     * Used by the recycler view in the {@link ChapterActivity}, to insert a button to the end of the
     * recycler.
     */
    public static class ChapterFooterHolder extends RecyclerView.ViewHolder {

        /**
         * This button closes the activity.
         */
        public Button closeButton;

        /**
         * Create a holder.
         * @param view This is expected to be inflated from R.layout.view_close_chapter
         */
        public ChapterFooterHolder(View view) {
            super(view);
            closeButton = view.findViewById(R.id.chapterConfirmButton);
        }

    }
}
