package com.gaspar.learnjava.asynctask;

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
public class FillChapterActivityTask extends LjAsyncTask<FillChapterActivityTask.Result> {

    /**
     * This chapter is received on start, only to get the chapter ID and name. It does not have
     * components. Component parsing happens in this class, in the background.
     */
    private final Chapter receivedChapter;

    /**
     * Creates a chapter activity filler task.
     * @param chapter The chapter that must be parsed, with components.
     */
    public FillChapterActivityTask(Chapter chapter) {
        this.receivedChapter = chapter;
    }

    /**
     * Loads the chapter components from XML.
     * @param objects Expected to contain only a {@link ChapterActivity} object!
     * @return The result.
     */
    @Override
    protected Result doInBackground(@Size(1) Object... objects) {
        ChapterActivity activity = (ChapterActivity) objects[0];
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

    /**
     * Creates the views that display the components in the {@link ChapterActivity}.
     * @param result The result of the task, any type.
     */
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
            ComponentAdapter<ChapterActivity> adapter = new ComponentAdapter<>(result.parsedChapter.getComponents(), result.activity,
                    footerHolderGenerator, footerHolderBinder);
            //attach adapter
            componentsView.setAdapter(adapter);
            //hide loading, show recycler
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            componentsView.setVisibility(View.VISIBLE);
            //ask about dark theme
            ThemeUtils.showDarkThemePromptIfNeeded(result.activity);
        } else {
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            LogUtils.showLoadingFailDialog(result.activity, result.activity.getString(R.string.courses));
        }
    }

    /**
     * Simple class that groups objects together so that they can be passed as the result of {@link #doInBackground(Object...)}.
     */
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
