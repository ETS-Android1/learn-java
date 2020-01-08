package com.gaspar.learnjava.database;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.curriculum.Status;

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
     * Saves the course count. When it detects that a new course has been added, the congratulations
     * dialog will show again regardless if the user checked the don't show again checkbox.
     *
     * @param courseCount Count of courses from the database.
     */
    public static void setCourseCount(int courseCount, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!prefs.contains(COURSE_COUNT)) { //first time
            prefs.edit().putInt(COURSE_COUNT, courseCount).apply();
        }
        if(!prefs.contains(CoursesActivity.CONGRATULATION_PROMPT)) { //first time
            prefs.edit().putBoolean(CoursesActivity.CONGRATULATION_PROMPT, true).apply();
        }
        if(prefs.getInt(COURSE_COUNT, 0) != courseCount) { //new course detected
            prefs.edit().putBoolean(CoursesActivity.CONGRATULATION_PROMPT, true).apply();
        }
        CourseStatus.courseCount = new AtomicInteger(courseCount); //update values
        prefs.edit().putInt(COURSE_COUNT, courseCount).apply();
    }

    public static void incrementCourseCount() {
        courseCount.incrementAndGet();
    }
}
