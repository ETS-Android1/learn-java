package com.gaspar.learnjava.playground;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.components.CodeComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.gaspar.learnjava.AndroidTestUtils.selectTabAtPosition;
import static com.gaspar.learnjava.AndroidTestUtils.withFontSize;

/**
 * The {@link OutputFragment} is part of {@link PlaygroundActivity}, responsible for displaying the output of
 * the program. Functionality that is managed by this fragment is tested here.
 * @see PlaygroundActivityTest
 */
public class OutputFragmentTest {

    @Rule
    public ActivityScenarioRule<PlaygroundActivity> rule = new ActivityScenarioRule<>(PlaygroundActivity.class);

    //default font size
    public int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.small_text_size));

    @Before
    public void setUp() throws InterruptedException {
        //dont show info dialog
        PlaygroundActivity.forceHideInfoDialog = true;
        //each test starts with navigating to the correct fragment
        onView(withId(R.id.playgroundTabLayout)).perform(selectTabAtPosition(PlaygroundTab.TAB_OUTPUT));
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        PlaygroundActivity.forceHideInfoDialog = false;
    }

    @Test
    public void testOutputFragmentDisplayed() {
        onView(withId(R.id.fragment_output_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testStdoutHint() {
        onView(withId(R.id.playgroundStdout)).check(matches(withHint(R.string.playground_stdout_hint)));
    }

    @Test
    public void testStdoutZoomIn() {
        onView(withId(R.id.playgroundStdout)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.stdoutZoomIn)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundStdout)).check(matches(withFontSize(defSize + CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testStdoutZoomOut() {
        onView(withId(R.id.playgroundStdout)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.stdoutZoomOut)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundStdout)).check(matches(withFontSize(defSize - CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testStderrHint() {
        onView(withId(R.id.playgroundStderr)).check(matches(withHint(R.string.playground_stderr_hint)));
    }

    @Test
    public void testStderrZoomIn() {
        onView(withId(R.id.playgroundStderr)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.stderrZoomIn)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundStderr)).check(matches(withFontSize(defSize + CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testStderrZoomOut() {
        onView(withId(R.id.playgroundStderr)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.stderrZoomOut)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundStderr)).check(matches(withFontSize(defSize - CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testExitCodeHint() {
        onView(withId(R.id.playgroundExitCode)).check(matches(withHint(R.string.playground_exit_code_hint)));
    }

    @Test
    public void testExitCodeZoomIn() {
        onView(withId(R.id.playgroundExitCode)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.exitCodeZoomIn)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundExitCode)).check(matches(withFontSize(defSize + CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testExitCodeZoomOut() {
        onView(withId(R.id.playgroundExitCode)).check(matches(withFontSize(defSize)));
        onView(withId(R.id.exitCodeZoomOut)).perform(scrollTo(), click());
        onView(withId(R.id.playgroundExitCode)).check(matches(withFontSize(defSize - CodeComponent.ZOOM_SIZE_CHANGE)));
    }

}