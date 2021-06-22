package com.gaspar.learnjava.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Represents the table which stores files from the playground, and can be used to
 * modify these stored files. This table is updated when {@link com.gaspar.learnjava.playground.PlaygroundActivity}
 * closes.
 * <p>
 * This is not an actual file represented on the Android file system, just a way to group the user's
 * code in the playground.
 * <p>
 * Aside from being used in database transactions, this class is also used in {@link com.gaspar.learnjava.playground.CodeFragment}
 * and as a model to serialize and deserialize JSON.
 * @see PlaygroundFileDao
 */
@Entity(tableName = "playground_files")
public class PlaygroundFile {

    /**
     * File name, must be unique in the table.
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "file_name")
    private String fileName;

    /**
     * File content, which is unformatted code.
     */
    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    @SuppressWarnings("unused")
    public PlaygroundFile() { //required by Room
        fileName = "";
        content = "";
    }

    /**
     * Creates a playground file.
     * @param fileName The file name.
     * @param content The file content.
     */
    @Ignore
    public PlaygroundFile(@NonNull String fileName, @NonNull String content) {
        this.fileName = fileName;
        this.content = content;
    }

    //getters and setters required by room

    @NonNull
    public String getFileName() {
        return fileName;
    }

    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    /**
     * This method is only overridden for logging purposes.
     * @return The file name.
     */
    @Override
    @NonNull
    public String toString() {
        return fileName;
    }
}
