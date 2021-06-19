package com.gaspar.learnjava.utils;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gaspar.learnjava.ClipSyncActivity;
import com.gaspar.learnjava.ContactActivity;
import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.ExamsActivity;
import com.gaspar.learnjava.GuideActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.TasksActivity;
import com.gaspar.learnjava.playground.PlaygroundActivity;

/**
 * This class contains utility methods related to the drawer menu on the
 * left side of the activities. This is to avoid duplicate code such as the
 * {@link com.gaspar.learnjava.LearnJavaActivity#onNavigationItemSelected(MenuItem)} in every activity.
 */
public abstract class DrawerUtils {

    /**
     * Starts an activity depending on which menu item was pressed in the drawer.
     * @param activity The current activity.
     * @param item The pressed menu item.
     * @param drawerLayoutId The resource id of the drawer layout. This differs in activities.
     */
    public static void handleDrawerOnClick(@NonNull final AppCompatActivity activity, @NonNull final MenuItem item, @IdRes int drawerLayoutId) {
        int id = item.getItemId(); // Handle navigation view item clicks here.
        Intent intent = null;
        if (id == R.id.nav_courses) {
            intent = new Intent(activity, CoursesActivity.class);
        } else if (id == R.id.nav_tasks) {
            intent = new Intent(activity, TasksActivity.class);
        } else if (id == R.id.nav_exams) {
            intent = new Intent(activity, ExamsActivity.class);
        } else if (id == R.id.nav_guide) {
            intent = new Intent(activity, GuideActivity.class);
        } else if (id == R.id.nav_contact) {
            intent = new Intent(activity, ContactActivity.class);
        } else if (id == R.id.nav_clipsync) {
            intent = new Intent(activity, ClipSyncActivity.class);
        } else if(id == R.id.nav_playground) {
            intent = new Intent(activity, PlaygroundActivity.class);
        } else {
            LogUtils.logError("Unknown id on drawer click: " + id);
        }
        DrawerLayout drawer = activity.findViewById(drawerLayoutId);
        drawer.closeDrawer(GravityCompat.START);
        if(intent == null) return;
        activity.startActivity(intent); //start selected activity
    }
}
