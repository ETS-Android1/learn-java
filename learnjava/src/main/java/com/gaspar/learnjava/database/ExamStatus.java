package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table that stores information about the exams, and can be used to
 * modify this table.
 * @see ExamDao
 */
@Entity(tableName = "exam_status")
public class ExamStatus {

    /**
     * Id of the exam.
     */
    @PrimaryKey
    @ColumnInfo(name = "exam_id")
    private int examId;

    /**
     * Status of the exam.
     */
    @ColumnInfo(name = "status")
    @Status
    private int status;

    /**
     * Stores the last time the exam was started as a UNIX timestamp. If this exam was never started
     * then {@value com.gaspar.learnjava.curriculum.Exam#EXAM_NEVER_STARTED} is stored.
     */
    @ColumnInfo(name = "last_started")
    private long lastStarted;

    /**
     * Stores the value of the users top score on the exam (regardless of failing or succeeding). If this exam was never started
     * then {@value com.gaspar.learnjava.curriculum.Exam#EXAM_NEVER_STARTED} is stored.
     */
    @ColumnInfo(name = "top_score")
    private int topScore;

    ExamStatus() {}

    /**
     * Create an exam status object which describes an exam.
     * @param examId Id of the exam.
     * @param status Status of the exam.
     * @param lastStarted When the exam was last started. If this exam was never started
     * then {@value com.gaspar.learnjava.curriculum.Exam#EXAM_NEVER_STARTED} should be used.
     * @param topScore Top score reached in the exam. If this exam was never started
     * then {@value com.gaspar.learnjava.curriculum.Exam#EXAM_NEVER_STARTED} should be used.
     */
    @Ignore
    public ExamStatus(int examId, @Status int status, long lastStarted, int topScore) {
        this.examId = examId;
        this.status = status;
        this.lastStarted = lastStarted;
        this.topScore = topScore;
    }

    int getExamId() {
        return examId;
    }

    void setExamId(int examId) {
        this.examId = examId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }

    public long getLastStarted() {
        return lastStarted;
    }

    void setLastStarted(long lastStarted) {
        this.lastStarted = lastStarted;
    }

    public int getTopScore() {
        return topScore;
    }

    public void setTopScore(int topScore) {
        this.topScore = topScore;
    }
}
