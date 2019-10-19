package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table where task statuses are stored.
 */
@Entity(tableName = "task_status")
public class TaskStatus {

    @PrimaryKey
    @ColumnInfo(name = "task_id")
    private int taskId;

    @ColumnInfo(name = "status")
    @Status
    private int status;

    public TaskStatus() {}

    @Ignore
    public TaskStatus(int taskId, @Status int status) {
        this.taskId = taskId;
        this.status = status;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }
}
