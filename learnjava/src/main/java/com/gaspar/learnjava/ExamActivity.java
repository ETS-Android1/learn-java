package com.gaspar.learnjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.gaspar.learnjava.asynctask.LoadExamQuestionsTask;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Question;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

import java.util.concurrent.Executors;

import cn.iwgang.countdownview.CountdownView;

/**
 * The activity that shows an exam and it's questions. This activity doesn't have access to the
 * drawer menu.
 */
public class ExamActivity extends ThemedActivity {

    /**
     * Used when loading the questions.
     */
    private volatile boolean loadSuccessful;

    /**
     * The exam object that this activity is displaying. At start this is the passed exam, with no questions.
     * When the loading is finished the parsed exam is assigned.
     */
    private volatile Exam exam;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.exam);
        examFinished = false;
        if(getIntent().getExtras() == null) { //should not happen
            Log.d("LearnJava", "Incorrect behaviour: No extras passed!");
            finish();
        }
        exam = (Exam)getIntent().getExtras().getSerializable(Exam.EXAM_PREFERENCE_STRING);
        if(exam == null) {
            Log.d("LearnJava", "Incorrect behaviour: No exam passed in extras!");
            finish();
        } else {
            setUpUI();
        }
        TICK_ANIMATION = AnimationUtils.loadAnimation(this, R.anim.tick);
    }

    private void setUpUI() {
        new LoadExamQuestionsTask(exam.getId()).execute(this); //this will update the exam variable
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Called when the toolbar back button is pressed. Does the same as the standard back button.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean examFinished;

    @Override
    public void onBackPressed() {
        if(examFinished) { //safe to leave
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExamActivity.this);
            builder.setMessage(R.string.confirm_abandon_exam);
            builder.setIcon(R.drawable.warning_icon);
            builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> super.onBackPressed()));
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        }
    }

    /**
     * Called when the user clicks the 'finish exam' button. After confirmation it shows the correct
     * answers and the result of the exam. Text changes to 'close'.
     */
    public void finishExamOnClick(View view) {
       if(!examFinished) {
           int unansweredQuestions = 0;
           for(Question question: exam.getQuestions()) { //count unanswered questions
               if(!question.isAnswered()) unansweredQuestions++;
           }
           AlertDialog.Builder builder = new AlertDialog.Builder(ExamActivity.this);
           builder.setTitle(R.string.confirm_finish_exam);
           builder.setIcon(R.drawable.warning_icon);
           builder.setView(inflateUnansweredWarningView(unansweredQuestions)); //warn about unanswered
           builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> finishExam((Button)view)));
           builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
           builder.create().show();
       } else {
           onBackPressed();
       }
    }

    private void finishExam(Button finishButton) {
        examFinished = true;
        ((CountdownView)findViewById(R.id.countdownView)).pause(); //stop countdown
        finishButton.setText(R.string.close_exam);
        int correct = 0;
        for(Question question: exam.getQuestions()) {
            question.lockQuestion();
            question.showCorrectAnswer();
            if(question.isCorrect()) correct++;
        }
        displayAndUpdateExamResult(correct); //show exam result
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Exam.EXAM_PREFERENCE_STRING, exam);
        setResult(Activity.RESULT_OK, resultIntent); //save result intent
    }

    /**
     * Creates the unanswered questions warning view.
     *
     * @param unansweredQuestions The number of unanswered questions.
     */
    @Nullable
    private View inflateUnansweredWarningView(int unansweredQuestions) {
        if(unansweredQuestions == 0) return null;
        View warningView = View.inflate(ExamActivity.this, R.layout.unanswered_question_warning, null);
        String text;
        if(unansweredQuestions == 1) {
            text = ExamActivity.this.getString(R.string.unanswered_question);
        } else {
            text = ExamActivity.this.getString(R.string.unanswered_questions, String.valueOf(unansweredQuestions));
        }
        ((TextView)warningView.findViewById(R.id.warningText)).setText(text);
        return warningView;
    }

    /**
     * Displays the result to the user. If the result is a pass than also updates the
     * database. If the result is fail, a notification gets posted that will display when
     * the exam is ready to be started again.
     *
     * @param correctQuestions The amount of questions correctly answered.
     */
    private void displayAndUpdateExamResult(double correctQuestions) {
        int percentage = Double.valueOf((correctQuestions/exam.getQuestions().size()) * 100).intValue();
        findViewById(R.id.remainingTimeLayout).setVisibility(View.GONE);
        View resultLayout = findViewById(R.id.examResultLayout);
        if (percentage >= Exam.getMinimumPassPercentage(this)) {
            resultLayout.setBackgroundResource(R.drawable.correct_answer_background);
            ((TextView)resultLayout.findViewById(R.id.examResultText)).setText(R.string.exam_passed);
            Executors.newSingleThreadExecutor().execute(() -> {
                LearnJavaDatabase.getInstance(ExamActivity.this)
                        .getExamDao().updateExamCompletionStatus(exam.getId(), Status.COMPLETED);
                int courseId = Exam.findCourseIdForExamId(exam.getId(), ExamActivity.this); //update course status too
                LearnJavaDatabase.getInstance(ExamActivity.this).getCourseDao()
                        .updateCourseStatus(new CourseStatus(courseId, Status.COMPLETED));
            });
        } else {
            resultLayout.setBackgroundResource(R.drawable.incorrect_background);
            ((TextView)resultLayout.findViewById(R.id.examResultText)).setText(R.string.exam_failed);
            ExamNotificationReceiver.postExamNotification(exam, this); //post the notification that shows on cool down.
        }
        int iCorrectQuestions = Double.valueOf(correctQuestions).intValue();
        String pointsText = iCorrectQuestions + "/" + exam.getQuestions().size() + " " + getString(R.string.points);
        ((TextView)resultLayout.findViewById(R.id.examScoreText)).setText(pointsText);
        ((TextView)resultLayout.findViewById(R.id.examPercentageText)).setText(String.valueOf(percentage).concat("%"));
        resultLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the countdown timer reaches zero. Shows a dialog that tells the user
     * that the time expired and the exam failed. Closes the activity.
     */
    public void onExamTimeExpired(CountdownView countdownView) {
        countdownView.pause();
        finishExam(findViewById(R.id.finishExamButton)); //exam will be locked and corrected
        AlertDialog.Builder builder = new AlertDialog.Builder(ExamActivity.this);
        builder.setMessage(R.string.exam_time_expired);
        builder.setIcon(R.drawable.problem_icon);
        builder.setPositiveButton(R.string.unfortunate, (dialogIF, i) -> dialogIF.dismiss());
        builder.create().show();
    }

    /**
     * This constant determines the amount of milliseconds under which the countdown timer
     * changes behavior.
     */
    private static final long LOW_REMAINING_TIME = 2 * 1000 * 60;

    /**
     * Tick animation for the countdown view when time is short.
     */
    private Animation TICK_ANIMATION;

    /**
     * Called when the countdown timer ticks. If the remaining time is short a ticking sound is played
     * and the countdown timer is animated.
     */
    public void onExamTimeTicked(CountdownView countdownView, long remainingTime) {
        if(remainingTime < LOW_REMAINING_TIME) { //time is short
            countdownView.playSoundEffect(SoundEffectConstants.CLICK);
            countdownView.startAnimation(TICK_ANIMATION);
        }
    }

    public boolean isLoadSuccessful() {
        return loadSuccessful;
    }

    public void setLoadSuccessful(boolean loadSuccessful) {
        this.loadSuccessful = loadSuccessful;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public Exam getExam() {
        return exam;
    }
}
