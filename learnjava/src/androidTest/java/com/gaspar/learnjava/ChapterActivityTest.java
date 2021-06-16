package com.gaspar.learnjava;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.components.Component;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.database.ChapterStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.InteractiveScrollView;
import com.gaspar.learnjava.utils.ThemeUtils;

import org.hamcrest.Matcher;
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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.gaspar.learnjava.AndroidTestUtils.LoadingIdlingResource;
import static com.gaspar.learnjava.AndroidTestUtils.withFontSize;
import static com.gaspar.learnjava.AndroidTestUtils.withSpannableText;

/**
 * Instrumental tests for {@link ChapterActivity}. These use a test chapter, which is found where the
 * normal chapters are, in the file 'test_chapter.xml'. The test chapter is opened directly with an intent,
 * which is {@link #startIntent}, and cannot be opened from anywhere in the application.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ChapterActivityTest {

    //id of test chapter
    private static final int TEST_CHAPTER_ID = 100000;

    //this intent tells the ChapterActivity to open the test chapter
    public static Intent startIntent;

    //test chapter
    public static Chapter testChapter;

    //initialize the intent
    static {
        final Context context = ApplicationProvider.getApplicationContext();
        startIntent = new Intent(context, ChapterActivity.class);
        try { //parse test chapter, without components (the ChapterActivity will load components)
            testChapter = CourseParser.getInstance().parseChapter(TEST_CHAPTER_ID, false, context);
        } catch (Exception e) {
            throw new RuntimeException("The test chapter must be parsed to run these tests!");
        }
        //put the test chapter into the intent
        startIntent.putExtra(Chapter.CHAPTER_PREFERENCE_STRING, testChapter);

        /*
        Before we can test this, the test chapter needs to be in the the database. Normally
        this is handled on app launch, but here the LearnJavaActivity is bypassed, and ChapterActivity
        is started directly. So need to add the test chapter to the database manually.
         */
        if(LearnJavaDatabase.getInstance(context).getChapterDao().queryChapterStatus(TEST_CHAPTER_ID) == null) {
            ChapterStatus testStatus = new ChapterStatus(TEST_CHAPTER_ID, Status.UNLOCKED);
            LearnJavaDatabase.getInstance(context).getChapterDao().addChapterStatus(testStatus);
        }
    }

    //this rule opens the chapter activity before each test (with the correct intent, defined above)
    @Rule
    public ActivityScenarioRule<ChapterActivity> rule = new ActivityScenarioRule<>(startIntent);

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
                   View chapterComponentsView = activity.findViewById(R.id.chapterComponentsLayout);
                   isIdle = chapterComponentsView.getVisibility() == View.VISIBLE;
                });
                return isIdle;
            }
        };
        loadingIdlingResource = new LoadingIdlingResource(loadingPredicate, rule);
        //register
        IdlingRegistry.getInstance().register(loadingIdlingResource);
        //dark theme dialog, methods that need this will have to turn it on and call it manually
        ThemeUtils.showDarkThemeDialog = false;
        //always start in orange theme
        ThemeUtils.updateSelectedTheme(ApplicationProvider.getApplicationContext(), ThemeUtils.Themes.ORANGE);
        //disable clip sync, here only the simple copy to clipboard is tested
        AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ClipSyncActivity.CLIP_SYNC_PREF_NAME, ClipSyncActivity.ClipSyncMode.NOT_SELECTED);
    }

    @After
    public void tearDown()  {
        //unregister
        IdlingRegistry.getInstance().unregister(loadingIdlingResource);
        ThemeUtils.showDarkThemeDialog = true;
    }

    @Test
    public void testChapterComponentsVisible() {
        onView(withId(R.id.chapterComponentsLayout)).check(matches(isDisplayed()));
    }

    @Test
    public void testChapterTitleCorrect() {
        onView(withId(R.id.toolbarChapter)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbarChapter)).check(matches(hasDescendant(withText(testChapter.getName()))));
    }

    @Test
    public void testAllChapterComponentsAdded() {
        rule.getScenario().onActivity(activity -> {
            //this chapter now has components, the loading completed
            Chapter parsedChapter = activity.getPassedChapter();
            ViewGroup componentsView = activity.findViewById(R.id.chapterComponents);
            //the +1 is needed, since the layout contains one extra view: the "confirm" view, with the confirm button
            Assert.assertEquals(parsedChapter.getComponents().size() + 1, componentsView.getChildCount());
        });
    }

    @Test
    public void testTextComponent() {
        final Matcher<View> textViewMatcher = withSpannableText("Text element 1.");
        onView(textViewMatcher).perform(scrollTo());
        onView(textViewMatcher).check(matches(isDisplayed()));
        //is the corresponding component a text component?
        rule.getScenario().onActivity(activity ->
                Assert.assertEquals(Component.ComponentType.TEXT, activity.getPassedChapter().getComponents().get(0).getType()));
    }

    @Test
    public void testTitleComponent() {
        final Matcher<View> titleViewMatcher = withSpannableText("Title element");
        onView(titleViewMatcher).perform(scrollTo());
        onView(titleViewMatcher).check(matches(isDisplayed()));
        //is the corresponding component a text component?
        rule.getScenario().onActivity(activity ->
                Assert.assertEquals(Component.ComponentType.TITLE, activity.getPassedChapter().getComponents().get(1).getType()));
    }

    @Test
    public void testCodeComponent() {
        onView(withId(R.id.codeComponent)).perform(scrollTo());
        onView(withId(R.id.codeComponent)).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity ->
                Assert.assertEquals(Component.ComponentType.CODE, activity.getPassedChapter().getComponents().get(3).getType()));
    }

    @Test
    public void testCodeComponentZoomIn() {
        final Matcher<View> zoomInMatcher = withId(R.id.zoomInButton);
        onView(zoomInMatcher).perform(scrollTo());
        onView(zoomInMatcher).check(matches(isDisplayed()));
        //default size?
        onView(withId(R.id.codeArea)).check(matches(isDisplayed()));
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.codeArea)).check(matches(withFontSize(defSize)));
        //press zoom in button
        onView(zoomInMatcher).perform(click());
        //font size increased?
        onView(withId(R.id.codeArea)).check(matches(withFontSize(defSize + Component.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testCodeComponentZoomOut() {
        final Matcher<View> zoomOutMatcher = withId(R.id.zoomOutButton);
        onView(zoomOutMatcher).perform(scrollTo());
        onView(zoomOutMatcher).check(matches(isDisplayed()));
        //default size?
        onView(withId(R.id.codeArea)).check(matches(isDisplayed()));
        int defSize = (int)Math.ceil(ApplicationProvider.getApplicationContext().getResources().getDimension(R.dimen.code_text_size));
        onView(withId(R.id.codeArea)).check(matches(withFontSize(defSize)));
        //press zoom out button
        onView(zoomOutMatcher).perform(click());
        //font size decreased?
        onView(withId(R.id.codeArea)).check(matches(withFontSize(defSize - Component.ZOOM_SIZE_CHANGE)));
    }

    @Test
    public void testCodeComponentCopyButton() {
        //clip sync should be disabled here
        final Matcher<View> copyButtonMatcher = withId(R.id.copyButton);
        onView(copyButtonMatcher).perform(scrollTo());
        onView(copyButtonMatcher).check(matches(isDisplayed()));
        //press
        onView(copyButtonMatcher).perform(click());
        //snackbar?
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.copy_successful)));
        rule.getScenario().onActivity(activity -> {
            View codeComponent = activity.findViewById(R.id.codeComponent);
            TextView codeArea = codeComponent.findViewById(R.id.codeArea);
            String code = codeArea.getText().toString();
            //is this in the clipboard?
            final ClipboardManager manager = (ClipboardManager)activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE);
            ClipData clipboardContent = manager.getPrimaryClip();
            Assert.assertNotNull(clipboardContent);
            ClipData.Item item = clipboardContent.getItemAt(0);
            Assert.assertNotNull(item);
            String clipboardText = item.getText().toString();
            Assert.assertEquals(code, clipboardText);
        });
    }

    @Test
    public void testAdvancedComponent() { //index: 7
        onView(withId(R.id.advancedComponent)).perform(scrollTo());
        onView(withId(R.id.advancedComponent)).check(matches(isDisplayed()));
        //title correct?
        onView(withSpannableText("Advanced element")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity ->
            Assert.assertEquals(Component.ComponentType.ADVANCED, activity.getPassedChapter().getComponents().get(7).getType())
        );
    }

    @Test
    public void testBoxedComponent() { //index: 9
        onView(withId(R.id.boxedComponent)).perform(scrollTo());
        onView(withId(R.id.boxedComponent)).check(matches(isDisplayed()));
        //title?
        onView(withSpannableText("Boxed element")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity ->
                Assert.assertEquals(Component.ComponentType.BOXED, activity.getPassedChapter().getComponents().get(9).getType()));
    }

    //test that scrolling to the bottom completes the course
    @Test
    public void testChapterCompletionWithScroll() throws InterruptedException {
        rule.getScenario().onActivity(activity -> {
            InteractiveScrollView scrollView = activity.findViewById(R.id.chapterComponentsLayout);
            scrollView.smoothScrollTo(0, scrollView.getHeight());
        });
        //allow time for database to update
        Thread.sleep(1500);
        //query status
        ChapterStatus status = LearnJavaDatabase.getInstance(ApplicationProvider.getApplicationContext())
                .getChapterDao().queryChapterStatus(TEST_CHAPTER_ID);
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.COMPLETED, status.getStatus());
        //change status back from completed, for other tests
        status = new ChapterStatus(TEST_CHAPTER_ID, Status.UNLOCKED);
        LearnJavaDatabase.getInstance(ApplicationProvider.getApplicationContext())
                .getChapterDao().updateChapterStatus(status);
    }

    //test that clicking the button completes the course
    @Test
    public void testChapterCompletionWithClick() throws InterruptedException {
        //scroll to the button (this wont trigger the completion by scroll, since it does not go completely to the bottom)
        onView(withId(R.id.chapterConfirmButton)).perform(scrollTo());
        //this closes the activity
        onView(withId(R.id.chapterConfirmButton)).perform(click());
        //allow time for database to update
        Thread.sleep(1500);
        //query status
        ChapterStatus status = LearnJavaDatabase.getInstance(ApplicationProvider.getApplicationContext())
                .getChapterDao().queryChapterStatus(TEST_CHAPTER_ID);
        Assert.assertNotNull(status);
        Assert.assertEquals(Status.COMPLETED, status.getStatus());
        //change status back from completed, for other tests
        status = new ChapterStatus(TEST_CHAPTER_ID, Status.UNLOCKED);
        LearnJavaDatabase.getInstance(ApplicationProvider.getApplicationContext())
                .getChapterDao().updateChapterStatus(status);
    }

    /* I can't get these test to work. Dialog does not show up, test hangs

    @Test
    public void testDarkThemeDialogAccepted() throws InterruptedException {
        ThemeUtils.showDarkThemeDialog = true;
        AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ThemeUtils.SHOW_DARK_THEME_PROMPT, true);
        rule.getScenario().onActivity(ThemeUtils::showDarkThemePromptIfNeeded);
        Thread.sleep(1000);
        //test dialog, should be visible
        onView(withText(R.string.dark_theme_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(DialogButtonId.POSITIVE.getId())).perform(click());
        //theme should be dark
        Assert.assertTrue(ThemeUtils.isDarkTheme());
        //go back to orange
        ThemeUtils.updateSelectedTheme(ApplicationProvider.getApplicationContext(), ThemeUtils.Themes.ORANGE);
    }

    @Test
    public void testDarkThemeDialogDenied() {
        //test dialog, should be visible
        onView(withText(R.string.dark_theme_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(DialogButtonId.NEGATIVE.getId())).perform(click());
        //theme should be orange
        Assert.assertFalse(ThemeUtils.isDarkTheme());
    }

    */
}