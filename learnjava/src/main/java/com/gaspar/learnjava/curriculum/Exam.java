package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.ExamActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.UpdatableActivity;
import com.gaspar.learnjava.asynctask.ExamStatusDisplayerTask;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     Represents an exam in the curriculum. Exams can have any number of questions. Exams can be finished or
 *     unfinished. An unfinished courses exam should be unfinished. Unfinished exams show to the user that they
 *     are unfinished, and they can't be started.
 * </p>
 *
 * <p>
 *     Exams are stored in XML, in the following format: exam_id.xml
 * </p>
 *
 * {@code
 *  <resources>
 *  *     <examdata>
 *  *         <id>*id here*</id>
 *            <questionAmount>*number of displayed questions</questionAmount>
 *            <timeLimit>*exam time limit in minutes*</timeLimit>
 *            <finished>true</finished>
 *  *     </examdata>
 *  *     <question>*question data*</question>
 *  *     ...
 *  *     <question>*question data*</question>
 *  </resources>
 * }
 */
public class Exam implements Serializable {

    /**
     * Used when passing exams between activities.
     */
    public static final String EXAM_PREFERENCE_STRING = "exam_preference_string";

    /**
     * A constant that indicated that an exam wasn't started. This is used to store info
     * about exams in the database.
     */
    public static final int EXAM_NEVER_STARTED = -1;

    /**
     * The amount of time challenging difficulty reduces exam time, in milliseconds.
     */
    public static final long REDUCED_EXAM_TIME = 10 * 1000 * 60;

    /**
     * The minimum percentage required to pass an exam.
     */
    private static int minimumPassPercentage;

    /**
     * Id of the exam.
     */
    private int id;

    /**
     * <p>
     *     The number of displayed questions. Initially all questions are parsed, but only this much
     *     randomly selected ones will be displayed in the {@link com.gaspar.learnjava.ExamActivity}.
     * </p>
     * <p>
     *     Since each question is worth 1 point, this is also the amount of the maximum score that can be
     *     reached on this exam.
     * </p>
     */
    private int questionAmount;

    /**
     * Stores the time limit (in MINUTES) for this exam.
     */
    private int timeLimit;

    /**
     * The questions of this exam.
     */
    private transient List<Question> questions;

    /**
     * Status of the exam. Can be locked, unlocked on completed.
     */
    @Status
    private volatile int status;

    /**
     * Stores if this exam is finished. Should be the same value as its course.
     */
    private final boolean finished;

    public Exam(int id, List<Question> questions, int questionAmount, int timeLimit, boolean finished) {
        this.id = id;
        this.timeLimit = timeLimit;
        this.questionAmount = questionAmount;
        this.questions = questions;
        this.finished = finished;
        status = Status.NOT_QUERIED;
    }

    /**
     * Exam with no question objects, only an ID. Used when displaying the questions isn't necessary.
     */
    public Exam(int id, boolean finished) {
        this.id = id;
        this.finished = finished;
        status = Status.NOT_QUERIED;
    }

    /**
     * Queries the status and last started time of an exam from the database, on a background thread.
     *
     * @param examView The view (inflated exam_selector_view) where the result of the query is shown.
     */
    public void queryAndDisplayStatus(View examView, AppCompatActivity activity) {
        new ExamStatusDisplayerTask(this).execute(examView, activity);
    }

    /**
     * A constant that stores the number of seconds the user must wait before they can retry a
     * failed exam.
     */
    public static final long EXAM_COOL_DOWN_TIME = TimeUnit.HOURS.toSeconds(5);

    /**
     * Checks if the given epoch time was more then {@link #EXAM_COOL_DOWN_TIME} ago.
     */
    public static boolean coolDownTimeAgo(long lastStarted) {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastStarted >= (1000*EXAM_COOL_DOWN_TIME);
    }

    /**
     * Checks if there is a exam in the database with the given id. If not it adds this exam
     * to the database with default status.
     */
    @WorkerThread
    public static void validateExamStatus(int examId, Context context) {
        ExamStatus status = LearnJavaDatabase.getInstance(context).getExamDao().queryExamStatus(examId);
        if(status == null) { //not found in the database
            @Status final int DEF_STATUS = Status.LOCKED;
            //both last_started and top_score are marked with exam never started
            ExamStatus newStatus = new ExamStatus(examId, DEF_STATUS, EXAM_NEVER_STARTED, EXAM_NEVER_STARTED);
            LearnJavaDatabase.getInstance(context).getExamDao().addExamStatus(newStatus); //add to database
        }
    }

    /**
     * Starts an exam activity. This is called from {@link ExamStatusDisplayerTask}.
     * @param result The result of the exam display task.
     * @param exam The exam that must be started.
     */
    public static void startExamActivity(ExamStatusDisplayerTask.Result result, Exam exam) {
        LearnJavaDatabase.DB_EXECUTOR.execute(() -> { //register current epoch in database
            LearnJavaDatabase.getInstance(result.activity).getExamDao()
                    .updateExamLastStarted(exam.getId(), System.currentTimeMillis());
        });
        Intent intent = new Intent(result.activity, ExamActivity.class);
        intent.putExtra(Exam.EXAM_PREFERENCE_STRING, exam); //pass exam
        if(result.activity instanceof UpdatableActivity) {
            ((UpdatableActivity)result.activity).setUpdateViews(result.examView); //save update view
        }
        result.activity.startActivityForResult(intent, CoursesActivity.EXAM_REQUEST_CODE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return id == exam.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }

    public int getQuestionAmount() {
        return questionAmount;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public static int getMinimumPassPercentage(Context context) {
        if(minimumPassPercentage == 0) {
            final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
            @SettingsActivity.Difficulties String currentDifficulty = prefs.getString
                    (SettingsActivity.DIFFICULTY_PREF_NAME, SettingsActivity.Difficulties.DEFAULT);
            switch (currentDifficulty) { //set status according to preferences
                case SettingsActivity.Difficulties.CHALLENGING:
                    minimumPassPercentage = 80;
                    break;
                case SettingsActivity.Difficulties.DEFAULT:
                    minimumPassPercentage = 60;
                    break;
                case SettingsActivity.Difficulties.EASY:
                   minimumPassPercentage = 50;
                    break;
            }
        }
        return minimumPassPercentage;
    }

    public static void setMinimumPassPercentage(int minimumPassPercentage) {
        Exam.minimumPassPercentage = minimumPassPercentage;
    }

    public boolean isFinished() {
        return finished;
    }
}
