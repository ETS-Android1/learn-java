package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.CourseAdapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Parses the courses from XML and fill the list view in the {@link com.gaspar.learnjava.CoursesActivity}
 * with course selector view.
 *
 * On completion it hides the loading indicator in the activity.
 */
public class FillCourseActivityTask extends AsyncTask<CoursesActivity, Void, CoursesActivity> {

    /**
     * The adapter which displays course views.
     */
    private CourseAdapter adapter;

    @Override
    protected CoursesActivity doInBackground(@Size(1) CoursesActivity... coursesActivities) {
        CoursesActivity activity = coursesActivities[0];
        activity.successfulLoad = true; //assume success
        try {
            if(CoursesActivity.coursesNotParsed()) { //not parsed before
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance()
                        .parseCourses(activity)); //parse and save courses
            }
            adapter = new CourseAdapter(activity, CoursesActivity.getParsedCourses());
        } catch (Exception e) {
            LogUtils.logError("Exception while loading course", e);
            activity.successfulLoad = false; //save that load failed.
        }
        return activity;
    }

    @Override
    protected void onPostExecute(CoursesActivity activity) {
        if(activity.successfulLoad) {
            ListView courseSelectors =  activity.findViewById(R.id.courseSelectors);
            courseSelectors.setAdapter(adapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.coursesLayout).setVisibility(View.VISIBLE); //show loaded courses
        } else { //failed to load.
            showFailDialog(activity, activity.getString(R.string.courses)); //will finish activity when dialog is not showing anymore
        }
    }

    /**
     * Shows a dialog which displays an error message to the user if the content could not be loaded.
     * @param activity Activity in which the dialog appears.
     * @param whatFailedToLoad The content that failed to load.
     */
    static void showFailDialog(AppCompatActivity activity, String whatFailedToLoad) {
        androidx.appcompat.app.AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle());
        String message = activity.getString(R.string.loading_error, whatFailedToLoad);
        builder.setMessage(message);
        builder.setOnCancelListener(dialogInterface -> {
           dialogInterface.dismiss();
           activity.finish();
        });
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            activity.finish();
        });
        builder.create().show();
    }
}
