package com.gaspar.learnjava.asynctask;

import android.bluetooth.BluetoothSocket;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.LogUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * This background task sends text data to the desktop app through bluetooth.
 * If a connection can be made and the data is sent the exchange is considered a success.
 * <p>
 * When the exchange stops, the user is notified according to the result.
 * <p>
 * This task assumes the user selected Bluetooth mode for clip sync and that the adapter is turned on.
 */
public class BluetoothExchangeTask extends LjAsyncTask<BluetoothExchangeTask.Result> {

    /**
     * Determines how long the async task will attempt to exchange data. After this the task
     * is cancelled and the result is handled in the onCancel callback.
     */
    static final int TIMEOUT_MILLIS = 10000;

    /**
     * This marks the end of the data exchanged. Should not be used in code samples.
     */
    static final String DATA_DELIMITER = "DATA_DELIMITER";

    /**
     * The text that will be sent to the desktop app.
     */
    private final String data;

    /**
     * This socket can be used to make a connection to the server.
     */
    private final BluetoothSocket socket;

    /**
     * Stores if a timeout has occurred.
     */
    private boolean timedOut;

    /**
     * Stores if the message is a handshake. In this case, we don't care about the response. A handshake
     * is only for the pairing process.
     */
    private final boolean handshakeMessage;

    public BluetoothExchangeTask(@NonNull String data, @NonNull final BluetoothSocket socket) {
        this.data = data;
        this.socket = socket;
        timedOut = false;
        handshakeMessage = data.equals(LearnJavaBluetooth.HANDSHAKE_MESSAGE);
        if(handshakeMessage) {
            LogUtils.log("This is a handshake message!");
        }
    }

    /**
     * Sets up a timer which cancels the task on timeout.
     */
    @Override
    protected void onPreExecute() {
        //start timeout countdown
        CountDownTimer timer = new CountDownTimer(TIMEOUT_MILLIS, TIMEOUT_MILLIS) {
            public void onTick(long millisUntilFinished) { } //not important

            public void onFinish() {
                // stop async task if in progress
                if (BluetoothExchangeTask.this.isRunning()) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    } finally {
                        //save that a timeout has happened
                        timedOut = true;
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * Performs the bluetooth exchange on a background thread.
     * @param objects This is expected to be an {@link AppCompatActivity}.
     * @return The result of the background operation.
     */
    @Override
    protected BluetoothExchangeTask.Result doInBackground(@Size(1) Object... objects) {
        final AppCompatActivity activity = (AppCompatActivity) objects[0];
        activity.runOnUiThread(()-> { //cant do it pre execute, as we don't have activity reference there
            final View loadingIndicator = activity.findViewById(R.id.loadingIndicator); //show loading icon
            if(loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        });
        try {
            LogUtils.log("Attempting to connect to server...");
            socket.connect(); //attempt connecting
            LogUtils.log("BT connected: " + socket.isConnected());
            //connection is live here
            final OutputStream output = socket.getOutputStream();
            output.write((data+DATA_DELIMITER).getBytes(StandardCharsets.UTF_8)); //send data
            output.flush();
            output.close();
            LogUtils.log("Message sent!");
            socket.close();
            return new Result(activity, true);
        } catch (IOException | UncheckedIOException e) {
            LogUtils.logError("Failed to send bluetooth data!", e);
            //failed to connect
            return new Result(activity, false);
        }
    }

    @Override
    protected void onPostExecute(@NonNull final Result result) {
        String message;
        if(result.success && !timedOut) { //if transfer succeeded
            message = result.activity.getString(R.string.clip_sync_success);
        } else if(!result.success && timedOut){ //fail because time out
            message = result.activity.getString(R.string.clip_sync_fail_time_out);
        } else { //other fail
            message = result.activity.getString(R.string.clip_sync_fail);
        }
        final View loadingIndicator = result.activity.findViewById(R.id.loadingIndicator); //hide loading icon
        if(loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
            if(!handshakeMessage) { //only show snackbar if this is a normal message, not a handshake
                Snackbar.make(loadingIndicator, message, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Helper class to send the result of the operation to the main thread.
     */
    static class Result {

        public final AppCompatActivity activity;

        public final boolean success;

        public Result(AppCompatActivity activity, boolean success) {
            this.activity = activity;
            this.success = success;
        }
    }
}