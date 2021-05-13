package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Queries and displays the status of a course. This depends on if it's unlocked or not, and if it's finished or not.
 */
public class CourseStatusDisplayerTask extends AsyncTask<Object, Void, CourseStatusDisplayerTask.Result> {

    /**
     * The course.
     */
    private final Course course;

    /**
     * Optional, this will be called when the task end, IN ADDITION TO displaying course status.
     */
    private final Runnable callAtEnd;

    /**
     * Create a task that queries the status of a course.
     * @param course The course
     */
    public CourseStatusDisplayerTask(Course course) {
        this.course = course;
        this.callAtEnd = null;
    }

    /**
     * Create a task that queries the status of a course.
     * @param course The course
     * @param callAtEnd A runnable, which will be called after the query is finished.
     *                  Can be used to perform something else.
     */
    public CourseStatusDisplayerTask(Course course, Runnable callAtEnd) {
        this.course = course;
        this.callAtEnd = callAtEnd;
    }

    /**
     * Performs the status query in the background.
     * @param objects First must be the status icon. Second must be the activity. Third CAN optionally be
     *                the view that is auto opened if needed.
     * @return Result of the query, with the status and other objects.
     */
    @Override
    protected Result doInBackground(@Size(min = 2, max = 3) Object... objects) {
        //extract parameters
        ImageView imageView = (ImageView) objects[0];
        AppCompatActivity activity = (AppCompatActivity) objects[1];
        View autoOpenView = null;
        if(objects.length > 2) { //may not be included
            autoOpenView = (View)objects[2];
        }
        CourseStatus queriedStatus = LearnJavaDatabase.getInstance(activity)
                .getCourseDao().queryCourseStatus(course.getId());
        if(queriedStatus == null)  { //database is validated on start, this should not happen
            throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new Result(activity, imageView, autoOpenView, status);
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
        //automatically open non-locked courses
        boolean autoSlideOpen = SettingsActivity.autoSlideOpenEnabled(result.activity);
        if(autoSlideOpen && result.autoOpenView != null &&
                result.status != com.gaspar.learnjava.curriculum.Status.LOCKED &&
                result.status != com.gaspar.learnjava.curriculum.Status.NOT_QUERIED) {
            result.autoOpenView.setVisibility(View.VISIBLE);
        }
        //add unfinished notification dialog, if needed
        if(!course.isFinished()) addDialogOnClick(result.activity, result.imageView);
        //save status for later use
        course.setStatus(result.status);
        //run the runnable, if it exists
        if(callAtEnd != null) callAtEnd.run();
    }

    /**
     * Adds a dialog that shows when the user taps the status icon. Notifies about unfinished courses.
     * @param activity Activity.
     * @param imageView The status icon.
     */
    private void addDialogOnClick(AppCompatActivity activity, View imageView) {
        imageView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle());
            builder.setTitle(R.string.in_development);
            builder.setMessage(R.string.course_development_info);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    /**
     * This utility class stores the result of the background operation, and can store
     * some extra objects as well. This is reused in other classes as well.
     */
    public static class Result {

        private final AppCompatActivity activity;

        ImageView imageView;

        View autoOpenView;

        public @com.gaspar.learnjava.curriculum.Status int status;

        Result(AppCompatActivity activity, ImageView imageView, View autoOpenView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.activity = activity;
            this.imageView = imageView;
            this.status = status;
            this.autoOpenView = autoOpenView;
        }

        Result(ImageView imageView, View autoOpenView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.activity = null;
            this.imageView = imageView;
            this.status = status;
            this.autoOpenView = autoOpenView;
        }

        Result(ImageView imageView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.activity = null;
            this.imageView = imageView;
            this.status = status;
            this.autoOpenView = null;
        }
    }
}
