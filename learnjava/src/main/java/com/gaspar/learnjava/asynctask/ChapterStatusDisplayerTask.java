package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.database.ChapterStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

/**
 * Queries and displays the status of a chapter. The result is then displayed inside {@link com.gaspar.learnjava.CoursesActivity}.
 */
public class ChapterStatusDisplayerTask extends LjAsyncTask<CourseStatusDisplayerTask.Result> {

    /**
     * The chapter, whose status must be queried.
     */
    private final Chapter chapter;

    /**
     * Creates a chapter status query task.
     * @param chapter The chapter.
     */
    public ChapterStatusDisplayerTask(Chapter chapter) {
        this.chapter = chapter;
    }

    /**
     * Queries the status from the database in the background.
     * @param objects Expected to be 2 argument, the first is an {@link ImageView} where the status,
     *                will get displayed the second is {@link Context}.
     * @return The result.
     */
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

    /**
     * Sets the icon of the received {@link ImageView} according to the status from the database.
     * @param result The result of the task, any type.
     */
    @Override
    protected void onPostExecute(CourseStatusDisplayerTask.Result result) {
        if(result.status == com.gaspar.learnjava.curriculum.Status.COMPLETED) {
            result.imageView.setImageResource(R.drawable.completed_icon);
        } //not completed chapters have no icon
        chapter.setStatus(result.status); //save status
    }
}
