package com.gaspar.learnjava;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.parsers.CourseParser;
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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

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
        loadingIdlingResource = new AndroidTestUtils.LoadingIdlingResource(loadingPredicate, rule);
        //register
        IdlingRegistry.getInstance().register(loadingIdlingResource);
        //dark theme dialog
        if(testName.getMethodName().equals("testDarkThemeDialogAccepted") || testName.getMethodName().equals("testDarkThemeDialogDenied")) {
            AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ThemeUtils.SHOW_DARK_THEME_PROMPT, null);
            System.out.println("Removed DARK_THEME_DIALOG preference, it should appear!");
        } else {
            AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ThemeUtils.SHOW_DARK_THEME_PROMPT, true);
            System.out.println("Added DARK_THEME_DIALOG preference, it should not appear!");
        }

    }

    @After
    public void tearDown()  {
        //unregister
        IdlingRegistry.getInstance().unregister(loadingIdlingResource);
        AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ThemeUtils.SHOW_DARK_THEME_PROMPT, null);
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
        rule.getScenario().onActivity(activity -> {
            ViewGroup componentsView = activity.findViewById(R.id.chapterComponents);
            View child = componentsView.getChildAt(0);
            Assert.assertTrue(child instanceof TextView);
            Assert.assertEquals(Component.ComponentType.TEXT, activity.getPassedChapter().getComponents().get(0).getType());
        });
    }

    @Test
    public void testTitleComponent() {
        rule.getScenario().onActivity(activity -> {
            ViewGroup componentsView = activity.findViewById(R.id.chapterComponents);
            View child = componentsView.getChildAt(1);
            Assert.assertTrue(child instanceof RelativeLayout);
            Assert.assertEquals(Component.ComponentType.TITLE, activity.getPassedChapter().getComponents().get(1).getType());
        });
    }

    private View child;

    @Test
    public void testCodeComponentZoomIn() {
        rule.getScenario().onActivity(activity -> {
            ViewGroup componentsView = activity.findViewById(R.id.chapterComponents);
            child = componentsView.getChildAt(3);
            Assert.assertTrue(child instanceof LinearLayout);
            Assert.assertEquals(Component.ComponentType.CODE, activity.getPassedChapter().getComponents().get(3).getType());
        });
        onView(withId(R.id.codeComponent)).perform(scrollTo());
        //press zoom in button
        float textSizeBefore = ((TextView)child.findViewById(R.id.codeArea)).getTextSize();
        String codeTag = ApplicationProvider.getApplicationContext().getString(R.string.code);
        onView(allOf(withId(R.id.zoomInButton), withTagValue(is(codeTag)))).perform(click());
        float textSizeAfter = ((TextView)child.findViewById(R.id.codeArea)).getTextSize();
        Assert.assertTrue(textSizeAfter > textSizeBefore);
    }

    @Test
    public void testCodeComponentZoomOut() {
        rule.getScenario().onActivity(activity -> {
            ViewGroup componentsView = activity.findViewById(R.id.chapterComponents);
            child = componentsView.getChildAt(3);
            child.requestFocus(); //scroll to view
            Assert.assertTrue(child instanceof LinearLayout);
            Assert.assertEquals(Component.ComponentType.CODE, activity.getPassedChapter().getComponents().get(3).getType());
        });
        onView(withId(R.id.codeComponent)).perform(scrollTo());
        //press zoom out button
        float textSizeBefore = ((TextView)child.findViewById(R.id.codeArea)).getTextSize();
        String codeTag = ApplicationProvider.getApplicationContext().getString(R.string.code);
        onView(allOf(withId(R.id.zoomOutButton), withTagValue(is(codeTag)))).perform(click());
        float textSizeAfter = ((TextView)child.findViewById(R.id.codeArea)).getTextSize();
        Assert.assertTrue(textSizeAfter <= textSizeBefore);
    }



    @Test
    public void testDarkThemeDialogAccepted() {
        //test dialog, should be visible
        onView(withText(R.string.dark_theme_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //theme should be dark
        Assert.assertTrue(ThemeUtils.isDarkTheme());
        //go back to orange
        ThemeUtils.updateSelectedTheme(ApplicationProvider.getApplicationContext(), ThemeUtils.Themes.ORANGE);
    }

    @Test
    public void testDarkThemeDialogDenied() {
        //test dialog, should be visible
        onView(withText(R.string.dark_theme_info)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //theme should be orange
        Assert.assertFalse(ThemeUtils.isDarkTheme());
    }


}