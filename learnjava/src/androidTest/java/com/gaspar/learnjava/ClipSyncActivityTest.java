package com.gaspar.learnjava;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.gaspar.learnjava.asynctask.BluetoothExchangeTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Contains UI tests for {@link ClipSyncActivity}. All tests assume that there are no ClipSync mode 
 * selected at the beginning. Bluetooth is assumed to be turned off. The computer with the ClipSync
 * server is assumed to be not paired.
 * <p>
 * Some tests require the ClipSync server to be up and running on a nearby computer, to test bluetooth
 * and network ClipSync. These tests are marked with {@link org.junit.Ignore}, so the test suite can be
 * safely run without the ClipSync server.
 * <p>
 * Some tests require that the pairing process is already complete, so that actual data sending can be
 * tested. These are marked with {@link org.junit.Ignore}, so the test suite does not depend on this condition.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ClipSyncActivityTest {

    //index of "allow" and "deny" buttons in various system alert dialogs
    private static final int ALLOW_BUTTON_INDEX = 2, DENY_BUTTON_INDEX = 0;

    //starts the activity before each test
    @Rule
    public ActivityScenarioRule<ClipSyncActivity> rule = new ActivityScenarioRule<>(ClipSyncActivity.class);

    //TODO: update this for API 12 (S) to request the new permissions instead, see LearnJavaBluetooth class javadoc
    @Rule
    public GrantPermissionRule locationPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setUp() throws InterruptedException {
        //remove clip sync selection
        AndroidTestUtils.modifySharedPreferenceValue(rule.getScenario(), ClipSyncActivity.CLIP_SYNC_PREF_NAME,
                ClipSyncActivity.ClipSyncMode.NOT_SELECTED);
        //turn off bluetooth
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
            Thread.sleep(500);
        }
    }

    @After
    public void tearDown() { }

    @Test
    public void testClipSyncActivityVisible() {
        onView(withId(R.id.drawer_layout_clip_sync_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testClipSyncDownloadLinkDenied() {
        //press link
        onView(withId(R.id.clipSyncDownloadButton)).perform(scrollTo());
        onView(withId(R.id.clipSyncDownloadButton)).perform(click());
        //dialog
        onView(withText(R.string.clip_sync_open_on_computer)).inRoot(isDialog()).check(matches(isDisplayed()));
        //say no
        onView(withId(AndroidTestUtils.DialogButtonId.NEGATIVE.getId())).perform(click());
        //activity is visible
        onView(withId(R.id.drawer_layout_clip_sync_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testClipSyncDownloadLinkAccepted() throws InterruptedException {
        //press link
        onView(withId(R.id.clipSyncDownloadButton)).perform(scrollTo());
        onView(withId(R.id.clipSyncDownloadButton)).perform(click());
        //dialog
        onView(withText(R.string.clip_sync_open_on_computer)).inRoot(isDialog()).check(matches(isDisplayed()));
        //say yes
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //activity is not visible, wait and press back
        Thread.sleep(2000);
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressBack();
        //should be visible again
        Thread.sleep(1000);
        onView(withId(R.id.drawer_layout_clip_sync_root)).check(matches(isDisplayed()));
    }

    @Test
    public void testClipSyncNotSelected() {
        onView(withText(R.string.clip_sync_mode_not_selected)).perform(scrollTo());
        onView(withText(R.string.clip_sync_mode_not_selected)).check(matches(isDisplayed()));
    }

    /*
    BEFORE starting bluetooth pairing, 2 things must be true: bluetooth must be on and the
    permissions must be granted. The permissions are automatically granted using a rule.
     */

    //deny the request for turning on bluetooth
    @Test
    public void testBluetoothDenyTurnOn() {
        //now click
        onView(withId(R.id.bluetoothIcon)).perform(scrollTo());
        onView(withId(R.id.bluetoothIcon)).perform(click());
        //bluetooth request dialog
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject denyButton = device.findObject(new UiSelector()
                .clickable(true)
                .checkable(false)
                .index(DENY_BUTTON_INDEX));
        if (denyButton.exists()) {
            try {
                denyButton.click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
        //snackbar is showing
        assertFalse(BluetoothAdapter.getDefaultAdapter().isEnabled());
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.clip_sync_bluetooth_cancelled)));
    }

    //allow to turn on bluetooth
    @Test
    public void testBluetoothAllowTurnOn() throws InterruptedException {
        //now click
        onView(withId(R.id.bluetoothIcon)).perform(scrollTo());
        onView(withId(R.id.bluetoothIcon)).perform(click());
        //bluetooth request dialog
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject allowButton = device.findObject(new UiSelector()
                .clickable(true)
                .checkable(false)
                .index(ALLOW_BUTTON_INDEX));
        if (allowButton.exists()) {
            try {
                allowButton.click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
        Thread.sleep(1000);
        assertTrue(BluetoothAdapter.getDefaultAdapter().isEnabled());
    }

    /*
    These tests are for the pairing process. They require the pairing process to not be completed.
    Some of them needs the ClipSync server, others do not.
     */

    //only cares about the information dialog which appears before pairing
    @Test
    public void testPairingInformationDialog() throws InterruptedException {
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            Thread.sleep(1000);
        }
        //both bluetooth and permission is ready
        onView(withId(R.id.bluetoothIcon)).perform(scrollTo());
        onView(withId(R.id.bluetoothIcon)).perform(click());
        //dialog which informs user
        onView(withText(R.string.clip_sync_pairing_description)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    //assumes the clip sync server is NOT PAIRED and NOT AVAILABLE, and the pairing fails
    @Test
    public void testPairingProcessFail() throws InterruptedException {
        //bring up dialog
        testPairingInformationDialog();
        //press ok
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //the pairing process should take some time
        Thread.sleep(15000);
        //a fail dialog is showing
        onView(withText(R.string.clip_sync_misc_error)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
    }

    /*
    These tests are for actual Clip sync data send.
     */

    /*
    THIS TEST ASSUMES THE FOLLOWING:
    - Remote device is running Learn Java Clip Sync server, in Bluetooth mode.
    - Remote device is not paired yet.
    - Remote device is in range.
    - Remote device will accept pairing request when prompted, in about 1-2 seconds.
     */
    @Test
    @Ignore("This test passes, but only when the preconditions listed above are fulfilled.")
    public void testPairingProgress() throws InterruptedException {
        //bring up and validate pairing information dialog
        testPairingInformationDialog();
        //press ok
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //the pairing process should take some time
        Thread.sleep(15000);
        //there is a system pairing dialog, press ok on it
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject allowButton = device.findObject(new UiSelector()
                .clickable(true)
                .checkable(false)
                .index(ALLOW_BUTTON_INDEX));
        if (allowButton.exists()) {
            try {
                //This accepts the pairing for the Android device, but the the remote device that is paired
                //must also accept. This is assumed.
                allowButton.click();
                Thread.sleep(5000);
                //now there is a success dialog
                onView(withText(R.string.clip_sync_remember_description)).inRoot(isDialog()).check(matches(isDisplayed()));
                onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("The system pairing dialog was not found!");
        }
    }

    /*
     THIS TEST ASSUMES THE FOLLOWING:
    - Remote device is running Learn Java Clip Sync server, in Bluetooth mode.
    - Remote device is in range.
    - Remote device is paired.
     */
    @Test
    @Ignore("This test passes, but only when the preconditions listed above are fulfilled.")
    public void testBluetoothClipSync() throws InterruptedException{
        //set bluetooth mode
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            Thread.sleep(1000);
        }
        //both bluetooth and permission is ready
        onView(withId(R.id.bluetoothIcon)).perform(scrollTo(), click());
        //dialog about the app remembering
        onView(withText(R.string.clip_sync_remember_description)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //swipe away snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //click copy button
        onView(withContentDescription(R.string.copy_content)).perform(scrollTo());
        //for some reason, this wait is necessary for the click to work
        Thread.sleep(1000);
        onView(withContentDescription(R.string.copy_content)).perform(click());
        //should be loading
        onView(withId(R.id.loadingIndicator)).check(matches(isDisplayed()));
        //wait for it to complete
        Thread.sleep(1500);
        //there should be a snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.clip_sync_success)));
    }

    /*
    THIS TEST ASSUMES THE FOLLOWING:
    - Remote device is in range.
    - Remote device is paired.
    - The learn java clip sync server cannot accept bluetooth response.
     */
    @Test
    @Ignore("This test passes, but only when the preconditions listed above are fulfilled.")
    public void testClipSyncBluetoothFailed() throws InterruptedException {
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            Thread.sleep(1000);
        }
        //click mode
        onView(withId(R.id.bluetoothIcon)).perform(scrollTo(), click());
        //dialog about the app remembering
        onView(withText(R.string.clip_sync_remember_description)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //swipe away snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //click copy button
        onView(withContentDescription(R.string.copy_content)).perform(scrollTo());
        //for some reason, this wait is necessary for the click to work
        Thread.sleep(1000);
        onView(withContentDescription(R.string.copy_content)).perform(click());
        //wait for fail
        Thread.sleep(200);
        onView(withId(R.id.loadingIndicator)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        //snackbar notifying about fail
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.clip_sync_fail)));
    }

    /*
     THIS TEST ASSUMES THE FOLLOWING:
    - Remote device is running Learn Java Clip Sync server, in network mode.
    - Android device and remote device are on the same local network.
    - Android device is not the WiFi hotspot of this network.
     */
    @Test
    @Ignore("This test passes, but only when the preconditions listed above are fulfilled.")
    public void testClipSyncNetwork() throws InterruptedException {
        //click mode
        onView(withId(R.id.networkIcon)).perform(scrollTo(), click());
        //dialog about the app remembering
        onView(withText(R.string.clip_sync_remember_description)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //swipe away snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //click copy button
        onView(withContentDescription(R.string.copy_content)).perform(scrollTo());
        //for some reason, this wait is necessary for the click to work
        Thread.sleep(1000);
        onView(withContentDescription(R.string.copy_content)).perform(click());
        //wait for it to complete
        Thread.sleep(500);
        //there should be a snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.clip_sync_success)));
    }

    //does not require the clip sync server to respond
    @Test
    public void testClipSyncNetworkTimeout() throws InterruptedException {
        //click mode
        onView(withId(R.id.networkIcon)).perform(scrollTo(), click());
        //dialog about the app remembering
        onView(withText(R.string.clip_sync_remember_description)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(AndroidTestUtils.DialogButtonId.POSITIVE.getId())).perform(click());
        //swipe away snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).perform(swipeRight());
        //click copy button
        onView(withContentDescription(R.string.copy_content)).perform(scrollTo());
        //for some reason, this wait is necessary for the click to work
        Thread.sleep(1000);
        onView(withContentDescription(R.string.copy_content)).perform(click());
        //wait for timeout
        Thread.sleep(200);
        onView(withId(R.id.loadingIndicator)).check(matches(isDisplayed()));
        Thread.sleep(BluetoothExchangeTask.TIMEOUT_MILLIS);
        //snackbar notifying about timeout
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.clip_sync_fail_time_out)));
    }
}