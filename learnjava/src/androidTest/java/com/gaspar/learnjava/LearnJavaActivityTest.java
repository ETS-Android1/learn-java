package com.gaspar.learnjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.gaspar.learnjava.parsers.RawParser;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.gaspar.learnjava.AndroidTestUtils.modifySharedPreferenceValue;

/**
 * Instrumentation tests for {@link LearnJavaActivity}, the startup activity.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class LearnJavaActivityTest {

    @Rule //this rule launches this activity before each test
    public ActivityScenarioRule<LearnJavaActivity> rule = new ActivityScenarioRule<>(LearnJavaActivity.class);

    @Rule //this rule stores the test name
    public TestName testName = new TestName();

    //these methods don't want to wait for the loading to complete, for various reasons
    public static final List<String> methodsThatDontNeedWait = Collections.singletonList("testLoadingVisible");

    private IdlingResource loadingIdleResource;

    @Before //before tests, we need to register the loader idling resource -> tests will wait for loading
    public void setUp() {
        //except for: the methods that do not want this
        if(!methodsThatDontNeedWait.contains(testName.getMethodName())) {
            //will wait until loading is complete
            Predicate<ActivityScenarioRule<?>> loadingPredicate = new Predicate<ActivityScenarioRule<?>>() {
                private boolean isIdle;
                @Override
                public boolean test(ActivityScenarioRule<?> rule) {
                    rule.getScenario().onActivity(activity -> {
                        View startedView = activity.findViewById(R.id.startedView);
                        View notStartedView = activity.findViewById(R.id.notStartedView);
                        isIdle = startedView.getVisibility() == View.VISIBLE || notStartedView.getVisibility() == View.VISIBLE;
                    });
                    return isIdle;
                }

            };
            loadingIdleResource = new AndroidTestUtils.LoadingIdlingResource(loadingPredicate, rule);
            IdlingRegistry.getInstance().register(loadingIdleResource);
        }
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(loadingIdleResource);
    }

    @Test
    public void testActivityInView() {
        onView(withId(R.id.drawer_layout_learn_java_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoadingVisible() {
        //is loader shown?
        onView(withId(R.id.loadingView)).check(matches(isDisplayed()));
        //other views hidden?
        onView(withId(R.id.startedView)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.notStartedView)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testIsTitleCorrect() {
        //is toolbar shown?
        onView(withId(R.id.toolbarExam)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbarExam)).check(matches(hasDescendant(withText(R.string.app_name))));
    }

    @Test
    public void testIsLoadingCompletes() {
        try {
            onView(withId(R.id.startedView)).check(matches(isDisplayed()));
        } catch (AssertionFailedError e) { //this means startedView was not visible, but not started view maybe is
            onView(withId(R.id.notStartedView)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testContinueButton() {
        //there must be a started course: let this be the 3. for example
        modifySharedPreferenceValue(rule.getScenario(), LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, 53);
        //mark the guide as read, so the dialog does not appear now
        modifySharedPreferenceValue(rule.getScenario(), GuideActivity.GUIDE_READ_PREFERENCE, true);
        //now wait for loading
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        //click button, should be visible
        onView(withId(R.id.continueLearningButton)).check(matches(isDisplayed()));
        onView(withId(R.id.continueLearningButton)).perform(click());
        //chapter activity should be in view
        onView(withId(R.id.drawer_layout_chapter_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testStartButton() {
        //there must NOT be a started course
        modifySharedPreferenceValue(rule.getScenario(), LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        //mark the guide as read, so the dialog does not appear now
        modifySharedPreferenceValue(rule.getScenario(), GuideActivity.GUIDE_READ_PREFERENCE, true);
        //now wait for loading
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        //click button, should be visible
        onView(withId(R.id.startLearningButton)).check(matches(isDisplayed()));
        onView(withId(R.id.startLearningButton)).perform(click());
        //chapter activity should be in view, WITH THE FIRST CHAPTER
        onView(withId(R.id.drawer_layout_chapter_root)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbarChapter)).check(matches(hasDescendant(withText("Java basics"))));
    }

    @Test
    public void testGuideIconButton() {
        //there must NOT be a started course
        modifySharedPreferenceValue(rule.getScenario(), LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        //now wait for loading
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withId(R.id.guideButton)).check(matches(isDisplayed()));
        onView(withId(R.id.guideButton)).perform(click());
        //is guide up?
        onView(withId(R.id.drawer_layout_guide_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testGuideDialogShowsUpAccepted() {
        //make it so there is no started course, so the start button shows up
        modifySharedPreferenceValue(rule.getScenario(), LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        //mark the guide as NOT read, so the dialog appears now
        modifySharedPreferenceValue(rule.getScenario(), GuideActivity.GUIDE_READ_PREFERENCE, false);
        //now wait for loading
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        //click button, should be visible
        onView(withId(R.id.startLearningButton)).check(matches(isDisplayed()));
        onView(withId(R.id.startLearningButton)).perform(click());
        //is dialog present?
        onView(withText(R.string.guide_not_read)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click()); //press accept
        //guide activity should be displayed
        onView(withId(R.id.drawer_layout_guide_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testGuideDialogShowsUpDenied() {
        //make it so there is no started course, so the start button shows up
        modifySharedPreferenceValue(rule.getScenario(), LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, -1);
        //mark the guide as NOT read, so the dialog appears now
        modifySharedPreferenceValue(rule.getScenario(), GuideActivity.GUIDE_READ_PREFERENCE, false);
        //now wait for loading
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        //click button, should be visible
        onView(withId(R.id.startLearningButton)).check(matches(isDisplayed()));
        onView(withId(R.id.startLearningButton)).perform(click());
        //is dialog present?
        onView(withText(R.string.guide_not_read)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click()); //press no
        //chapter should be displayed
        onView(withId(R.id.drawer_layout_chapter_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testAllImagesAddedToBackground() {
        int amount = RawParser.parseCodeImages(ApplicationProvider.getApplicationContext()).size();
        onView(withId(R.id.backgroundImagesLayout)).check(matches(hasChildCount(amount)));
    }

    @Test
    public void testFirstStartPreference() {
        //after loading the first start preference should be present
        rule.getScenario().onActivity(activity -> {
            SharedPreferences prefs = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            Assert.assertTrue(prefs.contains(LearnJavaActivity.FIRST_START));
        });
    }

    @Test
    public void testSwipeRightOnImage() throws InterruptedException {
        onView(withId(R.id.backgroundImagesLayout)).perform(swipeRight());
        //the swipe animations take time to complete
        Thread.sleep(1000);
        /*
        I found no way to programmatically validate that the background update happens,
        but if you look at the app while the test is ongoing, you can clearly see it works.
         */
    }

    @Test
    public void testSwipeLeftOnImage() throws InterruptedException {
        onView(withId(R.id.backgroundImagesLayout)).perform(swipeLeft());
        //the swipe animations take time to complete
        Thread.sleep(1000);
        /*
        I found no way to programmatically validate that the background update happens,
        but if you look at the app while the test is ongoing, you can clearly see it works.
         */
    }
}