package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Handles operations on the task status table.
 * @see TaskStatus
 */
@Dao
public interface TaskDao {

    /**
     * Add a new task to the database.
     * @param taskStatus Object describing the task.
     */
    @Insert
    void addTaskStatus(TaskStatus taskStatus);

    /**
     * Update an existing task in the database.
     * @param taskStatus Object describing the task.
     */
    @Update
    void updateTaskStatus(TaskStatus taskStatus);

    /**
     * Fetch the information about a task from the database.
     * @param taskId Id of the task.
     * @return Object describing the task.
     */
    @Query("SELECT * FROM task_status WHERE task_id == :taskId")
    TaskStatus queryTaskStatus(int taskId);

    /**
     * Clears the task table.
     */
    @Query("DELETE FROM task_status")
    void deleteRecords();
}
