package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table in the database which stores chapter completion info. Individual
 * objects can be used to create and update rows in the database.
 * @see ChapterDao
 */
@Entity(tableName = "chapter_status")
public class ChapterStatus {

    /**
     * Id of the chapter.
     */
    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    private int chapterId;

    /**
     * Status of the chapter, can be one of {@link Status} constants.
     */
    @ColumnInfo(name = "status")
    @Status
    private int status;

    ChapterStatus() {}

    /**
     * Creates an object which describes a chapter.
     * @param chapterId Id of the chapter.
     * @param status Status of the chapter.
     */
    @Ignore
    public ChapterStatus(int chapterId, @Status int status) {
        this.chapterId = chapterId;
        this.status = status;
    }

    public int getChapterId() {
        return chapterId;
    }

    void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }
}
