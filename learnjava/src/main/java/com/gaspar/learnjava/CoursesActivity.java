package com.gaspar.learnjava;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillCourseActivityTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays all courses. They can be opened to display the chapters.
 */
public class CoursesActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener
            , UpdatableActivity {

    /**
     * Used by background threads to indicate if the activity has loaded successfully.
     */
    public volatile boolean successfulLoad;

    /**
     * A list of the course objects parsed by the app.
     */
    private static final List<Course> PARSED_COURSES = new ArrayList<>();

    //these constants are used to determine what kind of activity finished.
    public static final int CHAPTER_REQUEST_CODE = 23;
    public static final int TASK_REQUEST_CODE = 24;
    public static final int EXAM_REQUEST_CODE = 25;

    /**
     * Holds the reference to the view that will be updated when the chapter, task, etc, ...
     * activity finishes.
     */
    private View updateView;

    /**
     * View that displays ads in this activity.
     */
    private AdView adView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.courses);
        setUpUI();
        showCongratulationPrompt();
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
        new FillCourseActivityTask().execute(this); //fill list view.
        addCourseOnClickListeners();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

       adView = LearnJavaAds.loadBannerAd(R.string.ad_unit_id_banner_courses, findViewById(R.id.adContainer));
    }

    /**
     * Updates the {@link #updateView}, so the selectors are updated to reflect possible changes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) return;
        if(data.getExtras() == null) return;
        switch (requestCode) {
            case CHAPTER_REQUEST_CODE: //a chapter activity just finished
                ImageView icon = updateView.findViewById(R.id.chapterStatusIcon);
                Chapter chapter = (Chapter) data.getExtras().getSerializable(Chapter.CHAPTER_PREFERENCE_STRING);
                if(chapter == null) return;
                chapter.queryAndDisplayStatus(icon, CoursesActivity.this);
                break;
            case TASK_REQUEST_CODE: //task activity finished
                ImageView taskIcon = updateView.findViewById(R.id.taskStatusIcon);
                Task task = (Task)data.getExtras().getSerializable(Task.TASK_PREFERENCE_STRING);
                if(task == null) return;
                task.queryAndDisplayStatus(taskIcon, CoursesActivity.this);
                break;
            case EXAM_REQUEST_CODE:
                Exam exam = (Exam)data.getExtras().getSerializable(Exam.EXAM_PREFERENCE_STRING);
                if(exam == null) return;
                //update view will be the exam view
                exam.queryAndDisplayStatus(updateView, CoursesActivity.this);
                break;
        }
    }

    /**
     * Adds a listener to each list view element. The listener hides or shows the chapter/task/exam
     * views on click, if the course is unlocked or completed.
     */
    private void addCourseOnClickListeners() {
        ListView courseViews = findViewById(R.id.courseSelectors);
        courseViews.setOnItemClickListener((adapterView, view, position, l) -> {
            Course c = PARSED_COURSES.get(position);
            if(!LearnJavaActivity.DEBUG) { //nothing happens on locked, except in debug
                if(c.getStatus() == Status.LOCKED || c.getStatus()==Status.NOT_QUERIED) return;
            }
            LinearLayout slideInView = view.findViewById(R.id.slideInView);
            if(slideInView.getVisibility() == View.GONE) {
                AnimationUtils.slideIn(slideInView);
            } else { //visible
                AnimationUtils.slideOut(slideInView);
            }
        });
    }

    /**
     * Shows the user which component to tap for details by shaking it a bit. This is always
     * the first course displayer in the list view
     */
    public void showTapPrompt(View tapView) {
        ListView courseSelectors = findViewById(R.id.courseSelectors);
        if(courseSelectors.getChildCount() == 0) return;
        View shakeThis = courseSelectors.getChildAt(0).findViewById(R.id.courseNameBar);

        shakeThis.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                CoursesActivity.this, R.anim.shake));
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

    public static final String CONGRATULATION_PROMPT = "congratulation_prompt";

    /**
     * Shows a dialog that congratulates the user upon completing all exams. By default, it comes on every
     * time the courses activity opens and the user finished all exams, but can be turned off.
     * <p>
     *     The {@link CourseStatus#getCourseCount()} method handles the reactivation of this dialog, when a new
     *     course has been added.
     * </p>
     */
    private void showCongratulationPrompt() {
        SharedPreferences prefs = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        List<Integer> statuses = LearnJavaDatabase.getInstance(this).getExamDao().getAllExamStatus();
        int counter = 0;
        for(int status: statuses) {
            if (status == Status.COMPLETED) counter++;
        }
        if(counter == statuses.size()) { //all exams completed
            AlertDialog.Builder builder = new AlertDialog.Builder(ThemeUtils.createDialogWrapper(this));
            View congratulationsView = View.inflate(CoursesActivity.this, R.layout.congratulation_prompt, null);
            builder.setView(congratulationsView);

            AlertDialog dialog = builder.create();
            congratulationsView.findViewById(R.id.congratulationsOkButton).setOnClickListener(v -> {
                CheckBox checkBox = congratulationsView.findViewById(R.id.congratulationsCheckBox);
                prefs.edit().putBoolean(CONGRATULATION_PROMPT, checkBox.isChecked()).apply(); //update show policy
                dialog.dismiss(); //close dialog
            });
            dialog.show();
        }
    }

    public static List<Course> getParsedCourses() {
        return PARSED_COURSES;
    }

    public static boolean coursesNotParsed() {
        return PARSED_COURSES.isEmpty();
    }

    @Override
    public void setUpdateView(View updateView) {
        this.updateView = updateView;
    }
}
