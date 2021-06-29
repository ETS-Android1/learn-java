package com.gaspar.learnjava;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.asynctask.LearnJavaExecutor;
import com.gaspar.learnjava.asynctask.LoadExamQuestionsTask;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.questions.Question;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import cn.iwgang.countdownview.CountdownView;

/**
 * The activity that shows an exam and it's questions. This activity doesn't have access to the
 * drawer menu.
 */
public class ExamActivity extends ThemedActivity {

    /**
     * This flag can be used to disable the dialog that pops up when the user attempts to finish
     * the exam. Tests for individual questions do not answer the other questions,
     * and with this flag the unnecessary warning dialog can be bypassed. SHOULD ONLY BE USED WHILE TESTING!
     */
    @VisibleForTesting
    public static boolean disableConfirmFinishWarning;

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
        setContentView(R.layout.activity_exam);
        examFinished = false;
        Bundle extras = getIntent().getExtras();
        if(extras == null) { //should not happen
            LogUtils.logError("Incorrect behaviour: No extras passed!");
            finish();
            return;
        }
        exam = (Exam)extras.getSerializable(Exam.EXAM_PREFERENCE_STRING);
        if(exam == null) {
            LogUtils.logError("Incorrect behaviour: No exam passed in extras!");
            finish();
            return;
        } else {
            setUpUI();
        }
        TICK_ANIMATION = AnimationUtils.loadAnimation(this, R.anim.tick);

