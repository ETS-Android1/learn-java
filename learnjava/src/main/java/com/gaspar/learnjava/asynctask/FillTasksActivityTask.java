package com.gaspar.learnjava.asynctask;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.TasksActivity;
import com.gaspar.learnjava.adapters.TaskAdapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;

/**
 * Loads the tasks sorted by courses and shows them in the list view, inside {@link TasksActivity}. When this process finishes
 * the loading indicator is hidden and the list view is shown.
 */
public class FillTasksActivityTask extends LjAsyncTask<TasksActivity> {

    /**
     * The adapter which displays task views.
     */
    private TaskAdapter taskAdapter;

    /**
     * Gets the courses from XML, if they are not parsed. If already parsed, then just simply
     * reads them from a list. Courses contain some task information, which is enough here.
     * @param objects Excepted to only contain {@link TasksActivity}.
     * @return The same activity.
     */
    @Override
    protected TasksActivity doInBackground(@Size(1) Object... objects) {
        TasksActivity activity = (TasksActivity) objects[0];
        activity.successfulLoad = true;
        try {
            if(CoursesActivity.coursesNotParsed()) {
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance()
                        .parseCourses(activity)); //parse courses
            }
            taskAdapter = new TaskAdapter(activity, CoursesActivity.getParsedCourses()); //create adapter
        } catch (Exception e) {
            LogUtils.logError("Exception while loading tasks!", e);
            activity.successfulLoad = false; //save that load failed.
        }
        return activity;
    }

    /**
     * Sets the created {@link #taskAdapter}, which will display the task views.
     * @param activity The activity.
     */
    @Override
    protected void onPostExecute(TasksActivity activity) {
        if(activity.successfulLoad) {
            ListView taskSelectors = activity.findViewById(R.id.taskSelectors);
            taskSelectors.setAdapter(taskAdapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.tasksActivityLayout).setVisibility(View.VISIBLE); //show loaded tasks
        } else { //failed to load.
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            //will finish activity when dialog is not showing anymore
            LogUtils.showLoadingFailDialog(activity, activity.getString(R.string.courses));
        }
    }
}
