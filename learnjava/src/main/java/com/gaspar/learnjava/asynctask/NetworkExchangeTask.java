package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.LogUtils;
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
 * <p>
 * <b>KNOWN PROBLEMS</b>: This does not work for some reason if the device running the app is also the mobile hotspot.
 */
public class NetworkExchangeTask extends LjAsyncTask<BluetoothExchangeTask.Result> {

    /**
     * The server sends this back to the app after receiving a message.
     */
    private static final String CONFIRMATION_MESSAGE = "rcvd";

    /**
     * The port that the server is using.
     */
    private static final int APP_PORT_NUMBER = 24480;

    /**
     * The port that the server will attempt to use.
     */
    public static final int SERVER_PORT_NUMBER = 24481;

    /**
     * The text that will be sent to the desktop app.
     */
    private final String data;

    /**
     * The socket for data transfer.
     */
    private DatagramSocket socket;

    /**
     * Stores if the network exchange timed out.
     */
    private boolean timedOut;

    /**
     * Creates a network exchange task.
     * @param data The data the must be sent.
     */
    public NetworkExchangeTask(@NonNull String data) {
        this.data = data;
        timedOut = false;
    }

    /**
     * Sets up a timer which cancels the task on timeout.
     */
    @Override
    protected void onPreExecute() {
        //start timeout
        CountDownTimer timer = new CountDownTimer(BluetoothExchangeTask.TIMEOUT_MILLIS, BluetoothExchangeTask.TIMEOUT_MILLIS) {
            public void onTick(long millisUntilFinished) { } //not important

            public void onFinish() {
                // stop async task if not in progress
                if (NetworkExchangeTask.this.isRunning()) {
                    socket.close();
                    timedOut = true;
                }
            }
        };
        timer.start();
    }

    /**
     * Finds the IP broadcast address on the current network. The app will send the data there.
     * @param context Context.
     * @return The broadcast address.
     * @throws IOException If this address could not be accessed.
     */
    InetAddress getBroadcastAddress(@NonNull final Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifi == null) {
            LogUtils.logError("Wifi manager is null!");
            return InetAddress.getByName("255.255.255.255");
        }
        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
        if(dhcpInfo == null) {
            LogUtils.logError("D H C P info is null!");
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * Performs the network calls on the background. Waits until response, or timeout.
     * @param objects Expected to be {@link AppCompatActivity}.
     * @return The result.
     */
    @Override
    protected BluetoothExchangeTask.Result doInBackground(@Size(1) Object... objects) {
        final AppCompatActivity activity = (AppCompatActivity) objects[0];
        activity.runOnUiThread(() -> { //indicate loading
            final View loadingIndicator = activity.findViewById(R.id.loadingIndicator); //show loading icon
            if(loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        });
        try {
            socket = new DatagramSocket(APP_PORT_NUMBER);
            socket.setBroadcast(true);
            byte[] buffer = (data+BluetoothExchangeTask.DATA_DELIMITER).getBytes(StandardCharsets.UTF_8);
            InetAddress broadcastAddress = getBroadcastAddress(activity);
            LogUtils.log("Broadcasting on " + broadcastAddress.toString());
            DatagramPacket messageToServer = new DatagramPacket(buffer, buffer.length, broadcastAddress, SERVER_PORT_NUMBER);
            socket.send(messageToServer);

            socket.setBroadcast(false);
            //wait for the servers confirmation message
            LogUtils.log("Waiting for responses...");
            boolean receivedResponse = false;
            while(!receivedResponse) {
                byte[] responseBuffer = new byte[CONFIRMATION_MESSAGE.length()];
                DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(response); //blocks until any response arrives
                if(response.getPort() != SERVER_PORT_NUMBER) {
                    continue; //this message is not from the server
                }
                String responseString = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                LogUtils.log("Received response: " + responseString);
                receivedResponse = true;
            }

            socket.close();
            //return success
            return new BluetoothExchangeTask.Result(activity, true);
        } catch (IOException e) {
            //indicate fail
            return new BluetoothExchangeTask.Result(activity, false);
        }
    }

    /**
     * Shows the result of the task ot the user.
     * @param result The result of the task.
     */
    @Override
    protected void onPostExecute(BluetoothExchangeTask.Result result) {
        String message;
        if(result.success && !timedOut) {
            message = result.activity.getString(R.string.clip_sync_success);
        } else { //fail
            message = result.activity.getString(R.string.clip_sync_fail);
        }
        final View loadingIndicator = result.activity.findViewById(R.id.loadingIndicator); //hide loading icon
        if(loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
            Snackbar.make(loadingIndicator, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
