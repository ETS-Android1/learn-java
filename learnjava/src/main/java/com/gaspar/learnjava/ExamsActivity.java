package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillExamsActivityTask;
import com.gaspar.learnjava.curriculum.Exam;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

/**
 * Activity that displays all exams.
 */
public class ExamsActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, UpdatableActivity {

    public volatile boolean successfulLoad;

    /**
     * Exam view that is updated when the associated exams activity finishes.
     */
    private View updateView;

    /**
     * View that displays ads in this activity.
     */
    private AdView adView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.exams);
        setUpUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }

    private void setUpUI() {
        new FillExamsActivityTask().execute(this); //add exam selectors

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        adView = LearnJavaAds.loadBannerAd(R.string.ad_unit_id_banner_exams, findViewById(R.id.adContainer));
    }

    /**
     * Reload the exam view of the previously finished exam to reflect possible changes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK || data == null) return;
        if(requestCode == CoursesActivity.EXAM_REQUEST_CODE) { //only care about exam requests
            Exam exam = (Exam) data.getSerializableExtra(Exam.EXAM_PREFERENCE_STRING);
            if(exam == null) return;
            exam.queryAndDisplayStatus(updateView, ExamsActivity.this); //update view is exam view now
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Handle navigation view item clicks here.
        Intent intent = null;
        if(id == R.id.nav_tasks) {
            intent = new Intent(this, TasksActivity.class);
        } else if(id == R.id.nav_guide) {
            intent = new Intent(this, GuideActivity.class);
        } else if(id == R.id.nav_courses) {
            intent = new Intent(this, CoursesActivity.class);
        } else if(id == R.id.nav_starter_screen) {
            intent = new Intent(this, LearnJavaActivity.class);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(intent == null) return true;
        startActivity(intent); //start selected activity
        return true;
    }

    @Override
    public void setUpdateView(View updateView) {
        this.updateView = updateView;
    }
}
