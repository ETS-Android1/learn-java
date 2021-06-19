package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillExamsActivityTask;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

/**
 * Activity that displays all exams in a list, and the user can start the ones that they have
 * unlocked and available.
 */
public class ExamsActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, UpdatableActivity {

    /**
     * Indicates if the exams have successfully loaded.
     */
    public volatile boolean successfulLoad;

    /**
     * Exam view that is updated when the associated exams activity finishes.
     */
    private View updateView;

    /**
     * View that displays ads in this activity.
     */
    private AdView adView;

    /**
     * Can start {@link ExamActivity}es and handle their result.
     */
    private ActivityResultLauncher<Intent> examActivityLauncher;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_exams);
        setUpUI();
        examActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data == null) return;
                        Exam exam = (Exam) data.getSerializableExtra(Exam.EXAM_PREFERENCE_STRING);
                        if(exam == null) return;
                        exam.queryAndDisplayStatus(updateView, ExamsActivity.this); //update view is exam view now
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView != null) adView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adView != null) adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adView != null) adView.resume();
    }

    /**
     * Initializes the activity's user interface.
     */
    private void setUpUI() {
        new FillExamsActivityTask().execute(this); //add exam selectors

        toolbar = findViewById(R.id.toolbarExams);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_exams_root);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if(LearnJavaAds.LOAD_ADS) {
            int adId = LearnJavaAds.DEBUG_ADS ? R.string.ad_unit_id_banner_test : R.string.ad_unit_id_banner_exams;
            adView = LearnJavaAds.loadBannerAd(adId, findViewById(R.id.adContainer));
        } else {
            findViewById(R.id.adContainer).setVisibility(View.GONE);
        }
    }

    /**
     * Settings button (in the toolbar) click handler.
     */
    public void settingsOnClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_exams_root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_exams_root);
        return true;
    }

    @Override
    public void setUpdateViews(View... updateViews) {
        this.updateView = updateViews[0];
    }

    /**
     * @return An object that can start {@link ExamActivity}es and handle their result.
     */
    public ActivityResultLauncher<Intent> getExamActivityLauncher() {
        return examActivityLauncher;
    }
}
