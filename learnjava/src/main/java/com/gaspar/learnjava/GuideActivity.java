package com.gaspar.learnjava;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

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
                Snackbar.make(findViewById(R.id.guideComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            } else { //complain
                Snackbar.make(findViewById(R.id.guideComponents), getString(R.string.clip_sync_bluetooth_cancelled),
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
                Snackbar.make(findViewById(R.id.guideComponents), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_LONG).show();
            } else { //granted, ask to try again now
                Snackbar.make(findViewById(R.id.guideComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * When the close guide button is clicked.
     */
    public void closeButtonOnClick(View view) {
        finish();
    }
}
