package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.database.ChapterStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

/**
 * Queries and displays the status of a chapter.
 */
public class ChapterStatusDisplayerTask extends AsyncTask<Object, Void, CourseStatusDisplayerTask.Result> {

    private Chapter chapter;

    public ChapterStatusDisplayerTask(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    protected CourseStatusDisplayerTask.Result doInBackground(@Size(2) Object... objects) {
        ImageView imageView = (ImageView) objects[0];
        Context context = (Context)objects[1];
        ChapterStatus queriedStatus = LearnJavaDatabase.getInstance(context).getChapterDao()
                .queryChapterStatus(chapter.getId());
        if(queriedStatus == null) { //chapter is not in the database, should not happen
           throw new RuntimeException("Database error!");
        }
        @com.gaspar.learnjava.curriculum.Status int status = queriedStatus.getStatus();
        return new CourseStatusDisplayerTask.Result(imageView, status);
    }

    @Override
    protected void onPostExecute(CourseStatusDisplayerTask.Result result) {
        if(result.status == com.gaspar.learnjava.curriculum.Status.COMPLETED) {
            result.imageView.setImageResource(R.drawable.completed_icon);
        } //not completed chapters have no icon
        chapter.setStatus(result.status); //save status
    }
}
