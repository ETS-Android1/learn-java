package com.gaspar.learnjava;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.gaspar.learnjava.asynctask.LoadExamQuestionsTask;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Question;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.ExamParser;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Predicate;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Instrumental tests for the {@link ExamActivity}, which displays an ongoing exam. For this, a
 * test exam is used, which can be found where the normal exams are, with the name test_exam.xml.
 * This exam can never be loaded by the actual application.
 * <p>
 * Besides the exam activity, {@link com.gaspar.learnjava.curriculum.Question}-s are also tested here,
 * as the test exam contains at least one of each question type.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ExamActivityTest {

    //id of the dummy exam
    private static final int TEST_EXAM_ID = 100000;

    //this intent tells the ExamActivity to open the test chapter
    public static Intent startIntent;

    //test exam
    public static Exam testExam;

    //test exam, but with questions parsed
    public static Exam testExamParsed;

    //initialize the intent
    static {
        final Context context = ApplicationProvider.getApplicationContext();
        startIntent = new Intent(context, ExamActivity.class);
        try {
            //parse test exam, without questions (the ExamActivity will load questions)
            testExam = ExamParser.getInstance().parseExam(TEST_EXAM_ID, false, context);
            //also parse with questions, for easy access to question list
            testExamParsed = ExamParser.getInstance().parseExam(TEST_EXAM_ID, true, context);
        } catch (Exception e) {
            throw new RuntimeException("The test exam must be parsed to run these tests!");
        }
        //put the test chapter into the intent
        startIntent.putExtra(Exam.EXAM_PREFERENCE_STRING, testExam);

        /*
        Before we can test this, the test exam needs to be in the the database. Normally
        this is handled on app launch, but here the LearnJavaActivity is bypassed, and ExamActivity
        is started directly. So need to add the test exam to the database manually.
         */
        if(LearnJavaDatabase.getInstance(context).getExamDao().queryExamStatus(TEST_EXAM_ID) == null) {
            ExamStatus examStatus = new ExamStatus(TEST_EXAM_ID, Status.UNLOCKED, -1, -1); //no last started and top score
            LearnJavaDatabase.getInstance(context).getExamDao().addExamStatus(examStatus);
        }
        //disable shuffle
        LoadExamQuestionsTask.shuffleQuestions = false;
    }

    @Rule //this rule launches this activity before each test
    public ActivityScenarioRule<ExamActivity> rule = new ActivityScenarioRule<>(startIntent);

    @Rule //this rule stores the test name
    public TestName testName = new TestName();

    //waits for exam to load
    private IdlingResource loadingIdleResource;

    @Before
    public void setUp() {
        //will wait until loading is complete
        Predicate<ActivityScenarioRule<?>> loadingPredicate = new Predicate<ActivityScenarioRule<?>>() {
            private boolean isIdle;
            @Override
            public boolean test(ActivityScenarioRule<?> rule) {
                rule.getScenario().onActivity(activity -> {
                    View loadingIndicator = activity.findViewById(R.id.loadingIndicator);
                    isIdle = loadingIndicator.getVisibility() == View.GONE;
                });
                return isIdle;
            }

        };
        loadingIdleResource = new AndroidTestUtils.LoadingIdlingResource(loadingPredicate, rule);
        IdlingRegistry.getInstance().register(loadingIdleResource);
        //disable the confirm dialog for the individual question tests
        if(testName.getMethodName().startsWith("testQuestion")) {
            ExamActivity.disableConfirmFinishWarning = true;
            rule.getScenario().onActivity(activity -> questionViewIds = activity.getQuestionViewIds());
        } else {
            ExamActivity.disableConfirmFinishWarning = false;
            questionViewIds = null;
        }
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(loadingIdleResource);
    }

    @Test
    public void testExamActivityVisible() {
        onView(withId(R.id.exam_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testExamActivityAbandonWarningAccepted() {
        //activity is loaded here, press back
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressBack();
        //warning dialog should appear
        onView(withText(R.string.confirm_abandon_exam)).inRoot(isDialog()).check(matches(isDisplayed()));
        //press ok
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //activity should be closed
        Assert.assertEquals(Lifecycle.State.DESTROYED, rule.getScenario().getState());
    }

    @Test
    public void testExamActivityAbandonWarningDenied() {
        //activity is loaded here, press back
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressBack();
        //warning dialog should appear
        onView(withText(R.string.confirm_abandon_exam)).inRoot(isDialog()).check(matches(isDisplayed()));
        //press cancel
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //activity should still be visible
        onView(withId(R.id.exam_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testUnfinishedWarningDialogAppears() {
        //click finish button
        onView(withId(R.id.finishExamButton)).perform(click());
        //there are unanswered questions, dialog should pop
        onView(withText(R.string.confirm_finish_exam)).inRoot(isDialog()).check(matches(isDisplayed()));
        //is the amount of unanswered questions correctly displayed?
        int questionAmount = testExam.getQuestionAmount(); //right now all questions are unanswered
        String warningText = ApplicationProvider.getApplicationContext().getString(R.string.unanswered_questions, String.valueOf(questionAmount));
        onView(withText(warningText)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void testUnfinishedWarningDialogAccepted() {
        //click finish button
        onView(withId(R.id.finishExamButton)).perform(click());
        //there are unanswered questions, dialog should pop
        onView(withText(R.string.confirm_finish_exam)).inRoot(isDialog()).check(matches(isDisplayed()));
        //press ok, meaning finish anyways
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //result view should be visible, countdown should not
        onView(withId(R.id.remainingTimeLayout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.examResultLayout)).check(matches(isDisplayed()));
    }

    @Test
    public void testUnfinishedWarningDialogDenied() {
        //click finish button
        onView(withId(R.id.finishExamButton)).perform(click());
        //there are unanswered questions, dialog should pop
        onView(withText(R.string.confirm_finish_exam)).inRoot(isDialog()).check(matches(isDisplayed()));
        //press ok, meaning finish anyways
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //countdown view should be visible, result should not
        onView(withId(R.id.examResultLayout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.remainingTimeLayout)).check(matches(isDisplayed()));
    }

    /*
    --------------------------------------------------------------------------------------------------------------
    Individual question tests. These tests disable the confirm finish dialog, so there is no need to
    handle that in each test. The exam will simply finish. The confirm finish dialog is tested above.
    Since shuffling is disabled for tests, we know exactly which question is at what position.

    The exam activity makes sure that each question view has a unique id. These can be accessed from the
    questionViewIds list, which is filled in @Before method.

    Not using 'isDisplayed' most of the time because it is possible that 'srollTo' method won't scroll that far
    down and the view wont be displayed, despite actually being there (scrollTo scroll to the TOP of the view).
    Instead using withEffectiveVisibility=VISIBLE.

    These tests must start with 'testQuestion...'!
    --------------------------------------------------------------------------------------------------------------
     */

    //question view id's extracted from the activity
    public List<Integer> questionViewIds;

    //text question with no modifiers: correct answer
    @Test
    public void testQuestionTextNoIgnoreSpaceNoIgnoreCaseCorrect() {
        //index of this question
        int index = 0;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("hello"));
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get green background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.correct_answer_background))));
    }

    //text question with no modifiers: incorrect answer
    @Test
    public void testQuestionTextNoIgnoreSpaceNoIgnoreCaseIncorrect() {
        //index of this question
        int index = 0;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("not hello"));
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get red background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.incorrect_background))));
        //correct solution displayed: not using 'isDisplayed' because it is possible that it wont scroll that far down and it wont be displayed, despite being there
        onView(allOf(withId(R.id.possibleSolutionTextView), withParent(withParent(questionViewMatcher)))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    //text question with ignore case modifier: correct answer
    @Test
    public void testQuestionTextNoIgnoreSpaceIgnoreCaseCorrect() {
        //index of this question
        int index = 1;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("HeLlO")); //correct since ignore case
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get green background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.correct_answer_background))));
    }

    //text question with ignore case modifier: incorrect answer
    @Test
    public void testQuestionTextNoIgnoreSpaceIgnoreCaseIncorrect() {
        //index of this question
        int index = 1;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("not hello"));
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get red background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.incorrect_background))));
        //correct solution displayed: not using 'isDisplayed' because it is possible that it wont scroll that far down and it wont be displayed, despite being there
        onView(allOf(withId(R.id.possibleSolutionTextView), withParent(withParent(questionViewMatcher)))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    //text question with ignore space modifier: correct answer
    @Test
    public void testQuestionTextIgnoreSpaceNoIgnoreCaseCorrect() {
        //index of this question
        int index = 2;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("hello1 world")); //correct since ignore space
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get green background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.correct_answer_background))));
    }

    //text question with ignore space modifier: incorrect answer
    @Test
    public void testQuestionTextIgnoreSpaceNoIgnoreCaseIncorrect() {
        //index of this question
        int index = 2;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("hello 1world")); //dont respect mandatory space between 1 and world, incorrect
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get red background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.incorrect_background))));
        //correct solution?
        onView(allOf(withId(R.id.possibleSolutionTextView), withParent(withParent(questionViewMatcher)))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    //text question with both modifiers: correct answer
    @Test
    public void testQuestionTextIgnoreSpaceIgnoreCaseCorrect() {
        //index of this question
        int index = 3;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("hElLo1 wOrLd")); //correct since ignore case and space
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get green background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.correct_answer_background))));
    }

    //text question with both modifier: incorrect answer
    @Test
    public void testQuestionTextIgnoreSpaceIgnoreCaseIncorrect() {
        //index of this question
        int index = 3;
        //matches the question view
        final Matcher<View> questionViewMatcher = withId(questionViewIds.get(index));
        //does question type match?
        Assert.assertEquals(Question.QuestionType.TEXT, testExamParsed.getQuestions().get(index).getType());
        //scroll there and fill the answer
        onView(questionViewMatcher).perform(scrollTo());
        onView(allOf(withId(R.id.answerEditText), withParent(questionViewMatcher))).perform(typeText("hElLo 1worLD")); //dont respect mandatory space between 1 and world, incorrect
        //finish exam (there wont be confirm dialog, disabled for these tests)
        onView(withId(R.id.finishExamButton)).perform(click());
        //did it get red background?
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(allOf(withId(R.id.answerDisplayerTextView), withParent(questionViewMatcher))).check(matches(withTagValue(is(R.drawable.incorrect_background))));
        //correct solution displayed: not using 'isDisplayed' because it is possible that it wont scroll that far down and it wont be displayed, despite being there
        onView(allOf(withId(R.id.possibleSolutionTextView), withParent(withParent(questionViewMatcher)))).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}