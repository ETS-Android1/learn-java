package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.TasksActivity;
import com.gaspar.learnjava.adapters.TaskAdapter;
import com.gaspar.learnjava.parsers.CourseParser;

/**
 * Loads the tasks sorted by courses and shows them in the list view. When this process finishes
 * the loading indicator is hidden and the list view is shown.
 */
public class FillTasksActivityTask extends AsyncTask<TasksActivity, Void, TasksActivity> {

    private TaskAdapter taskAdapter;

    @Override
    protected TasksActivity doInBackground(@Size(1) TasksActivity... tasksActivities) {
        TasksActivity activity = tasksActivities[0];
        activity.successfulLoad = true;
        try {
            if(CoursesActivity.coursesNotParsed()) {
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance()
                        .parseCourses(activity)); //parse courses)
            }
            taskAdapter = new TaskAdapter(activity, CoursesActivity.getParsedCourses()); //create adapter
        } catch (Exception e) {
            Log.e("[LearnJava]","Exception", e);
            activity.successfulLoad = false; //save that load failed.
        }
        return activity;
    }

    @Override
    protected void onPostExecute(TasksActivity activity) {
        if(activity.successfulLoad) {
            ListView taskSelectors = activity.findViewById(R.id.taskSelectors);
            taskSelectors.setAdapter(taskAdapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.tasksActivityLayout).setVisibility(View.VISIBLE); //show loaded tasks
        } else { //failed to load.
            //will finish activity when dialog is not showing anymore
            FillCourseActivityTask.showFailDialog(activity, activity.getString(R.string.courses));
        }
    }
}
