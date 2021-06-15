package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table where task statuses are stored and can be used to
 * modify this table.
 * @see TaskDao
 */
@Entity(tableName = "task_status")
public class TaskStatus {

    /**
     * Id of the task.
     */
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    private int taskId;

    /**
     * Status of the task.
     */
    @ColumnInfo(name = "status")
    @Status
    private int status;

    public TaskStatus() {}

    /**
     * Create an object which describes a task.
     * @param taskId Id of the task.
     * @param status Status of the task.
     */
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
