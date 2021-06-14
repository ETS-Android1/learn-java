package com.gaspar.learnjava;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;
import com.gaspar.learnjava.parsers.TaskParser;
import com.gaspar.learnjava.utils.ThemeUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.function.Predicate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumental tests for {@link TaskActivity}, which shows a task and its solution. Standard components
 * (text, boxed, code, etc...) are tested in {@link ChapterActivityTest}, and the tests are not repeated
 * here. Only things that are not present in a {@link ChapterActivity} are tested here, such as solution
 * related views.
 * <p>
 * Uses a test task, which can be found where the normal tasks are, named test_task.xml. The tesk task
 * can never be loaded by the actual application, and it not translated.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class TaskActivityTest {

    //id of test task
    private static final int TEST_TASK_ID = 100000;

    //this intent tells the ChapterActivity to open the test chapter
    public static Intent startIntent;

    //test chapter
    public static Task testTask;

    //initialize the intent and test task
    static {
        final Context context = ApplicationProvider.getApplicationContext();
        startIntent = new Intent(context, TaskActivity.class);
        try { //parse test task, without components (the TaskActivity will load components)
            testTask = TaskParser.getInstance().parseTask(TEST_TASK_ID, false, context);
        } catch (Exception e) {
            throw new RuntimeException("The test task must be parsed to run these tests!");
        }
        //put the test task into the intent
        startIntent.putExtra(Task.TASK_PREFERENCE_STRING, testTask);

        /*
        Before we can test this, the test task needs to be in the the database. Normally
        this is handled on app launch, but here the LearnJavaActivity is bypassed, and TaskActivity
        is started directly. So need to add the test task to the database manually.
         */
        if(LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(TEST_TASK_ID) == null) {
            TaskStatus testTaskStatus = new TaskStatus();
            testTaskStatus.setTaskId(TEST_TASK_ID);
            testTaskStatus.setStatus(Status.UNLOCKED);
            LearnJavaDatabase.getInstance(context).getTaskDao().addTaskStatus(testTaskStatus);
        }
    }

    //this rule opens the task activity before each test (with the correct intent, defined above)
    @Rule
    public ActivityScenarioRule<TaskActivity> rule = new ActivityScenarioRule<>(startIntent);

    //gives access to test name
    @Rule
    public TestName testName = new TestName();

    //idling resource that waits until the chapter components are loaded
    public IdlingResource loadingIdlingResource;

    @Before
    public void setUp() {
        Predicate<ActivityScenarioRule<?>> loadingPredicate = new Predicate<ActivityScenarioRule<?>>() {
            private boolean isIdle;
            @Override
            public boolean test(ActivityScenarioRule<?> rule) {
                rule.getScenario().onActivity(activity -> {
                    View chapterComponentsView = activity.findViewById(R.id.taskComponents);
                    isIdle = chapterComponentsView.getVisibility() == View.VISIBLE;
                });
                return isIdle;
            }
        };
        loadingIdlingResource = new AndroidTestUtils.LoadingIdlingResource(loadingPredicate, rule);
        //register
        IdlingRegistry.getInstance().register(loadingIdlingResource);
        //dark theme dialog, methods that need this will have to turn it on and call it manually
        ThemeUtils.showDarkThemeDialog = false;
        //always start in orange theme
        ThemeUtils.updateSelectedTheme(ApplicationProvider.getApplicationContext(), ThemeUtils.Themes.ORANGE);
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(loadingIdlingResource);
        ThemeUtils.showDarkThemeDialog = true;
    }

    @Test
    public void testTaskDisplayed() {
        onView(withId(R.id.drawer_layout_task_root)).check(matches(isDisplayed()));
    }

    //tests for the view that can be used to display solution, NOT the actual solution
    @Test
    public void testSolutionShowerDisplayed() {
        onView(withId(R.id.hideShowSolutionLayout)).check(matches(isDisplayed()));
    }

    //test if solution appears after click
    @Test
    public void testSolutionShower() throws InterruptedException {
        onView(withId(R.id.hideShowSolutionLayout)).check(matches(isDisplayed()));
        //click
        onView(withId(R.id.hideShowSolutionLayout)).perform(click());
        //this animation does not seem to complete instantly, so wait
        Thread.sleep(1500);
        //now check visibility
        onView(withId(R.id.solutionComponents)).check(matches(isDisplayed()));
    }

    //test if solution disappears after press
    @Test
    public void testSolutionHider() throws InterruptedException {
        //for this it needs to appear first
        testSolutionShower();
        //now click again
        onView(withId(R.id.hideShowSolutionLayout)).perform(click());
        //this animation does not seem to complete instantly, so wait
        Thread.sleep(1500);
        //now check visibility
        onView(withId(R.id.taskComponents)).check(matches(isDisplayed()));
    }

    @Test
    public void testCompletedCheckbox() throws InterruptedException {
        //for some reason this wait is needed
        Thread.sleep(1500);
        onView(withId(R.id.taskCompletedCheckBox)).check(matches(isDisplayed()));
        //press
        onView(withId(R.id.taskCompletedCheckBox)).perform(click());
        //wait for database to be updated, 1.5 sec should be enough
        Thread.sleep(1500);
        //ask status from database
        final Context context = ApplicationProvider.getApplicationContext();
        TaskStatus taskStatus = LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(TEST_TASK_ID);
        Assert.assertEquals(Status.COMPLETED, taskStatus.getStatus());
        //move task back to unlocked status, other tests may need this
        TaskStatus testTaskStatus = new TaskStatus();
        testTaskStatus.setTaskId(TEST_TASK_ID);
        testTaskStatus.setStatus(Status.UNLOCKED);
        LearnJavaDatabase.getInstance(ApplicationProvider.getApplicationContext()).getTaskDao().updateTaskStatus(testTaskStatus);
    }

    @Test
    public void testCompletedCheckboxUncheck() throws InterruptedException {
        //for some reason this wait is needed
        Thread.sleep(1500);
        onView(withId(R.id.taskCompletedCheckBox)).check(matches(isDisplayed()));
        //press and wait a bit for database
        onView(withId(R.id.taskCompletedCheckBox)).perform(click());
        Thread.sleep(1500);
        //press again
        onView(withId(R.id.taskCompletedCheckBox)).perform(click());
        //wait for database to be updated, 1.5 sec should be enough
        Thread.sleep(1500);
        //ask status from database
        final Context context = ApplicationProvider.getApplicationContext();
        TaskStatus taskStatus = LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(TEST_TASK_ID);
        Assert.assertEquals(Status.UNLOCKED, taskStatus.getStatus());
    }
}