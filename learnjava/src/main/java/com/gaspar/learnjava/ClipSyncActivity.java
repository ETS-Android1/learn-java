package com.gaspar.learnjava;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.curriculum.components.CodeComponent;
import com.gaspar.learnjava.curriculum.components.CodeHostingActivity;
import com.gaspar.learnjava.utils.DrawerUtils;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Activity that allows the user to set up and test clip sync. There are two ways for this, bluetooth and
 * local network, but most of the activity's code is about bluetooth as that is much harder to initialize.
 * <p>
 * For a detailed description about what permissions are needed for bluetooth usage, see {@link LearnJavaBluetooth}.
 * @see LearnJavaBluetooth
 */
public class ClipSyncActivity extends ThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener, CodeHostingActivity {

    /**
     * The constant is used to ask the user to allow the app to access location. Needed for bluetooth.
     */
    public static final int REQUEST_ALLOW_LOCATION = 12;

    /**
     * Name of the clip sync status preference.
     */
    public static final String CLIP_SYNC_PREF_NAME = "clip_sync_pref";

    /**
     * Preference object that this activity can use.
     */
    private SharedPreferences prefs;

    /**
     * Discovered devices are stores here to later query them for UUID-s.
     */
    private List<BluetoothDevice> deviceList;

    /**
     * Stores if the app is currently looking for the server.
     */
    private boolean pairingOngoing;

    /**
     * This object listens to bluetooth pairing related events, such as finding discoverable devices,
     * found UUID-s and result of pairing. IF it finds the server then a handshake connection is initiated.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) && pairingOngoing) { //found a device, add to list
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && pairingOngoing) { //finished scan, can start gathering UUID-s
                queryDeviceForUuid();
            } else if (BluetoothDevice.ACTION_UUID.equals(action) && pairingOngoing) { //the UUID-s have been found for a device
                BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert(deviceExtra!=null);
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                if (uuidExtra != null) { //check UUID-s
                    for (Parcelable p : uuidExtra) {
                        ParcelUuid parcelUuid = (ParcelUuid)p;
                        if(parcelUuid.getUuid().equals(LearnJavaBluetooth.SERVER_UUID)) { //Server found!!!
                            pairingDialog.dismiss();
                            //initiate a "handshake" connection, this will do pairing
                            Optional<BluetoothSocket> connResult = LearnJavaBluetooth.getInstance().getServerSocket(deviceExtra);
                            if (connResult.isPresent()) {
                                int result = ContextCompat.checkSelfPermission(ClipSyncActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                                if(result == PackageManager.PERMISSION_GRANTED) {
                                    //it should be granted here, because the pairing is already underway
                                    //send handshake
                                    LearnJavaBluetooth.getInstance().sendHandshakeMessage(connResult.get(), ClipSyncActivity.this);
                                }
                            } else {
                                Snackbar.make(ClipSyncActivity.this.findViewById(R.id.testCodeSample), ClipSyncActivity.this.getString(R.string.clip_sync_misc_error), Snackbar.LENGTH_LONG).show();
                            }
                            return;
                        }
                    }
                }
                queryDeviceForUuid(); //start another query, if possible
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                LogUtils.log("Bond state change broadcast was received!");
                //this event means that the pairing process is updated
                Bundle extras = intent.getExtras();
                if(extras != null) {
                    //get previous paired status and current paired status
                    int prevStatus = extras.getInt(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE);
                    int status = extras.getInt(BluetoothDevice.EXTRA_BOND_STATE);
                    //handle case where pairing SUCCEEDED
                    if(prevStatus == BluetoothDevice.BOND_BONDING && status == BluetoothDevice.BOND_BONDED) {
                        pairingOngoing = false;
                        LogUtils.log("Pairing succeeded!");
                        //handle case where pairing SUCCEEDED
                        saveBluetoothClipSync();
                    } else if(prevStatus == BluetoothDevice.BOND_BONDING && status == BluetoothDevice.BOND_NONE) {
                        pairingOngoing = false;
                        //handle case where pairing failed: the system shows a toast, dont have to notify user
                        LogUtils.logError("Pairing failed, not updating anything!");
                    }
                } else {
                    LogUtils.log("Pairing update: no extras, ignoring...");
                }
            }
        }
    };

    /**
     * This dialog is used to indicate the bluetooth pairing process.
     */
    private KProgressHUD pairingDialog;

