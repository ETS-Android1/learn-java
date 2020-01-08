package com.gaspar.learnjava.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gaspar.learnjava.curriculum.Status;

/**
 * Represents the table in the database which stores chapter completion info.
 */
@Entity(tableName = "chapter_status")
public class ChapterStatus {

    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    private int chapterId;

    @ColumnInfo(name = "status")
    @Status
    private int status;

    ChapterStatus() {}

    @Ignore
    public ChapterStatus(int chapterId, @Status int status) {
        this.chapterId = chapterId;
        this.status = status;
    }

    int getChapterId() {
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
