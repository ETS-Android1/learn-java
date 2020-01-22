package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillTaskActivityTask;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executors;

/**
 * Activity that shows a task.
 */
public class TaskActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

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
        setContentView(R.layout.task);
        if(getIntent().getExtras() == null) { //should not happen
            Log.d("LearnJava", "Incorrect behaviour: No extras passed!");
            finish();
        }
        //this task has no components, only name and id
        Task passedTask = (Task) getIntent().getExtras().getSerializable(Task.TASK_PREFERENCE_STRING);
        if(passedTask == null) {
            Log.d("LearnJava", "Incorrect behaviour: No task passed in extras!");
            finish();
        } else {
            setTitle(passedTask.getName());
            setUpUI(passedTask);
            interstitialAd = LearnJavaAds.loadInterstitialAd(this, R.string.ad_unit_id_interstitial_task);
        }
    }

    private void setUpUI(Task passedTask) {
        new FillTaskActivityTask(passedTask).execute(this); //load task components, handles checkbox status

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

    public void closeTaskOnClick(View view) {
        Intent result = new Intent();
        result.putExtra(Task.TASK_PREFERENCE_STRING, displayedTask);
        setResult(Activity.RESULT_OK, result); //return result
        //show ad with some possibility
        if(LearnJavaAds.rollForAd()) LearnJavaAds.showInterstitialAd(interstitialAd);
        finish();
    }

    /**
     * Keeps track of the visibility of the solution.
     */
    private boolean solutionShowing;

    //view holders
    private ImageView iconView;
    private TextView promptText;
    private View taskScroller;
    private View solutionScroller;

    /**
     * Click animation for the hide/show layout
     */
    private Animation clickAnimation;

    /**
     * Called when the hide/show layout is tapped. Slides in/out solution, also
     * updates the displayed text and icon.
     *
     * TODO: convert the entire solution layout to constraint and set scroller to match constraint height?
     */
    public void hideShowSolutionOnClick(View view) {
        if(clickAnimation == null) {
            clickAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.click);
        }
        view.startAnimation(clickAnimation);
        if(iconView == null) iconView = view.findViewById(R.id.hideShowSolutionIcon);
        if(promptText == null) promptText = view.findViewById(R.id.hideShowSolutionText);
        if(solutionScroller == null) solutionScroller = findViewById(R.id.solutionScroller);
        if(taskScroller == null) taskScroller = findViewById(R.id.taskScroller);
        if(solutionShowing) {
            AnimationUtils.slideOutHorizontal(solutionScroller, AnimationUtils.Direction.RIGHT, this);
            AnimationUtils.slideInHorizontal(taskScroller, AnimationUtils.Direction.LEFT, this);
            promptText.setText(R.string.show_solution);
            iconView.setImageResource(R.drawable.right_arrow);
        } else { //make solution appear
            AnimationUtils.slideOutHorizontal(taskScroller, AnimationUtils.Direction.LEFT, this);
            AnimationUtils.slideInHorizontal(solutionScroller, AnimationUtils.Direction.RIGHT, this);
            promptText.setText(R.string.hide_solution);
            iconView.setImageResource(R.drawable.left_arrow);
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
        Executors.newSingleThreadExecutor().execute(() -> {
            int status = completed ? com.gaspar.learnjava.curriculum.Status.COMPLETED
                    : com.gaspar.learnjava.curriculum.Status.UNLOCKED;
            TaskStatus newStatus = new TaskStatus(taskId, status);
            LearnJavaDatabase.getInstance(TaskActivity.this).getTaskDao().updateTaskStatus(newStatus); //update in database
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent result = new Intent();
            result.putExtra(Task.TASK_PREFERENCE_STRING, displayedTask);
            setResult(Activity.RESULT_OK, result); //return result
            if(LearnJavaAds.rollForAd()) LearnJavaAds.showInterstitialAd(interstitialAd); //show ad with some possibility
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
        } else if(id == R.id.nav_contact) {
            intent = new Intent(this, ContactActivity.class);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(intent == null) return true;
        startActivity(intent); //start selected activity
        return true;
    }

    public Task getDisplayedTask() {
        return displayedTask;
    }

    public void setDisplayedTask(Task displayedTask) {
        this.displayedTask = displayedTask;
    }
}
