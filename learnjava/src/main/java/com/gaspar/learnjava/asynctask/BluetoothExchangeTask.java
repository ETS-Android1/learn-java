package com.gaspar.learnjava.asynctask;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * This background task sends text data to the desktop app, and waits for a confirmation response.
 * If the response does not arrive within a time limit, the exchange is considered failed, otherwise
 * it's a success.
 * <p>
 * When the exchange stops, the user is notified according to the result.
 * <p>
 * This task assumes the user selected Bluetooth mode for clip sync and that the adapter is turned on.
 */
public class BluetoothExchangeTask extends AsyncTask<Void, Void, Boolean> {

    /**
     * Determines how long the async task will attempt to exchange data. After this the task
     * is cancelled and the result is handled in the onCancel callback.
     */
    private static final int TIMEOUT_MILLIS = 5000;
    /**
     * This marks the end of the data exchanged. Should not be used in code samples.
     */
    private static final String DATA_DELIMITER = "DATA_DELIMITER";
    /**
     * The text that will be sent to the desktop app.
     */
    private String data;
    /**
     * This socket can be used to make a connection to the server.
     */
    private final BluetoothSocket socket;
    /**
     * The result of the operation.
     */
    private boolean result;
    /**
     * The view which is used to make Snackbar confirmations to the user. Context leak but could not do it in any other way.
     */
    @SuppressLint("StaticFieldLeak")
    private final View view;

    public BluetoothExchangeTask(@NonNull String data, @NonNull final BluetoothSocket socket, @NonNull View view) {
        this.data = data;
        this.socket = socket;
        this.view = view;
    }

    @Override
    protected void onPreExecute() {
        //start a countdown for timeout
        //not important
        // stop async task if not in progress
        CountDownTimer timer = new CountDownTimer(TIMEOUT_MILLIS, TIMEOUT_MILLIS) {
            public void onTick(long millisUntilFinished) {
            } //not important

            public void onFinish() {
                // stop async task if not in progress
                if (BluetoothExchangeTask.this.getStatus() == Status.RUNNING) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    } finally {
                        result = false;
                    }
                }
            }
        };
        timer.start();
    }

    @Override
    protected Boolean doInBackground(@Size(0) Void... voids) {
        try {
            Log.d("LearnJava", "Attempting to connect to server...");
            socket.connect(); //attempt connecting
            Log.d("LearnJava", "BT connected: " + socket.isConnected());
            //connection is live here
            final OutputStream output = socket.getOutputStream();
            output.write((data+DATA_DELIMITER).getBytes(StandardCharsets.UTF_8)); //send data
            output.flush();
            output.close();
            Log.d("LearnJava", data);
            Log.d("LearnJava", "Message sent!");
            socket.close();
            result = true;
        } catch (IOException | UncheckedIOException e) {
            //failed to connect
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        String message;
        if(result) {
            message = view.getContext().getString(R.string.clip_sync_success);
        } else { //fail
            message = view.getContext().getString(R.string.clip_sync_fail);
        }
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }
}