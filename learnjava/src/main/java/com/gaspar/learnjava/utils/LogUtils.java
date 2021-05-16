package com.gaspar.learnjava.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gaspar.learnjava.LearnJavaActivity;

/**
 * Utility class with logging methods. Logging will only happen
 * in debug modes ({@link com.gaspar.learnjava.LearnJavaActivity#DEBUG}).
 */
public abstract class LogUtils {

    /**
     * Tag that this app uses to log.
     */
    private static final String TAG = "LearnJava";

    /**
     * Logs a message. Only logs in debug mode.
     * @param message The message to be logged.
     */
    public static void log(@NonNull String message) {
        if(LearnJavaActivity.DEBUG) Log.d(TAG, message);
    }

    /**
     * Logs an error message with red. Only logs in debug mode.
     * @param message The message to be logged.
     */
    public static void logError(@NonNull String message) {
        if(LearnJavaActivity.DEBUG) Log.e(TAG, message);
    }

    /**
     * Logs an error message and an exception stack trace with red. Only logs in debug mode.
     * @param message The message to be logged.
     * @param exception The exception which's stack trace is logged.
     */
    public static void logError(@NonNull String message, @NonNull Exception exception) {
        if(LearnJavaActivity.DEBUG) Log.e(TAG, message, exception);
    }
}
