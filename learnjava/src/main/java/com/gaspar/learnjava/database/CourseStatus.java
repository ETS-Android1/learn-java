package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
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

    public static void setCourseCount(int courseCount) {
        CourseStatus.courseCount = new AtomicInteger(courseCount);
    }

    public static void incrementCourseCount() {
        courseCount.incrementAndGet();
    }
}
