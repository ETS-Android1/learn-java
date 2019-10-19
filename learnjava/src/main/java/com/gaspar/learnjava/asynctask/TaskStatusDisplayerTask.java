package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;

/**
 * Queries and sets the status of a {@link com.gaspar.learnjava.curriculum.Task}. Shows the
 * result using the given image view.
 */
public class TaskStatusDisplayerTask extends AsyncTask<Object, Void, CourseStatusDisplayerTask.Result> {

    private Task task;

    public TaskStatusDisplayerTask(Task task) {
        this.task = task;
    }

    @Override
    protected CourseStatusDisplayerTask.Result doInBackground(@Size(2) Object... objects) {
        ImageView imageView = (ImageView) objects[0];
        Context context = (Context)objects[1];
        TaskStatus queriedStatus = LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(task.getId());
        if(queriedStatus == null) { //task is not in the database, should not happen, it's validated on start
           throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new CourseStatusDisplayerTask.Result(imageView, status);
    }

    @Override
    protected void onPostExecute(CourseStatusDisplayerTask.Result result) {
        if(result.status == com.gaspar.learnjava.curriculum.Status.COMPLETED) {
            result.imageView.setImageResource(R.drawable.completed_icon);
        } else {
            result.imageView.setImageResource(0); //0 removes the resource
        }
        task.setTaskStatus(result.status); //save status
    }
}
