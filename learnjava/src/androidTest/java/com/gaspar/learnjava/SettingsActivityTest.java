package com.gaspar.learnjava;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.gaspar.learnjava.utils.ThemeUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * UI tests for {@link SettingsActivity} and the widgets inside it. This activity does not load, 
 * so there is no need to register a custom {@link androidx.test.espresso.IdlingResource}. 
 * <p>
 * Before running these test, {@link SettingsActivity#resetSettings(Context)} must be called. All
 * settings are assumed to be on their default values when running this these tests.
 * <p>
 * The application is assumed to be using the orange theme at the start of each test.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class SettingsActivityTest {

    //used to check values
    public SharedPreferences preferences;

    //starts setting activity before each test
    @Rule
    public ActivityScenarioRule<SettingsActivity> rule = new ActivityScenarioRule<>(SettingsActivity.class);

    @Before
    public void setUp() {
        //reset settings
        SettingsActivity.resetSettings(ApplicationProvider.getApplicationContext());
        //set theme
        ThemeUtils.updateSelectedTheme(ApplicationProvider.getApplicationContext(), ThemeUtils.Themes.ORANGE);
        //prefs
        preferences = ApplicationProvider.getApplicationContext().getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Test
    public void testSettingsVisible() {
        onView(withId(R.id.drawer_layout_settings_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testExamNotificationSwitch() {
        onView(withId(R.id.examNotificationsSwitch)).perform(click());
        //default value was "on", now it should be off
        onView(withId(R.id.examNotificationsSwitch)).check(matches(isNotChecked()));
        assertFalse(preferences.getBoolean(SettingsActivity.EXAM_NOTIFICATIONS_PREF_NAME, true));
    }

    @Test
    public void testDifficultyChallenging() {
        //default difficulty is expected to be selected
        onView(withId(R.id.defaultButton)).check(matches(isChecked()));
        //select new
        onView(withId(R.id.challengingButton)).perform(click());
        assertEquals(SettingsActivity.Difficulties.CHALLENGING,
                preferences.getString(SettingsActivity.DIFFICULTY_PREF_NAME, SettingsActivity.Difficulties.DEFAULT));
    }

    @Test
    public void testDifficultyEasy() {
        //default difficulty is expected to be selected
        onView(withId(R.id.defaultButton)).check(matches(isChecked()));
        //select new
        onView(withId(R.id.easyButton)).perform(click());
        assertEquals(SettingsActivity.Difficulties.EASY,
                preferences.getString(SettingsActivity.DIFFICULTY_PREF_NAME, SettingsActivity.Difficulties.DEFAULT));
    }

    @Test
    public void testResetProgressDenied() {
        onView(withId(R.id.resetButton)).perform(scrollTo());
        //click
        onView(withId(R.id.resetButton)).perform(click());
        //dialog
        onView(withText(R.string.reset_warning)).inRoot(isDialog()).check(matches(isDisplayed()));
        //deny
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //snackbar is not showing
        onView(withText(R.string.reset_successful)).check(doesNotExist());
    }

    @Test
    public void testResetProgressAccepted() {
        onView(withId(R.id.resetButton)).perform(scrollTo());
        //click
        onView(withId(R.id.resetButton)).perform(click());
        //dialog
        onView(withText(R.string.reset_warning)).inRoot(isDialog()).check(matches(isDisplayed()));
        //accept
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //snackbar is showing
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.reset_successful)));
    }

    @Test
    public void testDarkThemeButton() throws InterruptedException {
        //assumed to be in orange theme
        onView(withId(R.id.darkThemeButton)).perform(scrollTo());
        onView(withId(R.id.darkThemeButton)).perform(click());
        //wait for recreate
        Thread.sleep(1000);
        //should be up again with dark mode
        onView(withId(R.id.drawer_layout_settings_root)).check(matches(isDisplayed()));
        assertTrue(ThemeUtils.isDarkTheme());
    }

    @Test
    public void testKeepAwakeSwitch() {
        onView(withId(R.id.keepAwakeSwitch)).perform(scrollTo());
        //assumed to be off, that is default
        onView(withId(R.id.keepAwakeSwitch)).check(matches(isNotChecked()));
        //click
        onView(withId(R.id.keepAwakeSwitch)).perform(click());
        //should be updated in preferences
        assertTrue(preferences.getBoolean(SettingsActivity.KEEP_AWAKE_PREF_NAME, false));
    }

    @Test
    public void testAutoOpenSwitch() {
        onView(withId(R.id.autoOpenSwitch)).perform(scrollTo());
        //assumed to be off, that is default
        onView(withId(R.id.autoOpenSwitch)).check(matches(isNotChecked()));
        //click
        onView(withId(R.id.autoOpenSwitch)).perform(click());
        //should be on in the prefs
        assertTrue(preferences.getBoolean(SettingsActivity.AUTO_SLIDE_OPEN_PREF_NAME, false));
    }
}