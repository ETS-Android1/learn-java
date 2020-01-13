package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gaspar.learnjava.curriculum.Status;

import java.util.List;

/**
 * Handles operations on the exam status table.
 */
@Dao
public interface ExamDao {

    @Insert
    void addExamStatus(ExamStatus examStatus);

    @Query("SELECT * FROM exam_status WHERE exam_id == :examId")
    ExamStatus queryExamStatus(int examId);

    @Query("UPDATE exam_status SET status = :newStatus WHERE exam_id == :examId")
    void updateExamCompletionStatus(int examId, @Status int newStatus);

    @Query("UPDATE exam_status SET last_started = :lastStarted WHERE exam_id == :examId")
    void updateExamLastStarted(int examId, long lastStarted);

    @Query("DELETE FROM exam_status")
    void deleteRecords();

    @Query("SELECT status FROM exam_status")
    List<Integer> getAllExamStatus();

    @Query("UPDATE exam_status SET top_score = :topScore WHERE exam_id == :examId")
    void updateTopScore(int examId, int topScore);

    @Query("SELECT top_score FROM exam_status WHERE exam_id == :examId")
    int queryTopScore(int examId);
}
