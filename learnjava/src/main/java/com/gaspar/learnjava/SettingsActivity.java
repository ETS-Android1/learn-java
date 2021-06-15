package com.gaspar.learnjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Activity to show app settings.
 */
public class SettingsActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Preference object that can be used by the methods of this activity.
     */
    private SharedPreferences prefs;

    /**
     * Constant for the 'exam notifications enabled' preference.
     */
    private static final String EXAM_NOTIFICATIONS_PREF_NAME = "exam_notification_pref_name";

    /**
     * Constant for the 'app difficulty' preference.
     */
    public static final String DIFFICULTY_PREF_NAME = "difficulty";

    /**
     * Constant for the 'keep screen on' preference.
     */
    private static final String KEEP_AWAKE_PREF_NAME = "keep_awake_pref_name";

    /**
     * Constant for the 'automatically slide unlocked content open' preference.
     */
    public static final String AUTO_SLIDE_OPEN_PREF_NAME = "auto_slide_open_pref_name";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        setUpUI();
    }

    /**
     * Initialized the UI components of the activity, such as the switches and radio buttons,
     * so they are consistent with the current settings.
     */
    private void setUpUI() {
        toolbar = findViewById(R.id.toolbarExam);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        initNotificationSettings(); //show up to date values in the settings screen
        initDifficultySettings();
        initScreenSettings();
    }

    /**
     * Called when a style selector button is clicked.
     */
    public void styleSelected(View styleSelector) {
        styleSelector.startAnimation(AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.tick));
        @ThemeUtils.Themes int newTheme;
        int buttonId = styleSelector.getId();
        if(buttonId == R.id.orangeThemeButton) {
            newTheme = ThemeUtils.Themes.ORANGE;
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
        SwitchMaterial examNotificationSwitch = findViewById(R.id.examNotificationsSwitch);
        examNotificationSwitch.setChecked(prefs.getBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true)); //set exam notification status
        examNotificationSwitch.setOnCheckedChangeListener((compoundButton, checked) ->
                prefs.edit().putBoolean(EXAM_NOTIFICATIONS_PREF_NAME, checked).apply());
    }

    /**
     * Initializes screen related settings such as "keep screen awake".
     */
    private void initScreenSettings() {
        //keep awake functionality
        SwitchMaterial keepAwakeSwitch = findViewById(R.id.keepAwakeSwitch);
        keepAwakeSwitch.setChecked(prefs.getBoolean(KEEP_AWAKE_PREF_NAME, false));
        keepAwakeSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
                prefs.edit().putBoolean(KEEP_AWAKE_PREF_NAME, checked).apply();
                if(checked) { //update settings activity as well
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else { //turn it off
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
        });
        //auto slide open functionality
        SwitchMaterial autoSlideOpenSwitch = findViewById(R.id.autoOpenSwitch);
        autoSlideOpenSwitch.setChecked(prefs.getBoolean(AUTO_SLIDE_OPEN_PREF_NAME, false));
        autoSlideOpenSwitch.setOnCheckedChangeListener((compoundButton, checked) ->
                //save preference
                prefs.edit().putBoolean(AUTO_SLIDE_OPEN_PREF_NAME, checked).apply());
    }

    /**
     * Initializes the difficulty related settings.
     */
    private void initDifficultySettings() {
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
            if (id == R.id.challengingButton) {
                prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.CHALLENGING).apply();
                Exam.setMinimumPassPercentage(80);
            } else if (id == R.id.defaultButton) {
                prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT).apply();
                Exam.setMinimumPassPercentage(60);
            } else if (id == R.id.easyButton) {
                prefs.edit().putString(DIFFICULTY_PREF_NAME, Difficulties.EASY).apply();
                Exam.setMinimumPassPercentage(50);
            }
        });
    }

    /**
     * Called when the reset button is clicked. Asks for confirmation before resetting.
     */
    public void resetButtonOnClick(View resetButton) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
        builder.setMessage(R.string.reset_warning);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            //deletes the contents of the database, and then re-validates it, filling it with default values.
            dialogInterface.dismiss();
            LearnJavaDatabase.DB_EXECUTOR.execute(() -> {
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
        DrawerUtils.handleDrawerOnClick(this, item);
        return true;
    }

    /**
     * Checks if the exam notifications are enabled.
     */
    public static boolean examNotificationsEnabled(@NonNull final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true);
    }

    /**
     * Checks if the keep screen awake function is enabled.
     */
    public static boolean keepAwakeEnabled(@NonNull final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEEP_AWAKE_PREF_NAME, false); //false by default
    }

    /**
     * Checks if the auto slide open preference is enabled.
     */
    public static boolean autoSlideOpenEnabled(@NonNull final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(AUTO_SLIDE_OPEN_PREF_NAME, false); //false by default
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
        final SharedPreferences.Editor editor = prefs.edit();
        if(!prefs.contains(EXAM_NOTIFICATIONS_PREF_NAME)) {
            editor.putBoolean(EXAM_NOTIFICATIONS_PREF_NAME, true); //exam notification is enabled by default
        }
        if(!prefs.contains(DIFFICULTY_PREF_NAME)) {
            editor.putString(DIFFICULTY_PREF_NAME, Difficulties.DEFAULT); //default difficulty
        }
        if(!prefs.contains(KEEP_AWAKE_PREF_NAME)) {
            editor.putBoolean(KEEP_AWAKE_PREF_NAME, false); //screen awake is disabled by default
        }
        if(!prefs.contains(AUTO_SLIDE_OPEN_PREF_NAME)) {
            editor.putBoolean(AUTO_SLIDE_OPEN_PREF_NAME, false); //auto slide open is disabled by default
        }
        editor.apply();
    }

}
