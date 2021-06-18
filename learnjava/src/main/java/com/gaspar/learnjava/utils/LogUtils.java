package com.gaspar.learnjava.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    /**
     * Common functionality of showing a dialog when loading failed, extracted into a utility method.
     * This should only be shown if the failed loading makes the activity unusable. Closes the activity
     * after the dialog is cancelled. Typically failed loading happens because of a malformed chapter, task,
     * exam, etc...
     * @param activity The activity, in which the loading failed.
     */
    public static void showLoadingFailDialog(@NonNull final AppCompatActivity activity, @NonNull String whatFailedToLoad) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle());
        String message = activity.getString(R.string.loading_error, whatFailedToLoad);
        builder.setMessage(message);
        builder.setOnCancelListener(dialogInterface -> {
            dialogInterface.dismiss();
            activity.finish();
        });
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            activity.finish();
        });
        builder.create().show();
    }
}