        Intent serviceIntent = new Intent(this, ExamSwipeClosedService.class);
        startService(serviceIntent); //start the swipe listener service
    }

    private void setUpUI() {
        new LoadExamQuestionsTask(exam.getId()).execute(this); //this will update the exam variable
        toolbar = findViewById(R.id.toolbarExam);
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

    /**
     * Flag that indicates if the exam was finished.
     */
    private boolean examFinished;

    @Override
    public void onBackPressed() {
        if(examFinished) { //safe to leave
            stopService(new Intent(this, ExamSwipeClosedService.class)); //stop dismiss listening service
            super.onBackPressed();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
            builder.setMessage(R.string.confirm_abandon_exam);
            builder.setIcon(R.drawable.warning_icon);
            builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> {
                //finishes, updates database
                finishExam(findViewById(R.id.finishExamButton), true); //this is a force close
                onBackPressed(); // instantly closes
            }));
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
          if(!disableConfirmFinishWarning) {
              int unansweredQuestions = 0;
              for(Question question: exam.getQuestions()) { //count unanswered questions
                  if(!question.isAnswered()) unansweredQuestions++;
              }
              MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
              builder.setTitle(R.string.confirm_finish_exam);
              builder.setIcon(R.drawable.warning_icon);
              builder.setView(inflateUnansweredWarningView(unansweredQuestions)); //warn about unanswered
              builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> finishExam((Button)view, false))); //this is not a force close
              builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
              builder.create().show();
          } else {
              //the confirm dialog is now disabled, most likely for testing
              finishExam((Button)view, false);
          }
       } else {
           onBackPressed();
       }
    }

    /**
     * Marks this exam as finished. Updates the database. All question views are locked and evaluated.
     * The correct answers are displayed and the incorrect ones are marked with red.
     * @param finishButton The button that closes the exam.
     * @param forceClose If the exam is force closed then the user does not care about the result,
     *                   and the activity can simply be closed without updating the UI.
     */
    private void finishExam(Button finishButton, boolean forceClose) {
        examFinished = true;
        //stop countdown
        ((CountdownView)findViewById(R.id.countdownView)).pause();
        //ui update, only if not force closed
        if(!forceClose) finishButton.setText(R.string.close_exam);
        int correct = 0;
        final RecyclerView questionsView = findViewById(R.id.questionsLayout);
        int i = 0;
        for(Question question: exam.getQuestions()) {
            //the adapter will take care of the rest, it will appear locked and displaying an answer
            question.setDisplayAnswer(true);
            Objects.requireNonNull(questionsView.getAdapter()).notifyItemChanged(i);
            //check if it was correct
            if(question.isCorrect()) correct++;
            i++;
        }
        displayAndUpdateExamResult(correct, forceClose); //show exam result and update database
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Exam.EXAM_PREFERENCE_STRING, exam);
        setResult(Activity.RESULT_OK, resultIntent); //save result intent
    }

    /**
     * Creates the unanswered questions warning view.
     * @param unansweredQuestions The number of unanswered questions.
     */
    @Nullable
    private View inflateUnansweredWarningView(int unansweredQuestions) {
        if(unansweredQuestions == 0) return null;
        View warningView = View.inflate(ExamActivity.this, R.layout.view_unanswered_question_warning, null);
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
     * Displays the result to the user. Updates the status of the exam in the database.
     * If the result is fail, a notification gets posted that will display when the exam is ready to be started again.
     * There is a reason why correctQuestions is double!!! To perform double division.
     * @param correctQuestions The amount of questions correctly answered (points).
     * @param forceClosed If it was a force close, then there is no need to show anything on the UI.
     *                    Database updates must still happen.
     */
    private void displayAndUpdateExamResult(double correctQuestions, boolean forceClosed) {
        LearnJavaExecutor.getInstance().executeOnBackgroundThread(() -> {
            //first launch the top score updating
            int prevScore = LearnJavaDatabase.getInstance(this).getExamDao().queryTopScore(exam.getId());
            if(correctQuestions > prevScore) { //this works for NEVER_STARTED as well, as its value is -1
                LearnJavaDatabase.getInstance(this).getExamDao()
                        .updateTopScore(exam.getId(), Double.valueOf(correctQuestions).intValue());
            }
        });
        double percentageAsDouble = 100 * (correctQuestions/exam.getQuestionAmount());
        int percentage = Double.valueOf(percentageAsDouble).intValue();
        int minPercentage = Exam.getMinimumPassPercentage(this);
        if(percentage >= minPercentage) {
            //update database with pass
            LearnJavaExecutor.getInstance().executeOnBackgroundThread(() -> {
                LearnJavaDatabase.getInstance(ExamActivity.this) //set this exam completed
                        .getExamDao().updateExamCompletionStatus(exam.getId(), Status.COMPLETED);
                Course nextCourse = Course.findNextCourse(exam.getId());
                if(nextCourse != null) { //if there is a next course, unlock it
                    LearnJavaDatabase.getInstance(this).getCourseDao().updateCourseStatus(nextCourse.getId(), Status.UNLOCKED);
                }
            });
        } else {
            //fail, no need to update database, but post notification
            ExamNotificationReceiver.postExamNotification(exam, this); //post the notification that shows on cool down.
        }
        if(!forceClosed) { //UI updates, if this was not a force close
            findViewById(R.id.remainingTimeLayout).setVisibility(View.GONE);
            View resultLayout = findViewById(R.id.examResultLayout);
            if (percentage >= minPercentage) { //pass
                resultLayout.setBackgroundResource(R.drawable.correct_answer_background);
                ((TextView)resultLayout.findViewById(R.id.examResultText)).setText(R.string.exam_passed);

            } else { //fail
                resultLayout.setBackgroundResource(R.drawable.incorrect_background);
                ((TextView)resultLayout.findViewById(R.id.examResultText)).setText(R.string.exam_failed);

            }
            int iCorrectQuestions = Double.valueOf(correctQuestions).intValue();
            String pointsText = iCorrectQuestions + "/" + exam.getQuestions().size() + " " + getString(R.string.points);
            ((TextView)resultLayout.findViewById(R.id.examScoreText)).setText(pointsText);
            ((TextView)resultLayout.findViewById(R.id.examPercentageText)).setText(String.valueOf(percentage).concat("%"));
            resultLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when the countdown timer reaches zero. Shows a dialog that tells the user
     * that the time expired and the exam failed. Closes the activity.
     */
    public void onExamTimeExpired(CountdownView countdownView) {
        countdownView.pause();
        if(notificationVisible) showExamNotification(false); //update notification if it's active
        finishExam(findViewById(R.id.finishExamButton), false); //exam will be locked and corrected
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
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
            if(!notificationVisible) { //only play sound effect when activity is in foreground
                countdownView.playSoundEffect(SoundEffectConstants.CLICK);
            }
            countdownView.startAnimation(TICK_ANIMATION);
        }
    }

    /**
     * Tracks if the notification is visible.
     */
    private boolean notificationVisible;

    /**
     * Request code for ongoing exam notification.
     */
    public static final int NOTIFICATION_REQUEST_CODE = 31278;

    /**
     * The object that posts and cancels notifications.
     */
    private NotificationManager notificationManager;

    /**
     * Posts the ongoing exam warning. (called when the user leaves to the activity while the exam is not yet finished)
     * @param ongoing True if the exam is still active, false if it finished while the user was in another activity.
     */
    private void showExamNotification(boolean ongoing) {
        String title = ongoing ? getString(R.string.ongoing_exam) : getString(R.string.exam_locked);
        String text = ongoing ? getString(R.string.ongoing_exam_detail) : getString(R.string.exam_time_expired);

        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, ExamNotificationReceiver.NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.learn_java_icon_round))
                .setSmallIcon(R.drawable.exam_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, ThemeUtils.getBackgroundColor()))
                .setAutoCancel(true);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setLights(ContextCompat.getColor(this, ThemeUtils.getPrimaryColor()), 500, 500);

        if(notificationManager == null) notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null) { //system service may return null
            LogUtils.logError("Can't exam post notification, system service is null!");
        } else {
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(NOTIFICATION_REQUEST_CODE, notification);
            notificationVisible = true;
        }
    }

    /**
     * Cancels the ongoing exam warning. (called when the user returns to the activity), or when it forcefully
     * stops. while the notification is visible.
     */
    private void cancelExamNotification() {
        notificationVisible = false;
        if(notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Cancels the 'ongoing exam notification' if necessary.
        cancelExamNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Starts the 'ongoing exam notification' if necessary.
        if(!examFinished) showExamNotification(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelExamNotification();
    }

    public boolean isLoadSuccessful() {
        return loadSuccessful;
    }

    public void setLoadSuccessful(boolean loadSuccessful) {
        this.loadSuccessful = loadSuccessful;
    }

    @SuppressWarnings("unused") //might be used later
    public boolean isExamFinished() { return examFinished; }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public Exam getExam() {
        return exam;
    }
}
