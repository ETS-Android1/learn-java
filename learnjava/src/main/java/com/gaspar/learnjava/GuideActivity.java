package com.gaspar.learnjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillGuideActivityTask;
import com.gaspar.learnjava.curriculum.components.CodeHostingActivity;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity where the application guide is displayed.
 */
public class GuideActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, CodeHostingActivity {

    /**
     * The constant used to find if the user read the guide in the preferences.
     */
    @VisibleForTesting
    public static final String GUIDE_READ_PREFERENCE = "guide_read_pref";

    /**
     * Used at component loading.
     */
    public volatile boolean successfulLoad;

    /**
     * This object can start bluetooth enable requests, and is prepared to handle the result.
     */
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_guide);
        setUpUI();
        //the guide was opened, save this information
        final SharedPreferences preferences = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(GUIDE_READ_PREFERENCE, true).apply();
        //set up bluetooth request handling
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

    private void setUpUI() {
        toolbar = findViewById(R.id.toolbarExam);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_guide_root);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_guide_root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_guide_root);
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

    /**
     * Initializes the preferences so that it contains the {@link #GUIDE_READ_PREFERENCE} value,
     * which stores if the user has read the guide or not. This is called on application launch.
     */
    public static void initializeGuideReadPreference(@NonNull final AppCompatActivity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!preferences.contains(GUIDE_READ_PREFERENCE)) {
            //at first, the guide was not read
            preferences.edit().putBoolean(GUIDE_READ_PREFERENCE, false).apply();
        }
    }

    /**
     * Shows a dialog which informs the user that they haven't read the guide yet.
     * Offers to open the guide.
     * @param activity Activity in which the dialog opens.
     * @param runElse Will be called if the dialog is displayed, but the user says no.
     */
    public static void displayGuideNotReadDialogIfNeeded(@NonNull final AppCompatActivity activity, @Nullable final Runnable runElse) {
        final SharedPreferences preferences = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(preferences.getBoolean(GUIDE_READ_PREFERENCE, false)) {
            //guide was read already, don't do anything
            //run whatever needed
            if(runElse != null) runElse.run();
        } else {
            //the guide was not read
            new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle())
                    .setTitle(R.string.guide)
                    .setMessage(R.string.guide_not_read)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        //start guide
                        Intent i = new Intent(activity, GuideActivity.class);
                        activity.startActivity(i);
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        dialog.dismiss();
                        //run whatever needed if no is selected
                        if(runElse != null) runElse.run();
                    })
                    .show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResultLauncher<Intent> getBluetoothEnableLauncher() {
        return bluetoothEnableLauncher;
    }
}
