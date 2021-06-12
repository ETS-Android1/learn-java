package com.gaspar.learnjava;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.asynctask.InitStarterViewTask;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.parsers.RawParser;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.OnSwipeTouchListener;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Random;

/**
 * Starting activity of the app. Prompts the user to start or continue the curriculum.
 */
public class LearnJavaActivity extends ThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Debug constant. If this is set to true the application is in debug mode. This makes all courses and tasks openable,
     * all exams can be started at any time.
     * <p>
     * DO NOT SET THIS HERE, it's set automatically from the build variant.
     */
    public static boolean DEBUG;

    /**
     * The name of the application's shared preferences.
     */
    public static final String APP_PREFERENCES_NAME = "learn_java_prefs";

    /**
     * The constant used to find the active (last started) chapter ID in the preferences.
     */
    public static final String ACTIVE_CHAPTER_ID_PREFERENCE = "active_chapter_pref";

    /**
     * The chapter currently started by the user. If no chapter has been started this is defaulted
     * to the first chapter of the first course.
     */
    private volatile Chapter startedChapter;

    /**
     * Used at loading time to store if the loading was successful or not.
     */
    public volatile boolean successfulLoad;

    /**
     * Stores if the activity is starting, or just resuming.
     */
    private boolean onCreate;

    /**
     * Called on application startup. Initializes preferences, database, and home screen. Most of the
     * initialization happens in the background while a loading indicator is shown.
     * @param savedInstanceState Unused.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize preferences needed in this activity
        GuideActivity.initializeGuideReadPreference(this);
        //check if it's debug or release, and set flag accordingly
        DEBUG = getResources().getBoolean(R.bool.is_debug);
        setContentView(R.layout.learn_java);
        LearnJavaDatabase.DB_EXECUTOR.execute(() -> { //initialize necessary variables, database
            try { //parse course objects
                CoursesActivity.setParsedCourses(CourseParser.getInstance().parseCourses(this));
            } catch (Exception e) {
                LogUtils.logError("Failed to parse courses!");
            }
            CourseStatus.initCourseCount(CoursesActivity.getParsedCourses().size(), this); //pass in new course size for check
            LearnJavaDatabase.validateDatabase(this); //check/add all elements
            createNotificationChannel(); //initialize the notification channel
            SettingsActivity.initSettings(this); //initialize settings
            LearnJavaAds.initAdConstants(this); //initialize ad variables
            if(LearnJavaAds.LOAD_ADS) {
                MobileAds.initialize(this, result -> {}); //initialize admob
            }
            ClipSyncActivity.initClipSync(this, findViewById(R.id.learnJavaMainView)); //initialize clip sync
            showStartContinueComponent(); //initialize the main component, curriculum is guaranteed to be loaded here
            runOnUiThread(() -> {
                //once everything is loaded, check for system dark theme
                ThemeUtils.showSystemDarkModeDialogIfNeeded(LearnJavaActivity.this);
            });
        });
        setUpUI(); //init toolbar and drawer here
        onCreate = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!onCreate) { //if we are returning to this activity, update the component
            showStartContinueComponent();
        } else {
            //we are launching this activity. component will be updated when all initialization completed
            onCreate = false;
        }
        RelativeLayout backgroundLayout = findViewById(R.id.backgroundImagesLayout); //show another background image
        backgroundLayout.getChildAt(visibleCodeIndex).setVisibility(View.GONE);
        visibleCodeIndex = new Random().nextInt(backgroundLayout.getChildCount()); //start with random image
        backgroundLayout.getChildAt(visibleCodeIndex).setVisibility(View.VISIBLE);
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
        createSwipeBackground();
        findViewById(R.id.learnJavaMainView).bringToFront();
    }

    private int visibleCodeIndex; //helper for the swipe background
    private static final String SHOW_SWIPE = "show_swipe";

    /**
     * Creates and fills the layout for background code images. User can swipe between backgrounds.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void createSwipeBackground() {
        RelativeLayout backgroundLayout = findViewById(R.id.backgroundImagesLayout); //set up the swipe background
        List<Drawable> drawables = RawParser.parseCodeImages(this);
        for(Drawable codeImage: drawables) { //create and add all image views
            ImageView iw = new ImageView(this);
            iw.setVisibility(View.GONE);
            iw.setBackground(codeImage);
            iw.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            backgroundLayout.addView(iw);
        }
        backgroundLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() { //on left animate in next image
                View slideIn, slideOut = backgroundLayout.getChildAt(visibleCodeIndex);
                if(visibleCodeIndex == backgroundLayout.getChildCount() - 1) { //last image is visible
                    slideIn = backgroundLayout.getChildAt(0);
                    visibleCodeIndex = 0;
                } else { //not the last image is visible
                    slideIn = backgroundLayout.getChildAt(visibleCodeIndex + 1);
                    visibleCodeIndex++;
                }
                AnimationUtils.slideOutHorizontal(slideOut, AnimationUtils.Direction.LEFT, LearnJavaActivity.this);
                AnimationUtils.slideInHorizontal(slideIn, AnimationUtils.Direction.RIGHT, LearnJavaActivity.this);
            }
            @Override
            public void onSwipeRight() { //on right animate in previous image
                View slideIn, slideOut = backgroundLayout.getChildAt(visibleCodeIndex);
                if(visibleCodeIndex == 0) { //first image is visible
                    slideIn = backgroundLayout.getChildAt(backgroundLayout.getChildCount() - 1);
                    visibleCodeIndex = backgroundLayout.getChildCount() - 1;
                } else { //not the first image is visible
                    slideIn = backgroundLayout.getChildAt(visibleCodeIndex - 1);
                    visibleCodeIndex--;
                }
                AnimationUtils.slideOutHorizontal(slideOut, AnimationUtils.Direction.RIGHT, LearnJavaActivity.this);
                AnimationUtils.slideInHorizontal(slideIn, AnimationUtils.Direction.LEFT, LearnJavaActivity.this);
            }
        });
        visibleCodeIndex = new Random().nextInt(backgroundLayout.getChildCount()); //start with random image
        backgroundLayout.getChildAt(visibleCodeIndex).setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
       if(!prefs.contains(SHOW_SWIPE)) { //on first start show swiping function
           prefs.edit().putBoolean(SHOW_SWIPE, false).apply();
           animateTouchIcon();
        }
    }

    /**
     * Moves the swipe icon first right, then left.
     */
    private void animateTouchIcon() {
        ImageView touchIcon = findViewById(R.id.touchIcon);
        Animation right = new TranslateAnimation(0, -750,0, 0);
        right.setDuration(2000);
        right.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                touchIcon.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                touchIcon.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        touchIcon.startAnimation(right);
    }

    /**
     * Shows the prompt which points to the drawer, after the user has completed
     * all chapters of a course and he cant progress anymore from the starter screen.
     */
    public void showAndAnimateOpenDrawerPrompt() {
        final View prompt = findViewById(R.id.showMenuView);
        final ObjectAnimator pulsator = ObjectAnimator.ofPropertyValuesHolder(
                prompt,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)).
                setDuration(500);
        pulsator.setRepeatCount(ValueAnimator.INFINITE);
        pulsator.setRepeatMode(ObjectAnimator.REVERSE);
        pulsator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                prompt.setVisibility(View.VISIBLE);
                prompt.bringToFront();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                prompt.setVisibility(View.GONE);
            }
        });
        pulsator.start();
    }

    @Override //cancel ongoing animation here
    protected void onPause() {
        super.onPause();
        final View prompt = findViewById(R.id.showMenuView);
        prompt.clearAnimation();
        //show loading indicator again, in case the last opened chapter changes.
        View startedView = findViewById(R.id.startedView);
        View notStartedView =  findViewById(R.id.notStartedView);
        View loadingView = findViewById(R.id.loadingView);
        startedView.setVisibility(View.GONE);
        notStartedView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    /**
     * Finds which chapter was last opened from the app preferences. If no chapter was found
     * than the "get started" screen is shown.
     * This can be called from both background and UI thread. If the activity is launched, then it
     * will be called from background, after the curriculum is initialized. If the activity is just
     * resumed, then it will be called from the main thread.
     */
    private void showStartContinueComponent() {
        //retrieve the id from preferences
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean started = preferences.contains(ACTIVE_CHAPTER_ID_PREFERENCE);
        int startedChapterId = preferences.getInt(ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        if(startedChapterId == -1) started = false;
        //start async task to find the name of this chapter (this will display the result)
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
        //offer the guide if needed
        Runnable runIfNoGuide = () -> {
            //this is not an updatable activity, so view is null. exam and extra exam view are also null
            Chapter.startChapterActivity(this, startedChapter, null, null, null);
        };
        GuideActivity.displayGuideNotReadDialogIfNeeded(this, runIfNoGuide);
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
        DrawerUtils.handleDrawerOnClick(this, item);
        return true;
    }

    public Chapter getStartedChapter() {
        return startedChapter;
    }

    public void setStartedChapter(Chapter startedChapter) {
        this.startedChapter = startedChapter;
    }
}
