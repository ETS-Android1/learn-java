package com.gaspar.learnjava;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.InitStarterViewTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executors;

public class LearnJavaActivity extends ThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Debug constant. If this is set to true the application is in debug mode. This makes all courses and tasks openable,
     * all exams can be started at any time.
     */
    public static final boolean DEBUG = true;

    /**
     * Constant that determines if ads will or won't be test(debug) ads. This only has a real effect if {@value LOAD_ADS}
     * is true.
     */
    public static final boolean DEBUG_ADS = true;

    /**
     * A constant that determines if ads will be loaded or not.
     */
    public static final boolean LOAD_ADS = false;

    /**
     * The name of the application's shared preferences.
     */
    public static final String APP_PREFERENCES_NAME = "learn_java_prefs";

    /**
     * The string used for the active chapter preference.
     */
    public static final String ACTIVE_CHAPTER_ID_PREFERENCE = "active_chapter_pref";

    /**
     * The chapter currently started by the user. If no chapter has been started this is defaulted
     * to the first chapter of the first course.
     */
    private volatile Chapter startedChapter;

    /**
     * Used at loading time.
     */
    public volatile boolean successfulLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Executors.newSingleThreadExecutor().execute(() -> { //initialize the database related variables
            CourseStatus.setCourseCount(LearnJavaDatabase.getInstance(this).getCourseDao().countCourses(), this);
            LearnJavaDatabase.validateDatabase(this); //check/add all elements
            createNotificationChannel(); //initialize the notification channel
            SettingsActivity.initSettings(this); //initialize settings
            MobileAds.initialize(this, getString(R.string.admob_app_id)); //initialize admob
            ThemeUtils.initSelectedTheme(this); //initialize themes
        });
        setContentView(R.layout.learn_java);
        setUpUI(); //init toolbar and drawer here
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStartContinueComponent();
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
        showStartContinueComponent(); //set up the component that prompts start/continue
    }

    private void showStartContinueComponent() {
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean started = preferences.contains(ACTIVE_CHAPTER_ID_PREFERENCE);
        int startedChapterId = preferences.getInt(ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        new InitStarterViewTask(started ? startedChapterId : -1, started).execute(this);
    }

    /**
     * Settings button (in the toolbar) click handler.
     */
    public void settingsOnClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * When the not started view is showing and the guide button is clicked.
     */
    public void guideButtonOnClick(View view) {
        Intent intent = new Intent(LearnJavaActivity.this, GuideActivity.class);
        startActivity(intent);
    }

    /**
     * When the started view is visible and the continue learning button is clicked.
     */
    public void continueLearningOnClick(View view) {
        if(startedChapter == null) return;
        Chapter.startChapterActivity(this, startedChapter, null); //this is not an updatable activity, so view is null
    }

    /**
     * Notification channel id.
     */
    public static final String CHANNEL_ID = String.valueOf(19970828);

    /**
     * Creates the notification channel for the application.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager == null) return;
            notificationManager.createNotificationChannel(channel);
        }
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
        if(id == R.id.nav_courses) {
            intent = new Intent(this, CoursesActivity.class);
        } else if(id == R.id.nav_tasks) {
            intent = new Intent(this, TasksActivity.class);
        } else if(id == R.id.nav_exams) {
            intent = new Intent(this, ExamsActivity.class);
        } else if(id == R.id.nav_guide) {
            intent = new Intent(this, GuideActivity.class);
        } else if(id == R.id.nav_contact) {
            intent = new Intent(this, ContactActivity.class);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(intent == null) return true;
        startActivity(intent); //start selected activity
        return true;
    }

    public Chapter getStartedChapter() {
        return startedChapter;
    }

    public void setStartedChapter(Chapter startedChapter) {
        this.startedChapter = startedChapter;
    }
}
