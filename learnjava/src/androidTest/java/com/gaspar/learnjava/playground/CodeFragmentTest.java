package com.gaspar.learnjava.playground;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.gaspar.learnjava.AndroidTestUtils;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.components.CodeComponent;
import com.gaspar.learnjava.database.LearnJavaDatabase;

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
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.gaspar.learnjava.AndroidTestUtils.selectTabAtPosition;
import static com.gaspar.learnjava.AndroidTestUtils.withFontSize;
import static junit.framework.Assert.assertEquals;

/**
 * The {@link CodeFragment} is part of {@link PlaygroundActivity}, responsible for listing the {@link com.gaspar.learnjava.database.PlaygroundFile}s
 * and providing a formatted, editable code sample for each. Functionality that is managed by this fragment is tested here.
 * <p>
 * These tests assume that the database's playground file table contains nothing, as if the user started {@link PlaygroundActivity} for
 * the first time.
 * @see PlaygroundActivityTest
 */
public class CodeFragmentTest {

    @Rule
    public ActivityScenarioRule<PlaygroundActivity> rule = new ActivityScenarioRule<>(PlaygroundActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        //dont show info dialog
        PlaygroundActivity.forceHideInfoDialog = true;
        //clear playground table in database
        Context context = ApplicationProvider.getApplicationContext();
        LearnJavaDatabase.getInstance(context).getPlaygroundFileDao().deleteRecords();
        //each test starts with navigating to the correct fragment
        onView(withId(R.id.playgroundTabLayout)).perform(selectTabAtPosition(PlaygroundTab.TAB_CODE));
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        PlaygroundActivity.forceHideInfoDialog = false;
    }

    @Test
    public void testCodeFragmentVisible() {
        onView(withId(R.id.fragment_code_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testMainJavaVisible() {
        //we start with a clear database, main should be present and visible by default
        onView(withText(CodeFragment.MAIN_JAVA_FILE_NAME)).check(matches(isDisplayed()));
    }

    /**
     * Helper method that creates a new file using the UI.
     * @param fileName File name, as it should be typed into the new file dialog, without file extension.
     */
    private void createNewFile(@NonNull String fileName) {
        //click selector
        onView(withId(R.id.fileSelectorSpinner)).perform(click());
        //assert new file is visible
        onView(withText(R.string.playground_new_file)).check(matches(isDisplayed()));
        //click new
        onView(withText(R.string.playground_new_file)).perform(click());
        //validate dialog
        onView(withText(R.string.playground_create_file_description)).inRoot(isDialog()).check(matches(isDisplayed()));
        //type in the file name
        onView(withId(R.id.fileNameInput)).check(matches(withHint(R.string.playground_create_file_hint)));
        onView(withId(R.id.fileNameInput)).perform(typeText(fileName), closeSoftKeyboard());
        //click ok
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
    }

    @Test
    public void testSwitchBetweenFiles() {
        createNewFile("Test");
        //switch back to main
        onView(withId(R.id.fileSelectorSpinner)).perform(click());
        onView(withText(CodeFragment.MAIN_JAVA_FILE_NAME)).perform(click());
        onView(withText(CodeFragment.MAIN_JAVA_FILE_NAME)).check(matches(isDisplayed()));
    }

    //test file create dialog with valid file name
    @Test
    public void testCreateNewFile() {
        createNewFile("Cat");
        //should be displayed now in the spinner
        onView(withText("Cat.java")).check(matches(isDisplayed()));
        //activity should be notified about this
        rule.getScenario().onActivity(activity -> assertEquals(2, activity.getPlaygroundFileAmount()));
    }

    @Test
    public void testCreateNewFileInvalidName() {
        createNewFile("__car__");
        //dialog should still be up with an error
        onView(withText(R.string.playground_create_file_incorrect)).check(matches(isDisplayed()));
    }

    private final String[] testFileNames = {"Cat", "Dog", "Car", "Test", "Random", "Some", "Haha"};

    @Test
    public void testCreateMoreThenMaxFiles() {
        //there is already one file: Main.java, create MAX_FILES-1 more
        for(int i = 0; i < CodeFragment.MAX_FILES - 1; i++) {
            createNewFile(testFileNames[i]);
        }
        //try to create another
        //click selector
        onView(withId(R.id.fileSelectorSpinner)).perform(click());
        //assert new file is visible
        onView(withText(R.string.playground_new_file)).check(matches(isDisplayed()));
        //click new
        onView(withText(R.string.playground_new_file)).perform(click());
        //a dialog displayed the error
        onView(withText(R.string.playground_too_many_files)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void testInputZoomIn() {
        //should have default text size
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.playgroundCodeArea)).check(matches(withFontSize(defSize)));
        //press zoom button
        onView(withId(R.id.playgroundZoomInButton)).perform(click());
        //text size increased
        onView(withId(R.id.playgroundCodeArea)).check(matches(withFontSize(defSize + CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testInputZoomOut() {
        //should have default text size
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.playgroundCodeArea)).check(matches(withFontSize(defSize)));
        //press zoom button
        onView(withId(R.id.playgroundZoomOutButton)).perform(click());
        //text size increased
        onView(withId(R.id.playgroundCodeArea)).check(matches(withFontSize(defSize - CodeComponent.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testDeleteMainJava() {
        //click delete while on main
        onView(withId(R.id.playgroundDeleteButton)).perform(click());
        //dialog about main can't be deleted
        onView(withText(R.string.playground_delete_main)).inRoot(isDialog()).check(matches(isDisplayed()));
        //click accept
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.playground_main_reset)));
    }

    @Test
    public void testDeleteFile() {
        //create and select new
        createNewFile("Random");
        //click delete while on main
        onView(withId(R.id.playgroundDeleteButton)).perform(click());
        //confirm dialog
        onView(withText(R.string.playground_delete_normal_file)).inRoot(isDialog()).check(matches(isDisplayed()));
        //accept
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.playground_file_deleted)));
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //main is showing
        onView(withText(CodeFragment.MAIN_JAVA_FILE_NAME)).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity -> assertEquals(1, activity.getPlaygroundFileAmount()));
    }

    @Test
    public void testDeleteFileCancel() {
        //create and select new
        createNewFile("Random");
        //click delete while on main
        onView(withId(R.id.playgroundDeleteButton)).perform(click());
        //confirm dialog
        onView(withText(R.string.playground_delete_normal_file)).inRoot(isDialog()).check(matches(isDisplayed()));
        //cancel
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //the newly created file should still be showing
        onView(withText("Random.java")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity -> assertEquals(2, activity.getPlaygroundFileAmount()));
    }

    //this button does not support clip sync
    @Test
    public void testCopyButton() {
        onView(withId(R.id.playgroundCopyButton)).perform(click());
        //snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.copy_successful)));
    }
}