package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.ExamsActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.ExamAdapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;

/**
 * Fills the exams activity with 'exam_selector_view' views.
 */
public class FillExamsActivityTask extends AsyncTask<ExamsActivity, Void, ExamsActivity> {

    private ExamAdapter adapter;

    @Override
    protected ExamsActivity doInBackground(@Size(1) ExamsActivity... examsActivities) {
        ExamsActivity activity = examsActivities[0];
        activity.successfulLoad = true;
        try {
            if(CoursesActivity.coursesNotParsed()) {
                CoursesActivity.getParsedCourses().addAll(CourseParser
                        .getInstance().parseCourses(activity));
            }
            adapter = new ExamAdapter(activity, CoursesActivity.getParsedCourses());
        } catch (Exception e) {
            LogUtils.logError( "Exception while loading exam!", e);
            activity.successfulLoad = false;
        }
        return activity;
    }

    @Override
    protected void onPostExecute(ExamsActivity activity) {
        if(activity.successfulLoad) {
            ListView examSelectors = activity.findViewById(R.id.examSelectors);
            examSelectors.setAdapter(adapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.examsActivityLayout).setVisibility(View.VISIBLE); //show loaded courses
        } else { //failed to load.
            //will finish activity when dialog is not showing anymore
            FillCourseActivityTask.showFailDialog(activity, activity.getString(R.string.exams));
        }
    }
}
