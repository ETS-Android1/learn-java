package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.ChapterActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.ComponentAdapter;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;

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
            result.activity.setPassedChapter(result.parsedChapter); //set the parsed chapter to activity
            //set up recycler view of components
            RecyclerView componentsView = result.activity.findViewById(R.id.chapterComponents);
            //define the footer view for adapter
            Function<ViewGroup, RecyclerView.ViewHolder> footerHolderGenerator = (parent) -> {
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_close_chapter, parent, false);
                return new ChapterActivity.ChapterFooterHolder(footerView);
            };
            BiConsumer<RecyclerView.ViewHolder, Integer> footerHolderBinder = (holder, position) -> {
                ChapterActivity.ChapterFooterHolder footerHolder = (ChapterActivity.ChapterFooterHolder)holder;
                footerHolder.closeButton.setOnClickListener(v -> result.activity.chapterConfirmedOnClick(v));
            };
            //create adapter from components
            ComponentAdapter adapter = new ComponentAdapter(result.parsedChapter.getComponents(), result.activity,
                    footerHolderGenerator, footerHolderBinder);
            //attach adapter
            componentsView.setAdapter(adapter);
            //hide loading, show recycler
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            componentsView.setVisibility(View.VISIBLE);
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
