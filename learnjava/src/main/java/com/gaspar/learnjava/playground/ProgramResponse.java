package com.gaspar.learnjava.playground;

import com.squareup.moshi.Json;

/**
 * A POJO that is used to parse the JSON response from the run API into usable data.
 * @see ProgramPayload
 * @see RunApi
 */
public class ProgramResponse {

    /**
     * What appears on the standard output when the program is running.
     */
    @Json(name = "stdout")
    public String stdout;

    /**
     * What appears on the standard error channel when the program is running.
     */
    @Json(name = "stderr")
    public String stderr;

    /**
     * What exceptions and errors happen when the program being compiled or running.
     */
    @Json(name = "error")
    public String error;

}
