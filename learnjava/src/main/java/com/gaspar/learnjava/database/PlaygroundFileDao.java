package com.gaspar.learnjava.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Used to modify contents of the playground file table.
 * @see PlaygroundFile
 */
@Dao
public interface PlaygroundFileDao {

    /**
     * Queries all the playground files from the database.
     * @return List of {@link PlaygroundFile}s.
     */
    @Query("SELECT * FROM playground_files")
    List<PlaygroundFile> queryPlaygroundFiles();

    /**
     * Attempts to insert a new playground file into the database. If a file with the given name
     * already exists then the old one will be replaced.
     * @param playgroundFile The new or existing file.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdatePlaygroundFile(@NonNull PlaygroundFile playgroundFile);

    /**
     * Removes a playground file from the database.
     * @param playgroundFile The file to be removed.
     */
    @Delete
    void deletePlaygroundFile(@NonNull PlaygroundFile playgroundFile);

    /**
     * Removes all saved playground files.
     */
    @Query("DELETE FROM playground_files")
    void deleteRecords();

}
