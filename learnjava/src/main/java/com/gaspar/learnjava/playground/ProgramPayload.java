package com.gaspar.learnjava.playground;

import androidx.annotation.Nullable;

import com.gaspar.learnjava.database.PlaygroundFile;
import com.squareup.moshi.Json;

import java.util.List;

/**
 * A POJO that is used to convert a Java program, stored as a list of {@link com.gaspar.learnjava.database.PlaygroundFile},
 * into JSON. This program may or may not have input for stdin.
 * @see ProgramResponse
 * @see RunApi
 */
public class ProgramPayload {

    /**
     * Text that is sent to the standard input. This is optional.
     */
    @Nullable
    @Json(name = "stdin")
    public String stdin;

    /**
     * List of {@link PlaygroundFile}s that are sent to compile and run.
     */
    @Json(name = "files")
    public List<PlaygroundFile> files;

}
