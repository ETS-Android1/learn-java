package com.gaspar.learnjava.curriculum;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.ClipSyncActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.asynctask.NetworkExchangeTask;
import com.gaspar.learnjava.parsers.RawParser;
import com.gaspar.learnjava.utils.LearnJavaBluetooth;
import com.gaspar.learnjava.utils.ListTagHandler;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents a component of a {@link Chapter} or a {@link Task}.
 *
 * <p>
 * The following components are supported:
 * <ul>
 *     <li>text: normal text</li>
 *     <li>code: code component in black background</li>
 *     <li>advanced: red background, can have title</li>
 *     <li>boxed: same as text, but different background, can have title</li>
 *     <li>list: displays a list of components</li>
 *     <li>image: displays a full width image in with a colored border</li>
 *     <li>title: displays a title text with some vertical margins</li>
 * </ul>
 * </p>
 */
public class Component implements Serializable {

    /**
     * Type of the component.
     */
    @ComponentType
    private int type;

    /**
     * The optional component title. Not all kinds of components can have titles.
     */
    @Nullable
    private String title;

    /**
     * Formatted string that the component's {@link android.view.View} will display.
     */
    protected String data;

    public Component(@ComponentType int type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Creates a view that shows the content of this component. This method does not care about interactive_component
     * components, as those have their separate class, with this method overridden.
     * @param parent The view group this question view will be added. This method DOES NOT add the
     *               inflated view.
     * @return The created view.
     */
    @SuppressLint("SwitchIntDef")
    @UiThread
    public View createComponentView(@NonNull final AppCompatActivity context, ViewGroup parent) {
        View componentView = null;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (type) {
            case ComponentType.ADVANCED: //advanced info
                componentView = inflater.inflate(R.layout.advanced_component, parent, false);
                TextView titleView = componentView.findViewById(R.id.title);
                titleView.setText(context.getString(R.string.advanced).concat(": ")
                        .concat(title == null ? "" : title)); //set title
                TextView advancedArea = componentView.findViewById(R.id.advancedArea); //add formatted code
                advancedArea.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
                advancedArea.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
                if(ThemeUtils.isDarkTheme()) { //some special formatting for dark theme
                    int color = ContextCompat.getColor(context, android.R.color.black);
                    titleView.setTextColor(color);
                    advancedArea.setTextColor(color);
                }
                break;
            case ComponentType.CODE: //code example
                componentView = inflater.inflate(R.layout.code_component, parent, false);
                TextView codeArea = componentView.findViewById(R.id.codeArea); //add formatted code
                codeArea.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
                ImageButton copyButton = componentView.findViewById(R.id.copyButton); //set up copy button
                copyButton.setOnClickListener(view -> copyOnClick(codeArea, context));
                initZoomButtons(componentView); //set up zoom buttons
                break;
            case ComponentType.TEXT: //standard text component
                componentView = inflater.inflate(R.layout.text_component, parent, false);
                TextView textComponent = (TextView) componentView;
                textComponent.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
                textComponent.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
                break;
            case ComponentType.BOXED:
                componentView = inflater.inflate(R.layout.boxed_component, parent, false);
                TextView boxedTitleView = componentView.findViewById(R.id.title);
                boxedTitleView.setText(title == null ? "" : title); //set title
                TextView boxedArea = componentView.findViewById(R.id.boxedArea);
                boxedArea.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
                boxedArea.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT));
                break;
            case ComponentType.LIST:
                componentView = inflater.inflate(R.layout.text_component, parent, false);
                TextView listComponent = (TextView) componentView;
                listComponent.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
                listComponent.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT, null, new ListTagHandler()));
                break;
            case ComponentType.IMAGE:
                componentView = inflater.inflate(R.layout.image_component, parent, false);
                ImageView imageView = componentView.findViewById(R.id.imageView);
                imageView.setImageDrawable(RawParser.parseImage(data, context)); //image name is stored in data
                break;
            case ComponentType.TITLE:
                componentView = inflater.inflate(R.layout.title_component, parent, false);
                ((TextView)componentView.findViewById(R.id.title)).setText(title);
                break;
        }
        return componentView;
    }

    /**
     * Constant used to label the copied code.
     */
    private static final String COPY_LABEL = "Code";

    /**
     * Executed when the user clicks the copy icon under a code example. What happens depends on the clip
     * sync method selected.
     * @param copyFromThis The text view whose content will be copied.
     */
    public static void copyOnClick(@NonNull final TextView copyFromThis, @NonNull final AppCompatActivity activity) {
        ClipboardManager clipboard = (ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard != null) { //copy to local clipboard
            ClipData clip = ClipData.newPlainText(COPY_LABEL, copyFromThis.getText());
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
                            LearnJavaBluetooth.getInstance().sendData(copyFromThis.getText().toString(), serverSocketOpt.get(), activity);
                        } else { //could not obtain socket for some reason
                            Snackbar.make(copyFromThis, activity.getString(R.string.clip_sync_misc_error), Snackbar.LENGTH_LONG).show();
                        }
                    } else { //not paired for some reason
                        Snackbar.make(copyFromThis, activity.getString(R.string.clip_sync_not_paired), Snackbar.LENGTH_LONG).show();
                    }
                } else { //bluetooth is off, ask user to turn on and cancel operation, result is handled in the activity
                    Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    activity.startActivityForResult(intentBtEnabled, ClipSyncActivity.REQUEST_ENABLE_BT);
                }
                break;
            case ClipSyncActivity.ClipSyncMode.NETWORK:
                new NetworkExchangeTask(copyFromThis.getText().toString(), activity).execute();
                break;
            default: //no clip sync, just a general confirmation
                Snackbar.make(copyFromThis, R.string.copy_successful, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * The amount of font size (in pixels) that the zoom buttons increase/decrease.
     */
    protected static final int ZOOM_SIZE_CHANGE = 10;

    /**
     * Adds listeners to the zoom in and zoom out button of code sample component. Also works for interactive component.
     * @param codeSampleView The code sample view.
     */
    @UiThread
    public void initZoomButtons(@NonNull View codeSampleView) {
        ImageButton zoomIn = codeSampleView.findViewById(R.id.zoomInButton);
        ImageButton zoomOut = codeSampleView.findViewById(R.id.zoomOutButton);
        final int minFontSize = (int)codeSampleView.getContext().getResources().getDimension(R.dimen.code_text_size);
        final TextView codeArea = codeSampleView.findViewById(R.id.codeArea);
        zoomIn.setOnClickListener(view ->
                codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, codeArea.getTextSize() + ZOOM_SIZE_CHANGE));
        zoomOut.setOnClickListener(view -> {
            float newSize = codeArea.getTextSize() - ZOOM_SIZE_CHANGE;
            if(newSize < minFontSize) return;
            codeArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        });
    }

    /**
     * Component type constants.
     */
    @IntDef({ComponentType.TEXT, ComponentType.CODE, ComponentType.ADVANCED,
            ComponentType.BOXED, ComponentType.LIST, ComponentType.IMAGE, ComponentType.TITLE, ComponentType.INTERACTIVE})
    public @interface ComponentType {
        int TEXT = 0;
        int CODE = 1;
        int ADVANCED = 2;
        int BOXED = 3; // these components are shown in a rounded, darker box
        int LIST = 4;
        int IMAGE = 5;
        int TITLE = 6;
        int INTERACTIVE = 7;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }
}
