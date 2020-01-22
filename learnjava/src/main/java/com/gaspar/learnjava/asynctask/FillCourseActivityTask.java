package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.CourseAdapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.ThemeUtils;

/**
 * Parses the courses from XML and fill the list view in the {@link com.gaspar.learnjava.CoursesActivity}
 * with course selector view.
 *
 * On completion it hides the loading indicator in the activity.
 */
public class FillCourseActivityTask extends AsyncTask<CoursesActivity, Void, CoursesActivity> {

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
            Log.e("[LearnJava]","Exception", e);
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

    static void showFailDialog(AppCompatActivity activity, String errorLocation) {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(ThemeUtils.createDialogWrapper(activity));
        String message = activity.getString(R.string.loading_error, errorLocation);
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
