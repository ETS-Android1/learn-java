package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface of the operations on the chapter_status table.
 * @see ChapterStatus
 */
@Dao
public interface ChapterDao {

    /**
     * Add a new chapter to the database.
     * @param chapterStatus The object that describes the chapter.
     */
    @Insert
    void addChapterStatus(ChapterStatus chapterStatus);

    /**
     * Update an existing chapter in the database.
     * @param chapterStatus The object that describes the chapter.
     */
    @Update
    void updateChapterStatus(ChapterStatus chapterStatus);

    /**
     * Fetch the details about a chapter from the database.
     * @param chapterId Id of the chapter.
     * @return The object that describes the chapter, or null if there is no chapter with the given id.
     */
    @Query("SELECT * FROM chapter_status WHERE chapter_id == :chapterId")
    ChapterStatus queryChapterStatus(int chapterId);

    /**
     * Lists all chapters from the database.
     * @return The objects that describe the chapters.
     */
    @Query("SELECT * FROM chapter_status")
    List<ChapterStatus> getAllChapterStatuses();

    /**
     * Clears all entries from the chapters table.
     */
    @Query("DELETE FROM chapter_status")
    void deleteRecords();
}
