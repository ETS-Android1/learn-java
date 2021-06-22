package com.gaspar.learnjava.curriculum.components;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

/**
 * An interface for activities that can have code samples ({@link CodeComponent}, {@link InteractiveComponent})
 * inside them. These activities need to handle ClipSync related tasks. These are
 * <ul>
 *     <li>Provide a way to start a bluetooth enable activity for result: {@link #getBluetoothEnableLauncher()}</li>
 *     <li>Handle {@link androidx.appcompat.app.AppCompatActivity#onRequestPermissionsResult(int, String[], int[])} for result
 *     code {@link com.gaspar.learnjava.ClipSyncActivity#REQUEST_ALLOW_LOCATION}.</li>
 * </ul>
 */
public interface CodeHostingActivity {

    /**
     * Returns an {@link ActivityResultLauncher} that can start a bluetooth enable request, and is
     * prepared to handle the result.
     * @return The launcher.
     */
    ActivityResultLauncher<Intent> getBluetoothEnableLauncher();

}