    /**
     * This object can start bluetooth enable requests, and is prepared to handle the result.
     */
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_sync);
        prefs = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // Register for broadcasts when a device is discovered.
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_UUID);
        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter1);
        registerReceiver(receiver, filter2);
        registerReceiver(receiver, filter3);
        registerReceiver(receiver, filter4);
        deviceList = new ArrayList<>();
        setUpUi();
        pairingDialog = KProgressHUD.create(ClipSyncActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.clip_sync_pairing))
                .setDetailsLabel(getString(R.string.clip_sync_pairing_info))
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        pairingOngoing = false;
        //register what to happen when a bluetooth request is finished
        bluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) { //the user chose to turn on bluetooth
                        LearnJavaBluetooth.getInstance().turnOnBluetooth();
                        //check if the desktop app is paired
                        if (LearnJavaBluetooth.getInstance().queryPairedDevices().isPresent()) { //desktop app is paired!
                            saveBluetoothClipSync();
                        } else { //the desktop app is not yet discovered
                            handleDevicePairing();
                        }
                    } else { //complain
                        Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.clip_sync_bluetooth_cancelled),
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void setUpUi() {
        //set the text of the status displayer
        final TextView statusDisplayer = findViewById(R.id.clipSyncStatusDisplayer);
        switch (prefs.getInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.NOT_SELECTED)) {
            case ClipSyncMode.BLUETOOTH:
                statusDisplayer.setText(getString(R.string.clip_sync_bluetooth_selected));
                findViewById(R.id.deselectButton).setVisibility(View.VISIBLE);
                break;
            case ClipSyncMode.NETWORK:
                statusDisplayer.setText(getString(R.string.clip_sync_network_selected));
                findViewById(R.id.deselectButton).setVisibility(View.VISIBLE);
                break;
            default:
                statusDisplayer.setText(getString(R.string.clip_sync_mode_not_selected));
        }
        //set the code sample
        String data = getString(R.string.dummy_code_sample);
        final CodeComponent component = new CodeComponent(data); //we can access component methods with this
        final View codeView = findViewById(R.id.testCodeSample);
        final TextView codeArea = codeView.findViewById(R.id.codeArea);
        codeArea.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
        ImageButton zoomIn = codeView.findViewById(R.id.zoomInButton);
        ImageButton zoomOut = codeView.findViewById(R.id.zoomOutButton);
        component.initZoomButtons(zoomIn, zoomOut, codeArea); //zoom functionality
        codeView.findViewById(R.id.copyButton).setOnClickListener(v -> component.copyOnClick(codeArea, this)); //copy functionality

        toolbar = findViewById(R.id.toolbarClipSync);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_clip_sync_root);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Called when the user taps the download link. Warns the user that this should be opened
     * on their computer.
     */
    public void downloadButtonOnClick(@NonNull View view) {
        new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                .setTitle(R.string.get_started)
                .setMessage(R.string.clip_sync_open_on_computer)
                .setPositiveButton(R.string.yes, (dialogInterface, _i) -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getString(R.string.download_link)));
                    startActivity(i);
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    /**
     * Called when the user selects bluetooth sync mode. Checks bluetooth availability, location permission.
     * If all permissions are granted, looks for the desktop app.
     */
    public void bluetoothSelectOnClick(@NonNull View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
        if (!LearnJavaBluetooth.getInstance().bluetoothSupported()) { //no bluetooth support :(
            builder.setMessage(R.string.bluetooth_not_supported);
            builder.setIcon(R.drawable.problem_icon);
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
            return; //nothing more can be done
        }
        //Ask about location permission
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED) { //permission is not given
            MaterialAlertDialogBuilder builder1 = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
            builder1.setMessage(R.string.clip_sync_location);
            builder1.setIcon(R.drawable.bluetooth_icon);
            builder1.setPositiveButton(R.string.ok, (dialog, i) -> {
                dialog.dismiss();
                //ask for permission, result is handled in the activity's onRequestPermissionResult
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ClipSyncActivity.REQUEST_ALLOW_LOCATION);
            });
            builder1.setNegativeButton(R.string.cancel, (dialog, i) -> {
                dialog.dismiss();
                Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_SHORT).show();
            });
            builder1.create().show();
        } else { //permissions are in order
            //check if bluetooth is on or not
            if(LearnJavaBluetooth.getInstance().bluetoothOn()) { //bluetooth app is on, check pairing
                if (LearnJavaBluetooth.getInstance().queryPairedDevices().isPresent()) { //desktop app is paired!
                    saveBluetoothClipSync(); //all good, save bluetooth sync mode
                } else { //the desktop app is not yet discovered
                    handleDevicePairing();
                }
            } else { //bluetooth is off
                //ask the user about turning on bluetooth. Update preferences only if he allows it.
                //see the onActivityResultMethod.
                Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                bluetoothEnableLauncher.launch(intentBtEnabled);
            }
        }
    }

    /**
     * Called when the user selects network sync mode.
     */
    public void networkSelectOnClick(@NonNull View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
        builder.setMessage(R.string.clip_sync_remember_description);
        builder.setIcon(R.drawable.local_network_icon);
        builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> {
            dialogInterface.dismiss();
            Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.using_network_clip_sync), Snackbar.LENGTH_SHORT).show();
        }));
        builder.create().show();
        prefs.edit().putInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.NETWORK).apply();
        final TextView statusDisplayer = findViewById(R.id.clipSyncStatusDisplayer);
        statusDisplayer.setText(getString(R.string.clip_sync_network_selected));
        findViewById(R.id.deselectButton).setVisibility(View.VISIBLE);
    }

    /**
     * Called when the user disables the clip sync functionality.
     */
    public void clipSyncDisabledOnClick(@NonNull final View view) {
        prefs.edit().putInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.NOT_SELECTED).apply(); //update preference
        final TextView statusDisplayer = findViewById(R.id.clipSyncStatusDisplayer); //update displayer
        statusDisplayer.setText(getString(R.string.clip_sync_mode_not_selected));
        findViewById(R.id.deselectButton).setVisibility(View.GONE);
        Snackbar.make(view, getString(R.string.clip_sync_disabled), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_clip_sync_root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerUtils.handleDrawerOnClick(this, item, R.id.drawer_layout_clip_sync_root);
        return true;
    }

    /**
     * The possible statuses of the clip sync.
     */
    @IntDef
    public @interface ClipSyncMode {
        int NOT_SELECTED = 0;
        int BLUETOOTH = 1;
        int NETWORK = 2;
    }

    /**
     * Shows notification to user about the need for pairing. If the user accepts, the pairing will begin.
     */
    private void handleDevicePairing() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
        builder.setMessage(R.string.clip_sync_pairing_description);
        builder.setIcon(R.drawable.bluetooth_icon);
        builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> {
            dialogInterface.dismiss();
            pairingDialog.show(); //show indeterminate progress dialog
            pairingOngoing = true;
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //the result of this is handled in the RECEIVER, or in the countdown timer below, if it times out
                LearnJavaBluetooth.getInstance().initiateDiscoveryProcess();
            }
        }));
        builder.create().show();
    }

    /**
     * Query one of the devices in {@link #deviceList} for its UUID-s. If there are no more devices,
     * that means that the pairing process has failed.
     */
    private void queryDeviceForUuid() {
        if (!deviceList.isEmpty()) { //query another device for UUID-s
            BluetoothDevice device = deviceList.remove(0);
            pairingDialog.setDetailsLabel(getString(R.string.scanning_device) + device.getName());
            device.fetchUuidsWithSdp();
        } else { //no success in finding the server
            pairingOngoing = false;
            if(pairingDialog != null) pairingDialog.dismiss();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
            builder.setMessage(R.string.clip_sync_misc_error);
            builder.setIcon(R.drawable.problem_icon);
            builder.setPositiveButton(R.string.ok, (dialog, i) -> dialog.dismiss());
            builder.create().show();
        }
    }

    /**
     * This is the result of the user deciding to allow location permission or not.
     * @param requestCode The identifier of this request.
     * @param permissions Array of requested permissions, only contains locations.
     * @param grantResults Array of user decisions, only the location decision here.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_ALLOW_LOCATION) {
            //the user denied location permission
            if(grantResults.length==0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_SHORT).show();
            } else { //granted, fire bluetooth select event again
                bluetoothSelectOnClick(findViewById(R.id.bluetoothIcon));
            }
        }
    }

    /**
     * Displays a dialog that indicates everything is fine with bluetooth clip sync. This is called
     * when the user selects bluetooth clip sync (and every permission is granted, pairing is in order).
     */
    public void saveBluetoothClipSync() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle());
        builder.setMessage(R.string.clip_sync_remember_description);
        builder.setIcon(R.drawable.bluetooth_icon);
        builder.setPositiveButton(R.string.ok, ((dialogInterface, i) -> {
            dialogInterface.dismiss();
            Snackbar.make(findViewById(R.id.testCodeSample), getString(R.string.using_bluetooth_clip_sync), Snackbar.LENGTH_SHORT).show();
        }));
        builder.create().show();
        prefs.edit().putInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.BLUETOOTH).apply();
        final TextView displayer = findViewById(R.id.clipSyncStatusDisplayer);
        displayer.setText(getString(R.string.clip_sync_bluetooth_selected));
        findViewById(R.id.deselectButton).setVisibility(View.VISIBLE);
    }

    /**
     * Adds the ClipSync preference if it does not exist. Should be called at the startup.
     * Also notifies the user if he's using any clip sync method.
     */
    @WorkerThread
    public static void initClipSync(@NonNull final Context context, @NonNull final View view) {
        final SharedPreferences prefs = context.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!prefs.contains(CLIP_SYNC_PREF_NAME)) { //first start, put in not selected
            prefs.edit().putInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.NOT_SELECTED).apply();
        } else { //check preference
            @ClipSyncMode int mode = prefs.getInt(CLIP_SYNC_PREF_NAME, ClipSyncMode.NOT_SELECTED);
            if(mode == ClipSyncMode.BLUETOOTH) {
                Snackbar.make(view, context.getString(R.string.using_bluetooth_clip_sync), Snackbar.LENGTH_SHORT).show();
            } else if(mode == ClipSyncMode.NETWORK) {
                Snackbar.make(view, context.getString(R.string.using_network_clip_sync), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResultLauncher<Intent> getBluetoothEnableLauncher() {
        return bluetoothEnableLauncher;
    }

    /*
    public boolean isPairingNotOngoing() {
        return !pairingOngoing;
    }
    */
}
