package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Size;
import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.database.ChapterStatus;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;
import com.google.android.material.snackbar.Snackbar;

/**
 * Finds the name of a single chapter from it's id. Sets the view in the main menu on completion.
 */
public class InitStarterViewTask extends AsyncTask<Object, Void, LearnJavaActivity> {

    private static final int NO_STARTED_CHAPTER = Integer.MIN_VALUE;

    private final int chapterId;

    private final boolean started;

    private boolean showOpenDrawerForExamPrompt;

    public InitStarterViewTask(int chapterId, boolean started) {
        this.started = started;
        if(started) {
            this.chapterId = chapterId;
        } else {
            this.chapterId = NO_STARTED_CHAPTER;
        }
    }

    @Override
    protected LearnJavaActivity doInBackground(@Size(1) Object... objects) {
        LearnJavaActivity activity = (LearnJavaActivity) objects[0];
        try {
            activity.successfulLoad = true;
            if(started) { //parse started chapter from xml
                ChapterStatus status = LearnJavaDatabase.getInstance(activity)
                        .getChapterDao().queryChapterStatus(chapterId);
                if(status == null || status.getStatus() != com.gaspar.learnjava.curriculum.Status.COMPLETED) {
                    //do not move to next chapter in this case
                    activity.setStartedChapter(CourseParser.getInstance().parseChapter(chapterId,
                            false, activity));
                } else { //advance to next chapter, previous was completed.
                    int nextId = nextDisplayableChapterId(chapterId, activity);
                    //this may actually be the same chapter, if there was no new one unlocked.
                    activity.setStartedChapter(CourseParser.getInstance().parseChapter(nextId,
                            false, activity));
                }
            } else { //use the first chapter in curriculum
                Chapter firstChapter = CoursesActivity.getParsedCourses().get(0).getChapters().get(0);
                activity.setStartedChapter(firstChapter);
            }
        } catch (Exception e) {
            Log.e("LearnJava", "Exception", e);
            activity.successfulLoad = false;
        }
        return activity;
    }

    @Override
    protected void onPostExecute(LearnJavaActivity activity) {
        Button button = activity.findViewById(R.id.continueLearningButton);
        if(!activity.successfulLoad) { //loading has failed
            String message = activity.getString(R.string.loading_error, activity.getString(R.string.courses));
            Snackbar.make(button, message, Snackbar.LENGTH_LONG).show();
            return;
        }
        View startedView = activity.findViewById(R.id.startedView);
        View notStartedView =  activity.findViewById(R.id.notStartedView);
        View loadingView = activity.findViewById(R.id.loadingView);
        loadingView.setVisibility(View.GONE); //don't show loading anymore
        if(started) {
            notStartedView.setVisibility(View.GONE);
            startedView.setVisibility(View.VISIBLE);
            button.setText(activity.getStartedChapter().getName());
        } else {
            startedView.setVisibility(View.GONE);
            notStartedView.setVisibility(View.VISIBLE);
        }
        //the user completed all chapters, but the next course it locked: he needs to take an exam
        //this prompt shows where he can open the drawer to start an exam.
        if(showOpenDrawerForExamPrompt) {
            activity.showAndAnimateOpenDrawerPrompt();
        }
    }

    @WorkerThread
    private int nextDisplayableChapterId(int currentChapterId, Context context) {
        try {
            if(CoursesActivity.coursesNotParsed()) { //only parse XML if necessary
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance().parseCourses(context));
            }
            for(int i=0; i<CoursesActivity.getParsedCourses().size(); i++) {
                Course course = CoursesActivity.getParsedCourses().get(i);
                for(int j=0; j<course.getChapters().size(); j++) {
                    if(course.getChapters().get(j).getId() == currentChapterId) { //found current chapter
                        if(j<course.getChapters().size()-1) { //next chapter is still in the same course
                            return course.getChapters().get(j+1).getId();
                        } else { //move on the the next course
                            int nextCourseId = CoursesActivity.getParsedCourses().get(i+1).getId(); //can throw exception, but it's handled
                            CourseStatus cStatus = LearnJavaDatabase.getInstance(context)
                                    .getCourseDao().queryCourseStatus(nextCourseId);
                            if(cStatus == null) throw new RuntimeException("Database error!");
                            if(cStatus.getStatus() == com.gaspar.learnjava.curriculum.Status.LOCKED) {
                                //next course is not yet unlocked
                                showOpenDrawerForExamPrompt = true; //show the user a prompt where he can progress
                                return currentChapterId;
                            } else { //next course is unlocked
                                return CoursesActivity.getParsedCourses().get(i+1).getChapters().get(0).getId();
                            }
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) { //expected exception
            return currentChapterId;
        } catch (Exception e) { //unexpected exceptions
            Log.e("LearnJava", "Exception", e);
            return currentChapterId;
        }
        return currentChapterId; //should not get here
    }
}
