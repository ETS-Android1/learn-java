package com.gaspar.learnjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.gms.ads.AdView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Predicate;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

/**
 * UI tests for {@link CoursesActivity}. This class displays an ad, so before running tests, the
 * {@link LearnJavaAds#LOAD_ADS} and {@link LearnJavaAds#DEBUG_ADS} should be configured. This is done
 * in {@link #setUp()}.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class CoursesActivityTest {

    private static List<Course> courses;

    //this activity requires courses to be parsed
    static {
        if(CoursesActivity.coursesNotParsed()) {
            try {
                courses = CourseParser.getInstance().parseCourses(ApplicationProvider.getApplicationContext());
                CoursesActivity.setParsedCourses(courses);
            } catch (Exception e) {
                throw new RuntimeException("Can't run tests without parsed courses!");
            }
            //init ads
            LearnJavaAds.initAdConstants(ApplicationProvider.getApplicationContext());
            //disable auto slide open
            SharedPreferences preferences = ApplicationProvider.getApplicationContext()
                    .getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            preferences.edit().putBoolean(SettingsActivity.AUTO_SLIDE_OPEN_PREF_NAME, false);
            //set debug
            LearnJavaActivity.DEBUG = true;
        }
    }

    //starts activity before test
    @Rule
    public ActivityScenarioRule<CoursesActivity> rule = new ActivityScenarioRule<>(CoursesActivity.class);

    //access to test method name
    @Rule
    public TestName testName = new TestName();

    //waits until courses are loaded
    private IdlingResource loadingResource;

    @Before
    public void setUp() {
        //loading resource
        Predicate<ActivityScenarioRule<?>> loadingPredicate = new Predicate<ActivityScenarioRule<?>>() {
            private boolean isIdle;
            @Override
            public boolean test(ActivityScenarioRule<?> rule) {
                rule.getScenario().onActivity(activity -> {
                    View loadingView = activity.findViewById(R.id.loadingIndicator);
                    AdView adView = activity.findViewById(R.id.ad_view_banner_id);
                    TextView adStatusLabel = activity.findViewById(R.id.adStatusLabel);
                    boolean adLoadingFinished;
                    if(LearnJavaAds.LOAD_ADS) {
                        adLoadingFinished = (adView != null && adView.getVisibility() == View.VISIBLE)
                                || adStatusLabel.getText().toString().equals(activity.getString(R.string.ad_load_fail));
                    } else {
                        //not loading ads at all
                        adLoadingFinished = true;
                    }
                    //idle when courses and ad loading resolved
                    isIdle = (loadingView.getVisibility() == View.GONE) && adLoadingFinished;
                });
                return isIdle;
            }
        };
        loadingResource = new AndroidTestUtils.LoadingIdlingResource(loadingPredicate, rule);
        //register
        IdlingRegistry.getInstance().register(loadingResource);
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(loadingResource);
    }

    @Test
    public void testCoursesActivityVisible() {
        onView(withId(R.id.drawer_layout_courses_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testAllCoursesAdded() {
        onView(withId(R.id.courseSelectors)).check(matches(hasChildCount(courses.size())));
    }

    @Test
    public void testIsCoursesAdVisible() {
        try {
            onView(withId(R.id.ad_view_banner_id)).check(matches(isDisplayed()));
        } catch (Exception e) {
            //it is possible that ad failed to load, in this case a text is shown
            onView(withId(R.id.adStatusLabel)).check(matches(allOf(isDisplayed(), withText(R.string.ad_load_fail))));
        }
    }

    @Test
    public void testCourseNameOnClick() throws InterruptedException {
        //open first course
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.courseNameView)).perform(click());
        Thread.sleep(1000);
        //check if components became visible
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.chaptersView)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.tasksView)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.examSelector)).check(matches(isDisplayed()));
    }

    @Test
    public void testOpenedCourseNameOnClick() throws InterruptedException {
        //first open
        testCourseNameOnClick();
        //close to click
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.courseNameView)).perform(click());
        Thread.sleep(1000);
        //check if components hidden
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.chaptersView)).check(matches(not(isDisplayed())));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.tasksView)).check(matches(not(isDisplayed())));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.examSelector)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testOpenCourseWithFloatingActionButton() throws InterruptedException {
        onView(withId(R.id.dropDownAllIcon)).check(matches(isDisplayed()));
        onView(withId(R.id.dropDownAllIcon)).perform(click());
        Thread.sleep(1000);
        //check if components displayed, only for first
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.chaptersView)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.tasksView)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.examSelector)).check(matches(isDisplayed()));
    }

    //open first chapter of the first course
    @Test
    public void testOpenChapterFromCourses() throws InterruptedException {
        //open first course
        testCourseNameOnClick();
        //disable this dialog that can ruin this test
        ThemeUtils.showDarkThemeDialog = false;
        //click first chapter
        String firstChapterName = courses.get(0).getChapters().get(0).getName();
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.chaptersView))
                .onChildView(withText(firstChapterName)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.chaptersView))
                .onChildView(withText(firstChapterName)).perform(click());
        //chapter activity should be visible
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout_chapter_root)).check(matches(isDisplayed()));
        //re enable
        ThemeUtils.showDarkThemeDialog = true;
    }

    //open first task of first course
    @Test
    public void testOpenTaskFromCourses() throws InterruptedException {
        //open first course
        testCourseNameOnClick();
        //disable this dialog that can ruin this test
        ThemeUtils.showDarkThemeDialog = false;
        //click first chapter
        String firstTaskName = courses.get(0).getTasks().get(0).getName();
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.tasksView))
                .onChildView(withText(firstTaskName)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withId(R.id.tasksView))
                .onChildView(withText(firstTaskName)).perform(click());
        //chapter activity should be visible
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout_task_root)).check(matches(isDisplayed()));
        //re enable
        ThemeUtils.showDarkThemeDialog = true;
    }

    //open exam of first course: it will be locked, needs chapters
    @Test
    public void testOpenExamFromCourses() throws InterruptedException {
        //open first course
        testCourseNameOnClick();
        //disable this dialog that can ruin this test
        ThemeUtils.showDarkThemeDialog = false;
        //click take exam button
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withText(R.string.take_exam))
                .check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(0).onChildView(withText(R.string.take_exam))
                .perform(click());
        //a confirm dialog shows up
        onView(withText(R.string.confirm_exam_start)).inRoot(isDialog()).check(matches(isDisplayed()));
        //click ok
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //wait for exam to load
        Thread.sleep(1000);
        onView(withId(R.id.exam_root)).check(matches(isDisplayed()));
        ThemeUtils.showDarkThemeDialog = true;
    }

    //this test assumes course 2 is still locked
    @Test
    public void testClickOnLockedCourse() throws InterruptedException {
        //must disable debug, all courses can be opened in debug
        LearnJavaActivity.DEBUG = false;
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(1).onChildView(withId(R.id.courseNameView)).perform(click());
        Thread.sleep(1000);
        //did not open
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(1).onChildView(withId(R.id.chaptersView)).check(matches(not(isDisplayed())));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(1).onChildView(withId(R.id.tasksView)).check(matches(not(isDisplayed())));
        onData(anything()).inAdapterView(withId(R.id.courseSelectors)).atPosition(1).onChildView(withId(R.id.examSelector)).check(matches(not(isDisplayed())));
        //re enable debug
        LearnJavaActivity.DEBUG = true;
    }
}