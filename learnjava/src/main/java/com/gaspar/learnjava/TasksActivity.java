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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillTasksActivityTask;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;
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
        setContentView(R.layout.tasks);
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
        loadTasks(); //load tasks

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        adView = LearnJavaAds.loadBannerAd(R.string.ad_unit_id_banner_tasks, findViewById(R.id.adContainer));
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
            if(course.getStatus() == Status.NOT_QUERIED || course.getStatus() == Status.LOCKED) return;
            View tasksOfCourseView = view.findViewById(R.id.tasksOfCourse);
            if(tasksOfCourseView.getVisibility() == View.GONE) {
                AnimationUtils.slideIn(tasksOfCourseView);
            } else { //visible
                AnimationUtils.slideOut(tasksOfCourseView);
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
        if(id == R.id.nav_exams) {
            intent = new Intent(this, ExamsActivity.class);
        } else if(id == R.id.nav_guide) {
            intent = new Intent(this, GuideActivity.class);
        } else if(id == R.id.nav_courses) {
            intent = new Intent(this, CoursesActivity.class);
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

    @Override
    public void setUpdateView(View updateView) {
        this.updateView = updateView;
    }
}
