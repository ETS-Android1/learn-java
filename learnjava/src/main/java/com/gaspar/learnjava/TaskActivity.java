package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillTaskActivityTask;
import com.gaspar.learnjava.asynctask.LearnJavaExecutor;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.LogUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity that shows a task. The user can see the solution here as well, with the buttons on the bottom of the
 * activity.
 */
public class TaskActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Stores the loading completed with success, or not.
     */
    public volatile boolean successfulLoad;

    /**
     * The task who's components and solution components are displayed by the activity. Loaded
     * in the background by {@link FillTaskActivityTask}.
     */
    private volatile Task displayedTask;

    /**
     * Ad object that is used to display interstitial (full screen) ad on activity close.
     */
    private InterstitialAd interstitialAd;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_task);
        if(getIntent().getExtras() == null) { //should not happen
            LogUtils.logError("Incorrect behaviour: No extras passed!");
            finish();
        }
        //this task has no components, only name and id
        Task passedTask = (Task) getIntent().getExtras().getSerializable(Task.TASK_PREFERENCE_STRING);
        if(passedTask == null) {
            LogUtils.logError("Incorrect behaviour: No task passed in extras!");
            finish();
        } else {
            setTitle(passedTask.getName());
            setUpUI(passedTask);

            if(LearnJavaAds.LOAD_ADS) {
                int adId = LearnJavaAds.DEBUG_ADS ? R.string.ad_unit_id_interstitial_test : R.string.ad_unit_id_interstitial_task;
                InterstitialAd.load(this, getString(adId), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd iad) {
                        interstitialAd = iad;
                    }
                });
            }
        }
    }

    private void setUpUI(Task passedTask) {
        new FillTaskActivityTask(passedTask).execute(this); //load task components, handles checkbox status

        toolbar = findViewById(R.id.toolbarTask);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_task_root);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void closeTaskOnClick(View view) {
        Intent result = new Intent();
        result.putExtra(Task.TASK_PREFERENCE_STRING, displayedTask);
        setResult(Activity.RESULT_OK, result); //return result
        //show ad with some possibility
        if(interstitialAd != null && LearnJavaAds.rollForAd()) interstitialAd.show(this);
        finish();
    }

    /**
     * Keeps track of the visibility of the solution.
     */
    private boolean solutionShowing;

    //view holders
    private ImageView iconView;
    private TextView promptText;
    private View taskComponentsView;
    private View solutionComponentsView;

    /**
     * Click animation for the hide/show layout
     */
    private Animation clickAnimation;

    /**
     * Called when the hide/show layout is tapped. Slides in/out solution, also
     * updates the displayed text and icon.
     */
    public void hideShowSolutionOnClick(View view) {
        if(clickAnimation == null) {
            clickAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.click);
        }
        view.startAnimation(clickAnimation);
        if(iconView == null) iconView = view.findViewById(R.id.hideShowSolutionIcon);
        if(promptText == null) promptText = view.findViewById(R.id.hideShowSolutionText);
        if(solutionComponentsView == null) solutionComponentsView = findViewById(R.id.solutionComponents);
        if(taskComponentsView == null) taskComponentsView = findViewById(R.id.taskComponents);
        if(solutionShowing) {
            AnimationUtils.slideOutHorizontal(solutionComponentsView, AnimationUtils.Direction.RIGHT, this);
            AnimationUtils.slideInHorizontal(taskComponentsView, AnimationUtils.Direction.LEFT, this);
            promptText.setText(R.string.show_solution);
            final Animation rotateAnimation = new RotateAnimation(180.0f, 0.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(AnimationUtils.DURATION);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setRepeatCount(0);
            iconView.startAnimation(rotateAnimation);
        } else { //make solution appear
            AnimationUtils.slideOutHorizontal(taskComponentsView, AnimationUtils.Direction.LEFT, this);
            AnimationUtils.slideInHorizontal(solutionComponentsView, AnimationUtils.Direction.RIGHT, this);
            promptText.setText(R.string.hide_solution);
            final Animation rotateAnimation = new RotateAnimation(0.0f, 180.0f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(AnimationUtils.DURATION);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setRepeatCount(0);
            iconView.startAnimation(rotateAnimation);
        }
        solutionShowing = !solutionShowing;
    }

    /**
     * Settings button (in the toolbar) click handler.
     */
    public void settingsOnClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the task completion marker checkbox is called. No need to call this on background
     * thread, as it starts it's own thread.
     */
    @UiThread
    public void updateTaskStatus(boolean completed, int taskId) {
        LearnJavaExecutor.getInstance().executeOnBackgroundThread(() -> {
            int status = completed ? com.gaspar.learnjava.curriculum.Status.COMPLETED
                    : com.gaspar.learnjava.curriculum.Status.UNLOCKED;
            TaskStatus newStatus = new TaskStatus(taskId, status);
            LearnJavaDatabase.getInstance(TaskActivity.this).getTaskDao().updateTaskStatus(newStatus); //update in database
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_task_root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent result = new Intent();
            result.putExtra(Task.TASK_PREFERENCE_STRING, displayedTask);
            setResult(Activity.RESULT_OK, result); //return result
            if(interstitialAd != null && LearnJavaAds.rollForAd()) interstitialAd.show(this); //show ad with some possibility
            super.onBackPressed();
        }
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
                Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            } else { //complain
                Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_bluetooth_cancelled),
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
                Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_LONG).show();
            } else { //granted, ask to try again now
                Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_task_root);
        return true;
    }

    public Task getDisplayedTask() {
        return displayedTask;
    }

    public void setDisplayedTask(Task displayedTask) {
        this.displayedTask = displayedTask;
    }
}
