package com.gaspar.learnjava.asynctask;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
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
public class BluetoothExchangeTask extends AsyncTask<Void, Void, Boolean> {

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
     * The activity which is used to make Snackbar confirmations to the user. Context leak but could not do it in any other way.
     */
    @SuppressLint("StaticFieldLeak")
    private final AppCompatActivity activity;

    public BluetoothExchangeTask(@NonNull String data, @NonNull final BluetoothSocket socket, @NonNull AppCompatActivity activity) {
        this.data = data;
        this.socket = socket;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        final View loadingIndicator = activity.findViewById(R.id.loadingIndicator); //show loading icon
        if(loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        //start timeout countdown
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
            message = activity.getString(R.string.clip_sync_success);
        } else { //fail
            message = activity.getString(R.string.clip_sync_fail);
        }
        final View loadingIndicator = activity.findViewById(R.id.loadingIndicator); //hide loading icon
        if(loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
            Snackbar.make(loadingIndicator, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}