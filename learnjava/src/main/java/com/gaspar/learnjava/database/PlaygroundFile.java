package com.gaspar.learnjava.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

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
    @Json(name = "name")
    private String fileName;

    /**
     * File content, which is unformatted code.
     */
    @NonNull
    @ColumnInfo(name = "content")
    @Json(name = "content")
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

    /**
     * Cleans the content of the file, to remove characters that would not be allowed by the Java compiler.
     * @return The cleaned content.
     */
    public String getCleanedContent() {
        StringBuilder builder = new StringBuilder();
        boolean insideLiteral = false;
        for(char contentChar: content.toCharArray()) {
            if(contentChar == '\"') { //moving in or out of a literal
                insideLiteral = !insideLiteral;
            }
            //found not wanted character outside of literal, ignore
            if(('\t' == contentChar || '\n' == contentChar || '\u00a0' == contentChar) && !insideLiteral) {
                continue;
            }
            //append
            builder.append(contentChar);
        }
        return builder.toString();
    }

    /**
     * Cleans the contents of all files.
     * @param playgroundFiles The files with uncleaned content.
     * @return A new list of files, with cleaned content.
     */
    public static List<PlaygroundFile> createCleanedPlaygroundFiles(@NonNull List<PlaygroundFile> playgroundFiles) {
        List<PlaygroundFile> cleanedFiles = new ArrayList<>();
        for(PlaygroundFile playgroundFile: playgroundFiles) {
            PlaygroundFile cleanedFile = new PlaygroundFile(playgroundFile.fileName, playgroundFile.getCleanedContent());
            cleanedFiles.add(cleanedFile);
        }
        return cleanedFiles;
    }
}
