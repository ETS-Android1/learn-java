package com.gaspar.learnjava.curriculum.components;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.ClipSyncActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.asynctask.NetworkExchangeTask;
import com.gaspar.learnjava.utils.AnimationUtils;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.Optional;

/**
 * A {@link Component} that displays a formatted code sample to the user. Provides ways to change the
 * font size of copy the contents. Also interacts with the ClipSync functionality, if that is enabled.
 * <p>
 * The formatted code is stored in {@link #data}.
 */
public class CodeComponent extends Component {

    /**
     * Constructor for the code component.
     * @param data Data displayed by the component, a formatted code sample.
     */
    public CodeComponent(String data) {
        super(ComponentType.CODE, data);
    }

    /**
     * Constructor for subclasses.
     * @param type Component type.
     * @param data Component data, which is formatted code.
     */
    public CodeComponent(int type, String data) {
        super(type, data);
    }

    /**
     * The amount of font size (in pixels) that the zoom buttons increase/decrease.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public static final int ZOOM_SIZE_CHANGE = 10;

    /**
     * Adds listeners to the zoom in and zoom out button of code sample component. All of these parameters
     * are saved in {@link CodeComponentHolder}.
     * @param zoomIn The zoom in button.
     * @param zoomOut The zoom out button.
     * @param codeArea The text view which displays code.
     */
    @UiThread
    public void initZoomButtons(@NonNull ImageButton zoomIn, @NonNull ImageButton zoomOut, @NonNull TextView codeArea) {
        //using value animator for smooth font size change
        zoomIn.setOnClickListener(view -> {
            int currentSize = (int)codeArea.getTextSize();
            final ValueAnimator animator = ValueAnimator.ofInt(currentSize, currentSize + ZOOM_SIZE_CHANGE);
            animator.setDuration(AnimationUtils.DURATION);
            //disable zoom buttons while ongoing
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    zoomIn.setEnabled(false);
                    zoomOut.setEnabled(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    zoomIn.setEnabled(true);
                    zoomOut.setEnabled(true);

                }
            });
            //update text size
            animator.addUpdateListener(pAnimator -> codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)pAnimator.getAnimatedValue()));
            animator.start();
        });
        zoomOut.setOnClickListener(view -> {
            int currentSize = (int)codeArea.getTextSize();
            int newSize = currentSize - ZOOM_SIZE_CHANGE;
            if(newSize <= 0) return;
            final ValueAnimator animator = ValueAnimator.ofInt(currentSize, newSize);
            animator.setDuration(AnimationUtils.DURATION);
            //disable zoom buttons while ongoing
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    zoomIn.setEnabled(false);
                    zoomOut.setEnabled(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    zoomIn.setEnabled(true);
                    zoomOut.setEnabled(true);

                }
            });
            //update text size
            animator.addUpdateListener(pAnimator -> codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)pAnimator.getAnimatedValue()));
            animator.start();
        });
    }

    /**
     * Constant used to label the copied code.
     */
    private static final String COPY_LABEL = "Code";

    /**
     * Executed when the user clicks the copy icon under a code example. What happens depends on the clip
     * sync method selected.
     * @param codeArea The text view whose content will be copied.
     */
    public void copyOnClick(@NonNull final TextView codeArea, @NonNull final AppCompatActivity activity) {
        ClipboardManager clipboard = (ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard != null) { //copy to local clipboard
            ClipData clip = ClipData.newPlainText(COPY_LABEL, codeArea.getText());
            clipboard.setPrimaryClip(clip);
        }
        final SharedPreferences prefs = activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int mode = prefs.getInt(ClipSyncActivity.CLIP_SYNC_PREF_NAME, ClipSyncActivity.ClipSyncMode.NOT_SELECTED);
        switch(mode) {
            case ClipSyncActivity.ClipSyncMode.BLUETOOTH: //using bluetooth clip sync
                if(LearnJavaBluetooth.getInstance().bluetoothOn()) {
                    //bluetooth on, send. the background task will show confirmation when it's done
                    Optional<BluetoothDevice> serverOpt = LearnJavaBluetooth.getInstance().queryPairedDevices();
                    if(serverOpt.isPresent()) {
                        Optional<BluetoothSocket> serverSocketOpt = LearnJavaBluetooth.getInstance().getServerSocket(serverOpt.get());
                        if(serverSocketOpt.isPresent()) {
                            //check if we have location permission for bluetooth
                            int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
                            if(result == PackageManager.PERMISSION_GRANTED) {
                                //all good, bluetooth on, permission granted, we can send
                                LearnJavaBluetooth.getInstance().sendData(codeArea.getText().toString(), serverSocketOpt.get(), activity);
                            } else {
                                //no permission yet, ask for it
                                MaterialAlertDialogBuilder builder1 = new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle());
                                builder1.setMessage(R.string.clip_sync_location);
                                builder1.setIcon(R.drawable.bluetooth_icon);
                                builder1.setPositiveButton(R.string.ok, (dialog, i) -> {
                                    dialog.dismiss();
                                    //ask for permission, result is handled in the activity's onRequestPermissionResult
                                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            ClipSyncActivity.REQUEST_ALLOW_LOCATION);
                                });
                                builder1.setNegativeButton(R.string.cancel, (dialog, i) -> {
                                    dialog.dismiss();
                                    Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.clip_sync_location_denied),
                                            Snackbar.LENGTH_SHORT).show();
                                });
                                builder1.create().show();
                            }
                        } else { //could not obtain socket for some reason
                            Snackbar.make(codeArea, activity.getString(R.string.clip_sync_misc_error), Snackbar.LENGTH_LONG).show();
                        }
                    } else { //not paired for some reason
                        Snackbar.make(codeArea, activity.getString(R.string.clip_sync_not_paired), Snackbar.LENGTH_LONG).show();
                    }
                } else { //bluetooth is off, ask user to turn on and cancel operation, result is handled in the activity
                    Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(intentBtEnabled, ClipSyncActivity.REQUEST_ENABLE_BT);
                }
                break;
            case ClipSyncActivity.ClipSyncMode.NETWORK:
                new NetworkExchangeTask(codeArea.getText().toString()).execute(activity);
                break;
            default: //no clip sync, just a general confirmation
                Snackbar.make(codeArea, R.string.copy_successful, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Functionally equivalent to {@link #getData()}.
     * @return The formatted code that this component displays.
     */
    public String getFormattedCode() {
        return data;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class CodeComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Text view which displays formatted code.
         */
        public TextView codeArea;

        /**
         * Copy button of the code sample.
         */
        public ImageButton copyButton;

        /**
         * Zoom in button.
         */
        public ImageButton zoomIn;

        /**
         * Zoom out button.
         */
        public ImageButton zoomOut;

        /**
         * Creates a view holder for a {@link CodeComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_code.
         */
        public CodeComponentHolder(View componentView) {
            super(componentView);
            codeArea = componentView.findViewById(R.id.codeArea);
            copyButton = componentView.findViewById(R.id.copyButton);
            zoomIn = componentView.findViewById(R.id.zoomInButton);
            zoomOut = componentView.findViewById(R.id.zoomOutButton);
        }

    }
}
