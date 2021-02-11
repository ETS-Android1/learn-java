package com.gaspar.learnjava.database;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Status;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an entity which stores data about the status of a course.
 */
@Entity(tableName = "course_status")
public class CourseStatus {

    /**
     * Number of courses registered in the database. Initialized on application start.
     */
    private static AtomicInteger courseCount;

    /**
     * Id of the course.
     */
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    private int courseId;

    /**
     * Status of the course.
     */
    @ColumnInfo(name = "status")
    @Status
    private int status;

    public CourseStatus() {}

    @Ignore
    public CourseStatus(int courseId, @Status int status) {
        this.courseId = courseId;
        this.status = status;
    }

    int getCourseId() {
        return courseId;
    }

    void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }

    public static int getCourseCount() {
        return courseCount.get();
    }

    private static final String COURSE_COUNT = "course_count";

    /**
     * Initiates course counting. When it detects that a new course has been added, the congratulations
     * dialog will show again regardless if the user checked the don't show again checkbox. Also handles if the
     * new course should be unlocked or not (see {@link #newCourseDetected(SharedPreferences, Context)}).
     *
     * @param xmlCourseCount Count of courses detected on application start (from XML).
     */
    @WorkerThread
    public static void initCourseCount(int xmlCourseCount, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!prefs.contains(COURSE_COUNT) || prefs.getInt(COURSE_COUNT,0) == 0) { //create preference
            prefs.edit().putInt(COURSE_COUNT, xmlCourseCount).apply();
        }
        if(!prefs.contains(CoursesActivity.CONGRATULATION_PROMPT)) { //this is handled here, since if course count changes then this must be reactivated
            prefs.edit().putBoolean(CoursesActivity.CONGRATULATION_PROMPT, true).apply();
        }
        //Log.d("LearnJava", "pref: " + prefs.getInt(COURSE_COUNT,0) + " xml: " + xmlCourseCount);
        if(prefs.getInt(COURSE_COUNT, 0) < xmlCourseCount) { //new course detected
            newCourseDetected(prefs, context);
        }
        CourseStatus.courseCount = new AtomicInteger(0); //initiate course counter variable
        prefs.edit().putInt(COURSE_COUNT, xmlCourseCount).apply(); //save counted value to preferences
    }

    /**
     * Called when the application detects a course that was previously not included (after an update).
     * It may need to be already unlocked.
     */
    @WorkerThread
    private static void newCourseDetected(final SharedPreferences prefs, @NonNull final Context context) {
        prefs.edit().putBoolean(CoursesActivity.CONGRATULATION_PROMPT, true).apply(); //re-enable congratulation dialog

        List<Course> courses = CoursesActivity.getParsedCourses();
        boolean unlockNewlyAddedCourse = true; //check if the newly added course must be unlocked in the database or not
        for(int i=0; i < courses.size(); i++) {
            Course course = courses.get(i);
            if(i < courses.size() - 1) { //not the last course
                CourseStatus cs = LearnJavaDatabase.getInstance(context).getCourseDao().queryCourseStatus(course.getId());
                if(cs.getStatus() == Status.LOCKED) {
                    unlockNewlyAddedCourse = false; //if any previous course is locked, the new one will be too
                }
            }
        }
        //its possible for all previous courses to be unlocked, but the last exam may still be uncompleted.
        Exam prevLastExam = courses.get(courses.size() - 2).getExam();
        ExamStatus es = LearnJavaDatabase.getInstance(context).getExamDao().queryExamStatus(prevLastExam.getId());
        if(es.getStatus() != Status.COMPLETED) {
            unlockNewlyAddedCourse = false; //if the last exam is not yet completed, the new course will be locked
        }
        //this is before database validation, new course is not yet in DB. add it here
        CourseStatus cs = new CourseStatus(courses.get(courses.size()-1).getId(), unlockNewlyAddedCourse ?
                Status.UNLOCKED : Status.LOCKED);
        LearnJavaDatabase.getInstance(context).getCourseDao().addCourseStatus(cs);
    }

    public static void incrementCourseCount() {
        courseCount.incrementAndGet();
    }
}
