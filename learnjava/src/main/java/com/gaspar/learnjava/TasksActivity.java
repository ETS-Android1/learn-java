package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.adapters.TaskAdapter;
import com.gaspar.learnjava.asynctask.FillTasksActivityTask;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

/**
 * Activity where all tasks are displayed.
 */
public class TasksActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, UpdatableActivity {

    public boolean successfulLoad;

    /**
     * View that is updated when an activity started from this one finishes.
     */
    private View updateView;

    /**
     * View that displays ads in this activity.
     */
    private AdView adView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_tasks);
        setUpUI();
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

    private void setUpUI() {
        loadTasks(); //load tasks

        toolbar = findViewById(R.id.toolbarTasks);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_tasks_root);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if(LearnJavaAds.LOAD_ADS) {
            int adId = LearnJavaAds.DEBUG_ADS ? R.string.ad_unit_id_banner_test : R.string.ad_unit_id_banner_tasks;
            adView = LearnJavaAds.loadBannerAd(adId, findViewById(R.id.adContainer));
        } else {
            findViewById(R.id.adContainer).setVisibility(View.GONE);
        }
    }

    /**
     * Loads the tasks sorted by courses and shows them in the list view. When this process finishes
     * the loading indicator is hidden and the list view is shown.
     */
    private void loadTasks() {
        new FillTasksActivityTask().execute(this);
        ListView tasks = findViewById(R.id.taskSelectors);
        tasks.setOnItemClickListener((adapterView, view, position, l) -> {
            Course course = CoursesActivity.getParsedCourses().get(position);
            if(course.getStatus() == Status.NOT_QUERIED || course.getStatus() == Status.LOCKED) {
                //inform about the course
                TaskAdapter.buildDialog(this).show();
                return;
            }
            View tasksOfCourseView = view.findViewById(R.id.tasksOfCourse);
            if(tasksOfCourseView.getVisibility() == View.GONE) {
                AnimationUtils.showView(tasksOfCourseView);
            } else { //visible
                AnimationUtils.hideView(tasksOfCourseView, adapterView);
            }
        });
    }

    /**
     * Updates the status icon of the task that's activity was finished.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK || data == null) return;
        if(requestCode == CoursesActivity.TASK_REQUEST_CODE) { //only care about task results
            Task task = (Task)data.getSerializableExtra(Task.TASK_PREFERENCE_STRING);
            if(task == null) return;
            task.queryAndDisplayStatus(updateView.findViewById(R.id.taskStatusIcon), TasksActivity.this);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_tasks_root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_tasks_root);
        return true;
    }

    @Override
    public void setUpdateViews(View... updateViews) {
        this.updateView = updateViews[0];
    }
}
