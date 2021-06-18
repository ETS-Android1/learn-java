package com.gaspar.learnjava.asynctask;

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
 * Fills the {@link ExamsActivity} with exam selector views.
 */
public class FillExamsActivityTask extends LjAsyncTask<ExamsActivity> {

    /**
     * The adapter which displays exam views.
     */
    private ExamAdapter adapter;

    /**
     * Gets the courses from XML, if they are not parsed. If already parsed, then just simply
     * reads them from a list. Courses contain some exam information, which is enough here.
     * @param objects Excepted to only contain {@link ExamsActivity}.
     * @return The same activity.
     */
    @Override
    protected ExamsActivity doInBackground(@Size(1) Object... objects) {
        ExamsActivity activity = (ExamsActivity) objects[0];
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

    /**
     * Sets the created {@link #adapter}, which will display the exam views.
     * @param activity The activity.
     */
    @Override
    protected void onPostExecute(ExamsActivity activity) {
        if(activity.successfulLoad) {
            ListView examSelectors = activity.findViewById(R.id.examSelectors);
            examSelectors.setAdapter(adapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.examsActivityLayout).setVisibility(View.VISIBLE); //show loaded courses
        } else { //failed to load.
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            //will finish activity when dialog is not showing anymore
            LogUtils.showLoadingFailDialog(activity, activity.getString(R.string.exams));
        }
    }
}
