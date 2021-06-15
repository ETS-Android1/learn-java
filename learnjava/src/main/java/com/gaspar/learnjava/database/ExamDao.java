package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaspar.learnjava.curriculum.Status;

import java.util.List;

/**
 * Handles operations on the exam status table.
 * @see ExamStatus
 */
@Dao
public interface ExamDao {

    /**
     * Adds a new exam to the database.
     * @param examStatus Object that describes this exam.
     */
    @Insert
    void addExamStatus(ExamStatus examStatus);

    /**
     * Fetches information about an exam from the database.
     * @param examId The id of the exam.
     * @return Object describing the exam, or null if no exam was found with this id.
     */
    @Query("SELECT * FROM exam_status WHERE exam_id == :examId")
    ExamStatus queryExamStatus(int examId);

    /**
     * Updates the status of an existing exam in the database.
     * @param examId Id of the exam to be updated.
     * @param newStatus New status of the exam, one of {@link Status} constants.
     */
    @Query("UPDATE exam_status SET status = :newStatus WHERE exam_id == :examId")
    void updateExamCompletionStatus(int examId, @Status int newStatus);

    /**
     * Updates the last started attribute of an existing exam in the database.
     * @param examId Id of the exam to be updated.
     * @param lastStarted New last started attribute, as UNIX timestamp.
     */
    @Query("UPDATE exam_status SET last_started = :lastStarted WHERE exam_id == :examId")
    void updateExamLastStarted(int examId, long lastStarted);

    /**
     * Clears all exams from the database.
     */
    @Query("DELETE FROM exam_status")
    void deleteRecords();

    /**
     * Lists all exams from the database.
     * @return Objects describing the exams.
     */
    @Query("SELECT status FROM exam_status")
    List<Integer> getAllExamStatus();

    /**
     * Updates the top score attribute of an existing exam in the database.
     * @param examId Id of the exam to be updated.
     * @param topScore New top score attribute.
     */
    @Query("UPDATE exam_status SET top_score = :topScore WHERE exam_id == :examId")
    void updateTopScore(int examId, int topScore);

    /**
     * Fetches the top score for an exam from the database.
     * @param examId The id of the exam.
     * @return The top score.
     */
    @Query("SELECT top_score FROM exam_status WHERE exam_id == :examId")
    int queryTopScore(int examId);
}
