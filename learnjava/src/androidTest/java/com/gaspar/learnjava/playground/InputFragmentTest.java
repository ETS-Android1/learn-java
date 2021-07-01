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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.gaspar.learnjava.AndroidTestUtils.selectTabAtPosition;
import static com.gaspar.learnjava.AndroidTestUtils.withFontSize;
import static junit.framework.Assert.assertEquals;

/**
 * The {@link InputFragment} is part of {@link PlaygroundActivity}, responsible for getting the program
 * input. Functionality that is managed by this fragment is tested here.
 * @see PlaygroundActivityTest
 */
public class InputFragmentTest {

    @Rule
    public ActivityScenarioRule<PlaygroundActivity> rule = new ActivityScenarioRule<>(PlaygroundActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        //dont show info dialog
        PlaygroundActivity.forceHideInfoDialog = true;
        //each test starts with navigating to the correct fragment
        onView(withId(R.id.playgroundTabLayout)).perform(selectTabAtPosition(PlaygroundTab.TAB_INPUT));
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        PlaygroundActivity.forceHideInfoDialog = false;
    }

    @Test
    public void testInputFragmentDisplayed() {
        onView(withId(R.id.fragment_input_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testInputInfoDisplayed() {
        onView(withText(R.string.playground_input_info)).check(matches(isDisplayed()));
    }

    @Test
    public void testInputHintDisplayed() {
        onView(withId(R.id.playgroundInputArea)).check(matches(withHint(R.string.playground_input_hint)));
    }

    private final String testInput = "test input";

    //types random text into the input area then hides soft keyboard
    private void typeTestInput() {
        onView(withId(R.id.playgroundInputArea)).perform(typeText(testInput), closeSoftKeyboard());
    }

    @Test
    public void testInputEntered() throws InterruptedException {
        typeTestInput();
        //this is sent to the playground activity after the user has stopped typing for 1 sec
        Thread.sleep(1100);
        rule.getScenario().onActivity(activity -> {
            assertEquals(testInput, activity.getInput());
        });
    }

    @Test
    public void testInputZoomIn() {
        typeTestInput();
        //should have default text size
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.playgroundInputArea)).check(matches(withFontSize(defSize)));
        //press zoom button
        onView(withId(R.id.inputZoomInButton)).perform(click());
        //text size increased
        onView(withId(R.id.playgroundInputArea)).check(matches(withFontSize(defSize + CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testInputZoomOut() {
        typeTestInput();
        //should have default text size
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.playgroundInputArea)).check(matches(withFontSize(defSize)));
        //press zoom button
        onView(withId(R.id.inputZoomOutButton)).perform(click());
        //text size increased
        onView(withId(R.id.playgroundInputArea)).check(matches(withFontSize(defSize - CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testInputDelete() {
        typeTestInput();
        //press delete
        onView(withId(R.id.inputDeleteButton)).perform(click());
        //snackbar, check and swipe away
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.playground_input_deleted)));
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //should have no text
        onView(withId(R.id.playgroundInputArea)).check(matches(withText("")));
    }
}