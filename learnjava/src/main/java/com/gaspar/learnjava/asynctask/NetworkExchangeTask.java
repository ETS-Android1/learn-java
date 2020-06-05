package com.gaspar.learnjava.asynctask;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
/**
 * This background task sends text data to the desktop app through the local network (using UDP protocol).
 * If broadcasting can be made and the data is sent, and a response is received, the exchange is considered a success.
 * <p>
 * When the exchange stops, the user is notified according to the result.
 */
public class NetworkExchangeTask extends AsyncTask<Void, Void, Boolean> {
    /**
     * The server sends this back to the app after receiving a message.
     */
    private static final String CONFIRMATION_MESSAGE = "rcvd";
    /**
     * The port that the server is using.
     */
    private static final int PORT_NUMBER = 24480;
    /**
     * The text that will be sent to the desktop app.
     */
    private String data;
    /**
     * The socket for data transfer.
     */
    private DatagramSocket socket;
    /**
     * Stores if the network exchange was successful or not.
     */
    private boolean result;
    /**
     * The activity which is used to make Snackbar confirmations to the user. Context leak but could not do it in any other way.
     */
    @SuppressLint("StaticFieldLeak")
    private final AppCompatActivity activity;

    public NetworkExchangeTask(String data, AppCompatActivity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        final View loadingIndicator = activity.findViewById(R.id.loadingIndicator); //show loading icon
        if(loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        //start timeout
        CountDownTimer timer = new CountDownTimer(BluetoothExchangeTask.TIMEOUT_MILLIS, BluetoothExchangeTask.TIMEOUT_MILLIS) {
            public void onTick(long millisUntilFinished) {
            } //not important

            public void onFinish() {
                // stop async task if not in progress
                if (NetworkExchangeTask.this.getStatus() == Status.RUNNING) {
                    socket.close();
                    result = false;
                }
            }
        };
        timer.start();
    }

    @Override
    protected Boolean doInBackground(@Size(0) Void... voids) {
        try {
            socket = new DatagramSocket(PORT_NUMBER);
            socket.setBroadcast(true);
            byte[] buffer = (data+BluetoothExchangeTask.DATA_DELIMITER).getBytes(StandardCharsets.UTF_8);
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            socket.send(new DatagramPacket(buffer, buffer.length, broadcastAddress, PORT_NUMBER));
            //wait for the servers confirmation message
            byte[] responseBuffer = new byte[CONFIRMATION_MESSAGE.length()];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(response); //blocks until response
            String responseString = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
            Log.d("LearnJava", "Received response: " + responseString);
            result = true; //got a response, success
            socket.close();
        } catch (IOException e) {
            result = false; //some kind of fail
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
