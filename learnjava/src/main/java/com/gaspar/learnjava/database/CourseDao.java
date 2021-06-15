package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Operations for querying and manipulating course data.
 * @see CourseStatus
 */
@Dao
public interface CourseDao {

    /**
     * Add a new course to the database.
     * @param courseStatus The object that describes the course.
     */
    @Insert
    void addCourseStatus(CourseStatus courseStatus);

    @Query("UPDATE course_status SET status = :newStatus WHERE course_id == :courseId")
    void updateCourseStatus(int courseId, @Status int newStatus);

    /**
     * Query the status of a course using the course id.
     * @param courseId The id.
     * @return Object that describes the course, or null if none was found with the id.
     */
    @Query("SELECT * FROM course_status WHERE course_id == :courseId")
    CourseStatus queryCourseStatus(int courseId);

    /*
    @Query("SELECT COUNT(*) FROM course_status")
    int countCourses();
    */

    /**
     * Clears the course table in the database.
     */
    @Query("DELETE FROM course_status")
    void deleteRecords();
}
