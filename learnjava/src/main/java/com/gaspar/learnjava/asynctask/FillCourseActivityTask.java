package com.gaspar.learnjava.asynctask;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.Size;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.CourseAdapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;

/**
 * Parses the courses from XML and fill the list view in the {@link com.gaspar.learnjava.CoursesActivity}
 * with course selector views. On completion it hides the loading indicator in the activity.
 */
public class FillCourseActivityTask extends LjAsyncTask<CoursesActivity> {

    /**
     * The adapter which displays course views.
     */
    private CourseAdapter adapter;

    /**
     * Gets the courses from XML, if they are not parsed. If already parsed, then just simply
     * reads them from a list.
     * @param objects Excepted to only contain {@link CoursesActivity}.
     * @return The same activity.
     */
    @Override
    protected CoursesActivity doInBackground(@Size(1) Object... objects) {
        CoursesActivity activity = (CoursesActivity) objects[0];
        activity.successfulLoad = true; //assume success
        try {
            if(CoursesActivity.coursesNotParsed()) { //not parsed before
                CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance()
                        .parseCourses(activity)); //parse and save courses
            }
            adapter = new CourseAdapter(activity, CoursesActivity.getParsedCourses());
        } catch (Exception e) {
            LogUtils.logError("Exception while loading course", e);
            activity.successfulLoad = false; //save that load failed.
        }
        return activity;
    }

    /**
     * Sets the created {@link #adapter}, which will display the courses.
     * @param activity The activity.
     */
    @Override
    protected void onPostExecute(CoursesActivity activity) {
        if(activity.successfulLoad) {
            ListView courseSelectors =  activity.findViewById(R.id.courseSelectors);
            courseSelectors.setAdapter(adapter); //add the created adapter to list view
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            activity.findViewById(R.id.coursesLayout).setVisibility(View.VISIBLE); //show loaded courses
        } else { //failed to load.
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading indicator
            LogUtils.showLoadingFailDialog(activity, activity.getString(R.string.courses)); //will finish activity when dialog is not showing anymore
        }
    }
}
