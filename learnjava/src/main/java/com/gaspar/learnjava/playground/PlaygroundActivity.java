package com.gaspar.learnjava.playground;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager2.widget.ViewPager2;

import com.gaspar.learnjava.ClipSyncActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.ThemedActivity;
import com.gaspar.learnjava.asynctask.RunCodeTask;
import com.gaspar.learnjava.curriculum.components.CodeHostingActivity;
import com.gaspar.learnjava.database.PlaygroundFile;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Objects;

import cn.iwgang.countdownview.CountdownView;

/**
 * Activity of the playground, where the user can edit and run code samples. This is a tabbed activity, where
 * the 3 main parts are the input (left), code (center) and output (right). This is managed by a
 * {@link ViewPager2} and a {@link TabLayout}.
 */
public class PlaygroundActivity extends ThemedActivity implements CodeHostingActivity {

    /**
     * Stores if the API calls should be mocked. If this is true the {@link RunApi#compileAndRunCodeMock(ProgramPayload)} will be
     * called instead of {@link RunApi#compileAndRunCode(ProgramPayload, String)}. The API will also be mocked when a non real,
     * mock API key is received (this happens in some build variants).
     */
    public static boolean mockRunApi = false;

    /**
     * The time in milliseconds that must pass between two code runs. If the user attempts to run code
     * faster, a dialog is shown: {@link #showCountdownDialog(long)}.
     */
    private static final long CODE_RUN_INTERVAL = 60000;

    /**
     * X position of the draggable floating action button.
     */
    private float dX;

    /**
     * Y position of the draggable floating action button.
     */
    private float dY;

    /**
     * This object can start bluetooth enable requests, and is prepared to handle the result.
     */
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;

    /**
     * A list of {@link PlaygroundFile}s, which come from the {@link CodeFragment}.
     */
    private List<PlaygroundFile> playgroundFiles;

