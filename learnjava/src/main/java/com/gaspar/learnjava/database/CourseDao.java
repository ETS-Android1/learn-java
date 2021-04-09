package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Operations for querying and manipulating course data.
 */
@Dao
public interface CourseDao {

    @Insert
    void addCourseStatus(CourseStatus courseStatus);

    @Query("UPDATE course_status SET status = :newStatus WHERE course_id == :courseId")
    void updateCourseStatus(int courseId, @Status int newStatus);

    /**
     * Query the status using the course id.
     */
    @Query("SELECT * FROM course_status WHERE course_id == :courseId")
    CourseStatus queryCourseStatus(int courseId);

    /*
    @Query("SELECT COUNT(*) FROM course_status")
    int countCourses();
    */

    @Query("DELETE FROM course_status")
    void deleteRecords();
}
