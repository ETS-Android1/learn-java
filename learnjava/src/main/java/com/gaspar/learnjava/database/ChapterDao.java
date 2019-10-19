package com.gaspar.learnjava.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Interface of the operations on the chapter_status table
 */
@Dao
public interface ChapterDao {

    @Insert
    void addChapterStatus(ChapterStatus chapterStatus);

    @Update
    void updateChapterStatus(ChapterStatus chapterStatus);

    @Query("SELECT * FROM chapter_status WHERE chapter_id == :chapterId")
    ChapterStatus queryChapterStatus(int chapterId);

    @Query("SELECT status FROM chapter_status")
    List<Integer> getAllChapterStatuses();

    @Query("DELETE FROM chapter_status")
    void deleteRecords();
}
