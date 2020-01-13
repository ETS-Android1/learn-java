package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.asynctask.ExamStatusDisplayerTask;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     Represents an exam in the curriculum. Exams can have any number of questions.
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
     * The challenging difficulty reduced exam time, in milliseconds.
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

    public Exam(int id, List<Question> questions, int questionAmount, int timeLimit) {
        this.id = id;
        this.timeLimit = timeLimit;
        this.questionAmount = questionAmount;
        this.questions = questions;
        status = Status.NOT_QUERIED;
    }

    /**
     * Exam with no question objects, only an ID. Used when displaying the questions isn't necessary.
     */
    public Exam(int id) {
        this.id = id;
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
     * failed exam. This is exactly one day.
     */
    public static final long EXAM_COOL_DOWN_TIME = TimeUnit.DAYS.toSeconds(1);

    /**
     * Checks if the given epoch time was more then a day ago. Accepts exactly 1 day ago as well.
     */
    public static boolean moreThenADayAgo(long lastStarted) {
        long currentTime = (long)(System.currentTimeMillis()/1000.0);
        return currentTime - lastStarted >= EXAM_COOL_DOWN_TIME;
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
     * Finds the id of the course, from the id of its exam.
     */
    public static int findCourseIdForExamId(int examId, Context context) {
        try {
            if(CoursesActivity.coursesNotParsed()) { //not parsed before
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance()
                        .parseCourses(context)); //parse and save courses
            }
            for(Course course: CoursesActivity.getParsedCourses()) {
                if(course.getExam().getId() == examId) return course.getId();
            }
        } catch (Exception e) {
            Log.d("LearnJava", "Exception", e);
        }
        throw new RuntimeException("Invalid exam id!");
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
}
