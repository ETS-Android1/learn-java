package com.gaspar.learnjava.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.asynctask.BluetoothExchangeTask;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A singleton class that handles bluetooth related tasks. These include turning
 * on bluetooth if needed and sending/receiving data.
 */
public class LearnJavaBluetooth {

    /**
     * The unique identifier of the clip sync server.
     */
    public static final UUID SERVER_UUID = new UUID(
            new BigInteger("2e33e9bcc2834347b642bfa1f48cdf72".substring(0, 16), 16).longValue(),
            new BigInteger("2e33e9bcc2834347b642bfa1f48cdf72".substring(16), 16).longValue());
    /**
     * This message is sent to the server to initiate connection. Won't be copied to clipboard.
     */
    public static final String HANDSHAKE_MESSAGE = "handshake";

    private static final LearnJavaBluetooth instance = new LearnJavaBluetooth();

    public static LearnJavaBluetooth getInstance() {
        return instance;
    }

    private final BluetoothAdapter adapter;

    private LearnJavaBluetooth() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Checks if the device has bluetooth. This method is only called when selecting the ClipSync mode,
     * and afterwards the device assumed to support bluetooth.
     * If not, the user won't be able to select the bluetooth mode.
     */
    public boolean bluetoothSupported() {
        return adapter != null;
    }

    /**
     * Turns on the devices bluetooth adapter. It does nothing if the adapter is already on. This turns
     * on bluetooth without any confirmation. If confirmation is needed, the activity must implement that.
     */
    public void turnOnBluetooth() {
        if(!adapter.isEnabled()) adapter.enable();
    }

    /**
     * Checks if the bluetooth is turned on.
     */
    public boolean bluetoothOn() {
        return adapter.isEnabled();
    }

    /**
     * Check the paired device list if the clip sync server is already paired.
     * @return Device object representing the server, if it's paired, empty optional if it's not.
     */
    public Optional<BluetoothDevice> queryPairedDevices() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
               for(ParcelUuid uuid: device.getUuids()) { //find the device of the server
                   if(uuid.getUuid().equals(SERVER_UUID)) return Optional.of(device);
               }
            }
        }
        return Optional.empty();
    }

    /**
     * Starts discovering new devices, hoping to find the clip sync server. Assumes bluetooth is turned on,
     * and that location permission is granted.
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void initiateDiscoveryProcess() {
        adapter.startDiscovery();
    }

    /**
     * Gets the socket used to make a connection. No actual connection is made here.
     * @param server Bluetooth device representing the server.
     * @return The socket representing the connection, if it was established.
     */
    public Optional<BluetoothSocket> getServerSocket(@NonNull final BluetoothDevice server) {
        BluetoothSocket socket;
        try {
            socket = server.createRfcommSocketToServiceRecord(SERVER_UUID);
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(socket);
    }

    /**
     * Sends a string to the desktop app. This will start an async task and is used to
     * interface with the bluetooth adapter in the chapter activities.
     * @param data The string.
     */
    @UiThread
    public void sendData(@NonNull String data, @NonNull BluetoothSocket socket, @NonNull final AppCompatActivity activity) {
        turnOnBluetooth(); //maybe it's turned off
        new BluetoothExchangeTask(data, socket).execute(activity);
    }
}
