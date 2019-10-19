package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Handles operations on the task status table.
 */
@Dao
public interface TaskDao {

    @Insert
    void addTaskStatus(TaskStatus taskStatus);

    @Update
    void updateTaskStatus(TaskStatus taskStatus);

    @Query("SELECT * FROM task_status WHERE task_id == :taskId")
    TaskStatus queryTaskStatus(int taskId);

    @Query("DELETE FROM task_status")
    void deleteRecords();
}
