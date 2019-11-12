package com.gaspar.learnjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;

/**
 * Activity to show app settings.
 */
public class SettingsActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.settings);
        setUpUI();
    }

    private void setUpUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        initNotificationSettings();
        initDifficultySettings();
    }

    /**
     * Constant for the 'exam notifications enabled' preference.
     */
    private static final String EXAM_NOTIFICATIONS_PREF_NAME = "exam_notification_pref_name";

    /**
     * Constant for the 'app difficulty' preference.
     */
    public static final String DIFFICULTY_PREF_NAME = "difficulty";

    /**
     * Called when a style selector button is clicked.
     */
    public void styleSelected(View styleSelector) {
        styleSelector.startAnimation(AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.tick));
        @ThemeUtils.Themes int newTheme;
        int buttonId = styleSelector.getId();
        if(buttonId == R.id.orangeThemeButton) {
            newTheme = ThemeUtils.Themes.ORANGE;
        } else if(buttonId == R.id.blueThemeButton) {
            newTheme = ThemeUtils.Themes.BLUE;
        } else if(buttonId == R.id.darkThemeButton) {
            newTheme = ThemeUtils.Themes.DARK;
        } else {
            throw new RuntimeException("Unknown theme selector button!");
        }
        ThemeUtils.updateSelectedTheme(SettingsActivity.this, newTheme); //update
        setTheme(ThemeUtils.getTheme()); //restyle settings activity
        recreate();
    }

    /**
     * Possible difficulties.
     */
    @StringDef
    public @interface Difficulties {
        String CHALLENGING = "challenging";
        String DEFAULT = "default";
        String EASY = "easy";
    }

    /**
     * Initializes the notification related settings.
     */
    private void initNotificationSettings() {
        final SharedPreferences prefs = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Switch examNotificationSwitch = findViewById(R.id.examNotificationsSwitch);
        examNotificationSwitch.setChecked(prefs.getBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true)); //set exam notification status
        examNotificationSwitch.setOnCheckedChangeListener((compoundButton, checked) ->
                prefs.edit().putBoolean(EXAM_NOTIFICATIONS_PREF_NAME, checked).apply());
    }

    /**
     * Initializes the difficulty related settings.
     */
    private void initDifficultySettings() {
        final SharedPreferences prefs = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        RadioGroup group = findViewById(R.id.difficultyButtons);
        @Difficulties String currentDifficulty = prefs.getString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT);
        switch (currentDifficulty) { //set status according to preferences
            case Difficulties.CHALLENGING:
                group.check(R.id.challengingButton);
                break;
            case Difficulties.DEFAULT:
                group.check(R.id.defaultButton);
                break;
            case Difficulties.EASY:
                group.check(R.id.easyButton);
                break;
        }
        group.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                case R.id.challengingButton:
                    prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.CHALLENGING).apply();
                    Exam.setMinimumPassPercentage(80);
                    break;
                case R.id.defaultButton:
                    prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT).apply();
                    Exam.setMinimumPassPercentage(60);
                    break;
                case R.id.easyButton:
                    prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.EASY).apply();
                    Exam.setMinimumPassPercentage(50);
                    break;
            }
        });
    }

    /**
     * Called when the reset button is clicked. Asks for confirmation before resetting.
     */
    public void resetButtonOnClick(View resetButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage(R.string.reset_warning);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            //deletes the contents of the database, and then re-validates it, filling it with default values.
            dialogInterface.dismiss();
            Executors.newSingleThreadExecutor().execute(() -> {
                LearnJavaDatabase.resetDatabase(SettingsActivity.this);
                LearnJavaDatabase.validateDatabase(SettingsActivity.this);
            });
            Snackbar.make(resetButton, R.string.reset_successful, Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
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
        if (id == R.id.nav_tasks) {
            intent = new Intent(this, TasksActivity.class);
        } else if (id == R.id.nav_exams) {
            intent = new Intent(this, ExamsActivity.class);
        } else if (id == R.id.nav_guide) {
            intent = new Intent(this, GuideActivity.class);
        } else if (id == R.id.nav_starter_screen) {
            intent = new Intent(this, LearnJavaActivity.class);
        } else if (id == R.id.nav_courses) {
            intent = new Intent(this, CoursesActivity.class);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (intent == null) return true;
        startActivity(intent); //start selected activity
        return true;
    }

    /**
     * Checks if the exam notifications are enabled.
     */
    public static boolean examNotificationsEnabled(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true);
    }

    /**
     * Called when loading an exam, checks if the challenging difficulty is selected.
     */
    public static boolean isOnChallengingDifficulty(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT).equals(Difficulties.CHALLENGING);
    }

    /**
     * Checks if all the settings preferences are present. If not, adds them with the default value. This is called when the
     * application is launched.
     */
    public static void initSettings(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!prefs.contains(EXAM_NOTIFICATIONS_PREF_NAME)) {
            prefs.edit().putBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true).apply(); //enabled by default
        }
        if(!prefs.contains(DIFFICULTY_PREF_NAME)) {
            prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT).apply(); //default difficulty
        }
    }

}
