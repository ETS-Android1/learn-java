package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Size;

import com.gaspar.learnjava.ChapterActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;

/**
 * Parses the chapter components from XML, then shows them in the list view of the activity.
 */
public class FillChapterActivityTask extends AsyncTask<ChapterActivity, Void, FillChapterActivityTask.Result> {

    //this chapter chapter has not parsed components!
    private final Chapter receivedChapter;

    public FillChapterActivityTask(Chapter chapter) {
        this.receivedChapter = chapter;
    }

    @Override
    protected Result doInBackground(@Size(1) ChapterActivity... chapterActivities) {
        ChapterActivity activity = chapterActivities[0];
        boolean successfulLoad = true;
        Chapter parsedChapter = null;
        try {
            parsedChapter = CourseParser.getInstance().parseChapter(receivedChapter.getId(),
                    true, activity); //this will have components
        } catch (Exception e) {
            LogUtils.logError("Exception when parsing chapter!", e);
            successfulLoad = false;
        }
        return new Result(activity, successfulLoad, parsedChapter);
    }

    @Override
    protected void onPostExecute(Result result) {
        if(result.success) {
            LinearLayout componentsLayout = result.activity.findViewById(R.id.chapterComponents);
            for(Component component: result.parsedChapter.getComponents()) { //inflate and add component views
                componentsLayout.addView(component.createComponentView(result.activity, componentsLayout));
            }
            result.activity.setPassedChapter(result.parsedChapter); //set the parsed chapter to activity
            View confirmView = View.inflate(result.activity, R.layout.view_close_chapter, componentsLayout);
            Button confirmButton = confirmView.findViewById(R.id.chapterConfirmButton);
            confirmButton.setOnClickListener(result.activity::chapterConfirmedOnClick);
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            result.activity.findViewById(R.id.chapterComponentsLayout).setVisibility(View.VISIBLE);
            //ask about dark theme
            ThemeUtils.showDarkThemePromptIfNeeded(result.activity);
        } else {
            FillCourseActivityTask.showFailDialog(result.activity, result.activity.getString(R.string.courses));
        }
    }

    static class Result {
        ChapterActivity activity;
        boolean success;
        Chapter parsedChapter;

        Result(ChapterActivity activity, boolean success, Chapter parsedChapter) {
            this.activity = activity;
            this.success = success;
            this.parsedChapter = parsedChapter;
        }
    }
}
