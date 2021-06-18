package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;

/**
 * Queries and sets the status of a {@link com.gaspar.learnjava.curriculum.Task}. Shows the
 * result using the given {@link ImageView}.
 */
public class TaskStatusDisplayerTask extends LjAsyncTask<CourseStatusDisplayerTask.Result> {

    /**
     * The task.
     */
    private final Task task;

    /**
     * Creates a task status query job.
     * @param task The task.
     */
    public TaskStatusDisplayerTask(Task task) {
        this.task = task;
    }

    /**
     * Queries the status on the background.
     * @param objects The first must be the status icon {@link ImageView}. The second must be a {@link Context} object.
     *                The third CAN optionally be a {@link View} which will be auto opened, if needed.
     * @return Result object.
     */
    @Override
    protected CourseStatusDisplayerTask.Result doInBackground(@Size(min = 2, max = 3) Object... objects) {
        //extract parameters
        ImageView imageView = (ImageView) objects[0];
        Context context = (Context)objects[1];
        View autoOpenView = null;
        if(objects.length > 2) {
            autoOpenView = (View)objects[2];
        }

        TaskStatus queriedStatus = LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(task.getId());
        if(queriedStatus == null) { //task is not in the database, should not happen, it's validated on start
           throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new CourseStatusDisplayerTask.Result(imageView, autoOpenView, status);
    }

    /**
     * Displays the queried status on the UI.
     * @param result The result of the task.
     */
    @Override
    protected void onPostExecute(CourseStatusDisplayerTask.Result result) {
        if(result.status == com.gaspar.learnjava.curriculum.Status.COMPLETED) {
            result.imageView.setImageResource(R.drawable.completed_icon);
        } else {
            result.imageView.setImageResource(0); //0 removes the resource
        }
        //show component if needed
        boolean autoOpen = SettingsActivity.autoSlideOpenEnabled(result.imageView.getContext());
        if(autoOpen && result.autoOpenView != null &&
                result.status != com.gaspar.learnjava.curriculum.Status.LOCKED &&
                result.status != com.gaspar.learnjava.curriculum.Status.NOT_QUERIED) {
            result.autoOpenView.setVisibility(View.VISIBLE);
        }
        //save status
        task.setTaskStatus(result.status);
    }
}
