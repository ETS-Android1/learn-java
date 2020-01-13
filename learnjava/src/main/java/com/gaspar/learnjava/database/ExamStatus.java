package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table that stores information about the exams.
 */
@Entity(tableName = "exam_status")
public class ExamStatus {

    @PrimaryKey
    @ColumnInfo(name = "exam_id")
    private int examId;

    @ColumnInfo(name = "status")
    @Status
    private int status;

    /**
     * Stores the last time the exam was started using Epoch time. If this exam was never started
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
