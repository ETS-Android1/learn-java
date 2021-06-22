package com.gaspar.learnjava.playground;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.gaspar.learnjava.ClipSyncActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.ThemedActivity;
import com.gaspar.learnjava.curriculum.components.CodeHostingActivity;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

/**
 * Activity of the playground, where the user can edit and run code samples. This is a tabbed activity, where
 * the 3 main parts are the input (left), code (center) and output (right). This is managed by a
 * {@link ViewPager2} and a {@link TabLayout}.
 */
public class PlaygroundActivity extends ThemedActivity implements CodeHostingActivity {

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
        viewPager.setCurrentItem(PlaygroundTab.TAB_CODE);
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
     * Called when the floating action button, run, was clicked.
     * @param fab The floating action button.
     */
    private void onRunClicked(@NonNull View fab) {
        //TODO
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
}