package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.FillCourseActivityTask;
import com.gaspar.learnjava.asynctask.ShowCongratulationTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.DrawerUtils;
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
    private static List<Course> PARSED_COURSES = new ArrayList<>();

    //these constants are used to determine what kind of activity finished.
    public static final int CHAPTER_REQUEST_CODE = 23;
    public static final int TASK_REQUEST_CODE = 24;
    public static final int EXAM_REQUEST_CODE = 25;

    /**
     * View that displays ads in this activity.
     */
    private AdView adView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.courses);
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
        showCongratulationPrompt();
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
     * Updates the {@link #updateViews}, so the selectors are updated to reflect possible changes. Exam or course unlocked,
     * for example.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) return;
        if(data.getExtras() == null) return;
        switch (requestCode) {
            case CHAPTER_REQUEST_CODE: //a chapter activity just finished
                if(updateViews.length == 0) return; //should not happen
                ImageView icon = updateViews[0].findViewById(R.id.chapterStatusIcon);
                Chapter chapter = (Chapter) data.getExtras().getSerializable(Chapter.CHAPTER_PREFERENCE_STRING);
                if(chapter == null) return;
                chapter.queryAndDisplayStatus(icon, CoursesActivity.this);
                if(!data.getExtras().containsKey(Exam.EXAM_PREFERENCE_STRING)) return; //no exam, stop updating
                Exam exam = (Exam)data.getExtras().getSerializable(Exam.EXAM_PREFERENCE_STRING);
                if(updateViews.length > 1 && exam != null) { // exam view and exam object found, update them
                    exam.queryAndDisplayStatus(updateViews[1], CoursesActivity.this);
                }
                break;
            case TASK_REQUEST_CODE: //task activity finished
                if(updateViews.length == 0) return; //should not happen
                ImageView taskIcon = updateViews[0].findViewById(R.id.taskStatusIcon);
                Task task = (Task)data.getExtras().getSerializable(Task.TASK_PREFERENCE_STRING);
                if(task == null) return;
                task.queryAndDisplayStatus(taskIcon, CoursesActivity.this);
                break;
            case EXAM_REQUEST_CODE:
                if(updateViews.length == 0) return; //should not happen
                Exam examData = (Exam)data.getExtras().getSerializable(Exam.EXAM_PREFERENCE_STRING);
                if(examData == null) return;
                examData.queryAndDisplayStatus(updateViews[0], CoursesActivity.this); //update view will be the exam view
                updateNextCourseDisplay(examData.getId()); //if there is a next course, and it's unlocked, update it
                break;
        }
    }

    //helper method, called when an exam finishes and the user returns to the course activity
    private void updateNextCourseDisplay(int currentExamId) {
        int i;
        for(i=0; i<PARSED_COURSES.size(); i++) {
            if(PARSED_COURSES.get(i).getExam().getId() == currentExamId) break;
        }
        if(i < PARSED_COURSES.size() - 1) { //there is a course after this one
            Course nextCourse = PARSED_COURSES.get(i+1);
            ListView listView = findViewById(R.id.courseSelectors);
            View nextCourseView = listView.getChildAt((i+1) - listView.getFirstVisiblePosition());
            if(nextCourseView != null) { //next course view is null when it isn't visible, but then it gets updated when it appears
                nextCourse.queryAndDisplayStatus(nextCourseView.findViewById(R.id.statusIconView), this);
            }
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
            if(!LearnJavaActivity.DEBUG) { //only some shaking happens on locked, except in debug
                if(c.getStatus() == Status.LOCKED || c.getStatus()==Status.NOT_QUERIED) {
                    view.findViewById(R.id.statusIconView).
                            startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake));
                    return;
                }
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
        DrawerUtils.handleDrawerOnClick(this, item);
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
     * <p>
     *     See {@link com.gaspar.learnjava.asynctask.ShowCongratulationTask} for implementation.
     * </p>
     */
    private void showCongratulationPrompt() {
        new ShowCongratulationTask().execute(this);
    }

    public static List<Course> getParsedCourses() {
        return PARSED_COURSES;
    }

    public static void setParsedCourses(List<Course> parsedCourses) {
        PARSED_COURSES = parsedCourses;
    }

    public static boolean coursesNotParsed() {
        return PARSED_COURSES.isEmpty();
    }

    /**
     * The views that will be updated when the ongoing activity finished.
     *
     * @see UpdatableActivity
     */
    private View[] updateViews;

    @Override
    public void setUpdateViews(View... updateViews) {
        this.updateViews = updateViews;
    }

}
