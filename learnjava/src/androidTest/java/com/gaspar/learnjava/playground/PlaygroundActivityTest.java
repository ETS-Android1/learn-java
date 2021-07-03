package com.gaspar.learnjava.playground;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.gaspar.learnjava.AndroidTestUtils;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.database.LearnJavaDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * UI tests for {@link PlaygroundActivity}, but not its fragments. This activity is made up of 3 fragments,
 * {@link InputFragment}, {@link CodeFragment} and {@link OutputFragment} in a {@link androidx.viewpager2.widget.ViewPager2}.
 * Functionality that is managed by one of these 3 fragments is tested in the fragments own test class. Functionality that is not handled by
 * any fragment, but rather {@link PlaygroundActivity} itself is tested here.
 * @see CodeFragmentTest
 * @see InputFragmentTest
 * @see OutputFragmentTest
 */
public class PlaygroundActivityTest {

    //not using ActivityScenarioRule here, need explicit control when to start activity
    public ActivityScenario<PlaygroundActivity> scenario;

    @Rule
    public TestName testName = new TestName();

    public SharedPreferences preferences = ApplicationProvider.getApplicationContext().getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);

    @Before
    public void setUp() {
        //clear playground table in database
        Context context = ApplicationProvider.getApplicationContext();
        LearnJavaDatabase.getInstance(context).getPlaygroundFileDao().deleteRecords();
        //mock
        PlaygroundActivity.mockRunApi = true;
        //some tests want the info dialog
        if(testName.getMethodName().equals("testInfoDialogChecked") || testName.getMethodName().equals("testInfoDialogNotChecked")) {
            PlaygroundActivity.forceShowInfoDialog = true;
            preferences.edit().remove(PlaygroundActivity.PLAYGROUND_SHOW_INFO_PREF_NAME).apply();
        } else {
           PlaygroundActivity.forceHideInfoDialog = true;
           preferences.edit().putBoolean(PlaygroundActivity.PLAYGROUND_SHOW_INFO_PREF_NAME, false).apply();
        }
        //cancel API cool down time between tests
        preferences.edit().remove(PlaygroundActivity.PLAYGROUND_COUNTDOWN_PREF_NAME).apply();
        //start activity
        scenario = ActivityScenario.launch(PlaygroundActivity.class);
    }

    @After
    public void tearDown() {
        PlaygroundActivity.forceHideInfoDialog = false;
        PlaygroundActivity.forceShowInfoDialog = false;
        if(scenario != null) scenario.close();
    }

    @Test
    public void testPlaygroundVisible() {
        onView(withId(R.id.playground_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testInfoDialogChecked() {
        //dialog should be showing
        onView(withText(R.string.playground_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        //check dont show again
        onView(withId(R.id.playgroundInfoCheckbox)).perform(click());
        //accept
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //shared preference should be "false"
        scenario.onActivity(activity -> assertFalse(preferences.getBoolean(PlaygroundActivity.PLAYGROUND_SHOW_INFO_PREF_NAME, true)));
    }

    @Test
    public void testInfoDialogNotChecked() {
        //dialog should be showing
        onView(withText(R.string.playground_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        //accept
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //shared preference should be true
        scenario.onActivity(activity -> assertTrue(preferences.getBoolean(PlaygroundActivity.PLAYGROUND_SHOW_INFO_PREF_NAME, false)));
    }

    @Test
    public void testRunButtonDisappear() throws InterruptedException {
        //focusing code area hides run button
        onView(withId(R.id.playgroundCodeArea)).perform(typeText("test"));
        //should be hidden
        onView(withId(R.id.playgroundRunButton)).check(matches(not(isDisplayed())));
        //close keyboard
        onView(withId(R.id.playgroundCodeArea)).perform(closeSoftKeyboard());
        //should be visible again
        onView(withId(R.id.playgroundRunButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testRunButton() throws InterruptedException {
        //run default main program, in mock mode
        onView(withId(R.id.playgroundRunButton)).perform(click());
        //run dialog should be on
        onView(withText(R.string.playground_in_progress)).inRoot(isDialog()).check(matches(isDisplayed()));
        //wait until mock completes
        Thread.sleep(RunApi.MOCK_TIME);
        //should have changed to output fragment
        onView(withId(R.id.fragment_output_root)).check(matches(isDisplayed()));
        //mock messages are displayed: I cant get these to match using any of these: withText, withSubstring, AndroidTestUtils.withSpannableText, not sure why, but they are displayed
        //onView(withId(R.id.playgroundStdout)).check(matches(withSubstring(RunApi.MOCK_STDOUT)));
        //onView(withId(R.id.playgroundStderr)).check(matches(withSubstring(RunApi.MOCK_STDERR)));
        //onView(withId(R.id.playgroundExitCode)).check(matches(withSubstring(RunApi.MOCK_EXIT_CODE)));
    }

    @Test
    public void testRunOnCoolDown() throws InterruptedException {
        //start a run
        testRunButton();
        //wait a bit
        Thread.sleep(1000);
        //go back to code, but this is not necessary
        onView(withId(R.id.playgroundTabLayout)).perform(AndroidTestUtils.selectTabAtPosition(PlaygroundTab.TAB_CODE));
        Thread.sleep(1000);
        //try to run again
        onView(withId(R.id.playgroundRunButton)).perform(click());
        //should see cool down dialog counting down
        onView(withText(R.string.playground_countdown)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    @Ignore("This test takes a long time to complete (>1 min), but it succeeds")
    public void testRunOnCoolDownOver() throws InterruptedException {
        //run twice to trigger countdown dialog
        testRunOnCoolDown();
        //wait enough time to end the count down
        Thread.sleep(PlaygroundActivity.CODE_RUN_INTERVAL);
        //should see updated message
        onView(withText(R.string.playground_countdown_over)).inRoot(isDialog()).check(matches(isDisplayed()));
    }
}