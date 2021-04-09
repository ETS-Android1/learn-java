package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Queries and displays the status of a course. This depends on if it's unlocked or not, and if it's finished or not.
 */
public class CourseStatusDisplayerTask extends AsyncTask<Object, Void, CourseStatusDisplayerTask.Result> {

    private final Course course;

    /**
     * Optional, this will be called when the task end, IN ADDITION TO displaying course status.
     */
    private final Runnable callAtEnd;

    public CourseStatusDisplayerTask(Course course) {
        this.course = course;
        this.callAtEnd = null;
    }

    public CourseStatusDisplayerTask(Course course, Runnable callAtEnd) {
        this.course = course;
        this.callAtEnd = callAtEnd;
    }

    @Override
    protected Result doInBackground(@Size(2) Object... objects) {
        ImageView imageView = (ImageView) objects[0];
        AppCompatActivity activity = (AppCompatActivity) objects[1];
        CourseStatus queriedStatus = LearnJavaDatabase.getInstance(activity)
                .getCourseDao().queryCourseStatus(course.getId());
        if(queriedStatus == null)  { //database is validated on start, this should not happen
            throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new Result(activity ,imageView, status);
    }

    @Override
    protected void onPostExecute(Result result) {
        switch (result.status) { //set icon according to queried status
            case com.gaspar.learnjava.curriculum.Status.LOCKED:
                //in this case locked will always show, even if unfinished
                result.imageView.setImageResource(R.drawable.locked_icon);
                break;
            case com.gaspar.learnjava.curriculum.Status.UNLOCKED:
                if(course.isFinished()) {
                    result.imageView.setImageResource(R.drawable.unlocked_icon);
                } else { //unlocked but unfinished courses have special icon
                    result.imageView.setImageResource(R.drawable.unfinished_icon);
                }
                break;
            case com.gaspar.learnjava.curriculum.Status.COMPLETED:
                if(course.isFinished()) {
                    result.imageView.setImageResource(R.drawable.completed_icon);
                } else { // unfinished courses have special icon
                    result.imageView.setImageResource(R.drawable.unfinished_icon);
                }
                break;
            case com.gaspar.learnjava.curriculum.Status.NOT_QUERIED:
                Log.d("LearnJava" , "Incorrect behavior: NOT_QUERIED status");
        }
        if(!course.isFinished()) addDialogOnClick(result.activity, result.imageView);
        course.setStatus(result.status); //save status
        if(callAtEnd != null) callAtEnd.run();
    }

    //adds a dialog that notifies about unfinished courses on click
    private void addDialogOnClick(AppCompatActivity activity, View imageView) {
        imageView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ThemeUtils.createDialogWrapper(activity));
            builder.setTitle(R.string.in_development);
            builder.setMessage(R.string.course_development_info);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    public static class Result {
        private final AppCompatActivity activity;
        ImageView imageView;
        public @com.gaspar.learnjava.curriculum.Status int status;

        Result(AppCompatActivity activity, ImageView imageView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.activity = activity;
            this.imageView = imageView;
            this.status = status;
        }

        Result(ImageView imageView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.activity = null;
            this.imageView = imageView;
            this.status = status;
        }
    }
}
