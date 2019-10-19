package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

/**
 * Queries and displays the status of a course.
 */
public class CourseStatusDisplayerTask extends AsyncTask<Object, Void, CourseStatusDisplayerTask.Result> {

    private Course course;

    /**
     * Optional, this will be called when the task end, IN ADDITION TO displaying course status.
     */
    private Runnable callAtEnd;

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
        Context context = (Context)objects[1];
        CourseStatus queriedStatus = LearnJavaDatabase.getInstance(context)
                .getCourseDao().queryCourseStatus(course.getId());
        if(queriedStatus == null)  { //database is validated on start, this should not happen
            throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new Result(imageView, status);
    }

    @Override
    protected void onPostExecute(Result result) {
        switch (result.status) { //set icon according to queried status
            case com.gaspar.learnjava.curriculum.Status.LOCKED:
                result.imageView.setImageResource(R.drawable.locked_icon);
                break;
            case com.gaspar.learnjava.curriculum.Status.UNLOCKED:
                result.imageView.setImageResource(R.drawable.unlocked_icon);
                break;
            case com.gaspar.learnjava.curriculum.Status.COMPLETED:
                result.imageView.setImageResource(R.drawable.completed_icon);
                break;
            case com.gaspar.learnjava.curriculum.Status.NOT_QUERIED:
                Log.d("LearnJava" , "Incorrect behavior: NOT_QUERIED status");
        }
        course.setStatus(result.status); //save status
        if(callAtEnd != null) callAtEnd.run();
    }

    public static class Result {
        ImageView imageView;
        public @com.gaspar.learnjava.curriculum.Status int status;

        Result(ImageView imageView, @com.gaspar.learnjava.curriculum.Status int status) {
            this.imageView = imageView;
            this.status = status;
        }
    }
}