    /**
     * The input of the program, which comes from the {@link InputFragment}.
     */
    private String input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if this functionality is enabled
        if(getResources().getBoolean(R.bool.playground_enabled)) {
            setContentView(R.layout.activity_playground);
            setUpUi();
            //define what to happen when a bluetooth enable request happens
            bluetoothEnableLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() == RESULT_OK) { //the user chose to turn on bluetooth
                            LearnJavaBluetooth.getInstance().turnOnBluetooth();
                            //show the user that he should try again now
                            Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_bluetooth_try_again),
                                    Snackbar.LENGTH_LONG).show();
                        } else { //complain
                            Snackbar.make(findViewById(R.id.taskComponents), getString(R.string.clip_sync_bluetooth_cancelled),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
            );
            //show info dialog
            showPlaygroundInfoDialogIfNeeded();
        } else {
            //playground is not enabled
            new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                    .setTitle(R.string.playground_unavailable)
                    .setMessage(R.string.playground_unavailable_info)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        PlaygroundActivity.this.finish();
                    })
                    .setCancelable(false)
                    .show();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Initializes the activity's user interface.
     */
    private void setUpUi() {
        //set up toolbar
        toolbar = findViewById(R.id.toolbarPlayground);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //connect TabLayout and ViewPager2
        final TabLayout tabLayout = findViewById(R.id.playgroundTabLayout);
        final ViewPager2 viewPager = findViewById(R.id.playgroundViewPager);
        viewPager.setAdapter(new PlaygroundAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (TabLayout.Tab tab, @PlaygroundTab int position) -> {
            switch (position) {
                case PlaygroundTab.TAB_CODE:
                    tab.setText(R.string.playground_code);
                    break;
                case PlaygroundTab.TAB_INPUT:
                    tab.setText(R.string.playground_input);
                    break;
                case PlaygroundTab.TAB_OUTPUT:
                    tab.setText(R.string.playground_output);
                    break;
            }
        }).attach();
        //start on code tab
        viewPager.setCurrentItem(PlaygroundTab.TAB_CODE, false);
        //disables scrolling. TABS CAN STILL BE USED
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(3);
        //hide loading and show views
        findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        //set up floating action button
        FloatingActionButton runButton = findViewById(R.id.playgroundRunButton);
        runButton.setVisibility(View.VISIBLE);
        setUpFloatingActionButton(runButton);
    }

    /**
     * Initializes the floating action button so it responds to clicks, and becomes draggable.
     * @param fab The floating action button.
     */
    private void setUpFloatingActionButton(@NonNull final FloatingActionButton fab) {
        //add action to perform on short click
        fab.setOnClickListener(this::onRunClicked);
        fab.setFocusable(false);
        fab.setFocusableInTouchMode(false);
        //dragging will begin on long click
        fab.setOnLongClickListener(v -> {
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(fab);
            v.startDragAndDrop(null, myShadow, null, View.DRAG_FLAG_OPAQUE);
            v.setVisibility(View.INVISIBLE);
            return true;
        });
        //dragging on the root
        final View root = findViewById(R.id.playground_content_root);
        root.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    dX = event.getX();
                    dY = event.getY();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //these changes will make the position "permanent"
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
                    params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                    int leftMargin = (int)dX - fab.getWidth()/2;
                    int topMargin = (int)dY - fab.getHeight()/2;
                    params.leftMargin = leftMargin;
                    params.topMargin = topMargin;
                    fab.layout(leftMargin, topMargin, (int)dX + fab.getWidth()/2, (int)dY + fab.getHeight()/2);
                    fab.setVisibility(View.VISIBLE);
                    break;
            }
            return true;
        });
    }

    /**
     * Called when the {@link InputFragment} sends a new input.
     * @param input The input.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String input) {
        this.input = input;
    }

    /**
     * Called when the {@link CodeFragment} sends the list of {@link PlaygroundFile}s.
     * @param playgroundFiles The list of files.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull List<PlaygroundFile> playgroundFiles) {
        this.playgroundFiles = playgroundFiles;
    }

    /**
     * Called when the floating action button, run, was clicked. Takes into account the last time
     * when code was run, {@link #codeLastRunAt()}.
     * @param fab The floating action button.
     */
    private void onRunClicked(@NonNull View fab) {
        //check connectivity
        if(!isNetworkConnected()) {
            new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                    .setMessage(R.string.playground_no_connection)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
            return;
        }
        //check time
        long lastRunAt = codeLastRunAt();
        if(lastRunAt != -1) {
            //there was a code run in the past, check if it was in the interval
            long currentTime = System.currentTimeMillis();
            long timePassed = currentTime - lastRunAt;
            if(timePassed < CODE_RUN_INTERVAL) {
                //the user is attempting to run code too soon
                showCountdownDialog(CODE_RUN_INTERVAL - timePassed);
                return;
            }
        }
        LogUtils.log("Sending program to run:");
        if(input == null) {
            LogUtils.logError("Input was null, not sending input!");
        }
        LogUtils.log("With input:\n" + (input != null ? input : "not sending input"));
        LogUtils.log("With playground files: " + playgroundFiles.toString());
        //create program payload
        ProgramPayload programPayload = new ProgramPayload();
        if(input != null) programPayload.stdin = input;
        programPayload.files = PlaygroundFile.createCleanedPlaygroundFiles(playgroundFiles);
        //log what we send
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<ProgramPayload> programPayloadAdapter = moshi.adapter(ProgramPayload.class);
        LogUtils.log("Sending the following JSON as request body:\n" + programPayloadAdapter.toJson(programPayload));
        //launch task
        fab.setEnabled(false);
        displayLoadingDialog();
        new RunCodeTask(programPayload).execute(this);
    }

    /**
     * Moves the activity's view pager to the output fragment, with a smooth scroll.
     */
    public void moveToOutputFragment() {
        ViewPager2 viewPager = findViewById(R.id.playgroundViewPager);
        viewPager.setCurrentItem(PlaygroundTab.TAB_OUTPUT, true);
    }

    /**
     * Sends program output data to the {@link OutputFragment}, which will display this.
     * @param programResponse The program response, containing the stdout, stderr and exceptions.
     */
    public void sendDataToOutputFragment(@NonNull ProgramResponse programResponse) {
        EventBus.getDefault().post(programResponse);
    }

    /**
     * Launches {@link com.gaspar.learnjava.SettingsActivity} after the user clicked the settins
     * icon on the toolbar.
     * @param view The settings icon.
     */
    public void settingsOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the back button is clicked.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Called when the back button is clicked on the toolbar.
     * @return True.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResultLauncher<Intent> getBluetoothEnableLauncher() {
        return bluetoothEnableLauncher;
    }

    /**
     * This is the result of the user deciding to allow location permission or not. On rare cases it is possible
     * that the user has to enable location permission in this activity for bluetooth.
     * @param requestCode The identifier of this request.
     * @param permissions Array of requested permissions, only contains locations.
     * @param grantResults Array of user decisions, only the location decision here.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ClipSyncActivity.REQUEST_ALLOW_LOCATION) {
            //the user denied location permission
            if(grantResults.length==0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_location_denied),
                        Snackbar.LENGTH_LONG).show();
            } else { //granted, ask to try again now
                Snackbar.make(findViewById(R.id.chapterComponents), getString(R.string.clip_sync_bluetooth_try_again),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * View of the loading dialog. This is null, if there is no loading dialog.
     */
    @Nullable
    private View loadingDialogView;

    /**
     * The loading dialog. This is null, if the loading dialog is currently not visible.
     */
    @Nullable
    private AlertDialog loadingDialog;

    /**
     * Shows the loading dialog.
     */
    @SuppressLint("InflateParams")
    public void displayLoadingDialog() {
        //null is fine here, this is a dialog root
        loadingDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_playground_in_progress, null, false);
        //build dialog
        loadingDialog = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                .setView(loadingDialogView)
                .create();
        //add listeners
        Button okButton;
        if (loadingDialogView != null) {
            okButton = loadingDialogView.findViewById(R.id.loadingDialogOkButton);
            okButton.setOnClickListener(view -> loadingDialog.dismiss());
        }
        //show
        loadingDialog.show();
    }

    /**
     * Updates the loading dialog, so that it displays a message and an ok button.
     * @param message The message to be displayed.
     */
    public void setLoadingDialogMessage(@NonNull String message) {
        if(loadingDialog == null || loadingDialogView == null) {
            LogUtils.logError("Loading dialog not found when calling setLoadingDialogMessage!");
            return;
        }
        //hide loading and show error layout
        loadingDialogView.findViewById(R.id.loadingDialogProgress).setVisibility(View.GONE);
        loadingDialogView.findViewById(R.id.loadingDialogErrorLayout).setVisibility(View.VISIBLE);
        //set message
        TextView messageTextView = loadingDialogView.findViewById(R.id.loadingDialogMessageTextView);
        messageTextView.setText(message);
    }

    /**
     * Dismisses the loading dialog.
     */
    public void hideLoadingDialog() {
        if(loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Displays the countdown dialog which shows the user how much they have to wait before the next
     * code run is available.
     * @param timeRemaining The time that the user need to wait before running code again, in milliseconds.
     */
    private void showCountdownDialog(long timeRemaining) {
        @SuppressLint("InflateParams") //null is ok, this is a dialog root
        View countdownDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_playground_countdown, null, false);

        final Button okButton = countdownDialogView.findViewById(R.id.playgroundCountdownOkButton);
        final TextView countdownTextView = countdownDialogView.findViewById(R.id.playgroundCountdownTextView);
        final CountdownView countdownView = countdownDialogView.findViewById(R.id.playgroundCountdownView);

        final AlertDialog countdownDialog = new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                .setView(countdownDialogView)
                .setOnCancelListener(dialogInterface -> countdownView.stop())
                .create();
        countdownDialog.setOnShowListener(dialogInterface -> countdownView.start(timeRemaining));

        //when countdown ends, show a different text
        countdownView.setOnCountdownEndListener(cv -> {
            countdownView.stop();
            countdownView.setVisibility(View.GONE);
            countdownTextView.setText(R.string.playground_countdown_over);
        });

        //ok button closes dialog
        okButton.setOnClickListener(view -> {
            countdownView.stop();
            countdownDialog.dismiss();
        });

        countdownDialog.show();
    }

    /**
     * The timestamp when code was last run can be found in the preferences with this key. If there is
     * no value with this key, that means there never was a code run.
     */
    private static final String PLAYGROUND_COUNTDOWN_PREF_NAME = "playground_countdown";

    /**
     * @return The timestamp when the user last run code from the {@link PlaygroundActivity}. If there was
     * no run, -1 is returned.
     */
    private long codeLastRunAt() {
        final SharedPreferences preferences = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(preferences.contains(PLAYGROUND_COUNTDOWN_PREF_NAME)) {
            //there was a code run
            return preferences.getLong(PLAYGROUND_COUNTDOWN_PREF_NAME, -1);
        } else {
            //there never was a code run
            return -1;
        }
    }

    /**
     * Saves a timestamp when code was last run.
     * @param codeRunAt The timestamp.
     */
    public void registerCodeRunTime(long codeRunAt) {
        final SharedPreferences preferences = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(PLAYGROUND_COUNTDOWN_PREF_NAME, codeRunAt).apply();
    }

    /**
     * This key is used to find in the preferences if the playground information dialog should be shown.
     */
    private static final String PLAYGROUND_SHOW_INFO_PREF_NAME = "playground_show_info";

    /**
     * Shows the playground info dialog, if the value {@link #PLAYGROUND_SHOW_INFO_PREF_NAME} in the preferences
     * is true, or not present. IF it is not present, that means this is the first time the activity is started.
     */
    private void showPlaygroundInfoDialogIfNeeded() {
        final SharedPreferences preferences = getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean showDialog;
        if(preferences.contains(PLAYGROUND_SHOW_INFO_PREF_NAME)) {
            //preference found, show value
            showDialog = preferences.getBoolean(PLAYGROUND_SHOW_INFO_PREF_NAME, true);
        } else {
            //preference not found, this is the first time the activity launches, so the dialog is shown
            showDialog = true;
            //save it for later, with true value
            preferences.edit().putBoolean(PLAYGROUND_SHOW_INFO_PREF_NAME, true).apply();
        }
        if(showDialog) { //should show the dialog
            @SuppressLint("InflateParams") //null is ok, this is a dialog root
            View infoView = LayoutInflater.from(this).inflate(R.layout.dialog_playground_info, null, false);
            final MaterialCheckBox infoCheckBox = infoView.findViewById(R.id.playgroundInfoCheckbox);

            new MaterialAlertDialogBuilder(this, ThemeUtils.getThemedDialogStyle())
                    .setView(infoView)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        if(infoCheckBox.isChecked()) {
                            //user clicked dont show again
                            preferences.edit().putBoolean(PLAYGROUND_SHOW_INFO_PREF_NAME, false).apply();
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        }
    }

    /**
     * Checks if the device is connected to the internet.
     * @return True if it is connected.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Handles the event when the floating action button, run, needs to change visibility.
     * @param showHideFab Determines if the button must appear or disappear.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull ShowHideFab showHideFab) {
        LogUtils.log("Floating action button visibility event!");
        FloatingActionButton fab = findViewById(R.id.playgroundRunButton);
        if(showHideFab.show) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    /**
     * Message class.
     * @see #onMessageEvent(ShowHideFab)
     */
    public static class ShowHideFab {
        public boolean show;
    }
}