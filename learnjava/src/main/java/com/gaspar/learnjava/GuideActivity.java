package com.gaspar.learnjava;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillGuideActivityTask;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.google.android.material.navigation.NavigationView;

/**
 * Activity where the application guide is displayed.
 */
public class GuideActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Used at component loading.
     */
    public volatile boolean successfulLoad;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.guide);
        setUpUI();
    }

    private void setUpUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        new FillGuideActivityTask().execute(this); //parse components
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
        DrawerUtils.handleDrawerOnClick(this, item);
        return true;
    }

    /**
     * When the close guide button is clicked.
     */
    public void closeButtonOnClick(View view) {
        finish();
    }
}
