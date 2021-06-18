package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.ChapterActivity;
import com.gaspar.learnjava.GuideActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.adapters.ComponentAdapter;
import com.gaspar.learnjava.curriculum.components.Component;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Loads components into the guide activities scroll view.
 */
public class FillGuideActivityTask extends AsyncTask<GuideActivity, Void, FillGuideActivityTask.Result> {

    @Override
    protected Result doInBackground(@Size(1) GuideActivity... guideActivities) {
        GuideActivity activity = guideActivities[0];
        List<Component> components = null;
        activity.successfulLoad = true;
        try {
            components = CourseParser.getInstance().parseGuide(activity);
        } catch (Exception e) {
            LogUtils.logError("Exception while loading guide!", e);
            activity.successfulLoad = false;
        }

        return new Result(activity, components);
    }

    @Override
    protected void onPostExecute(Result result) {
        if(result.activity.successfulLoad) {
            //set up recycler view of components
            RecyclerView componentsView = result.activity.findViewById(R.id.guideComponents);
            //define the footer view for adapter
            Function<ViewGroup, RecyclerView.ViewHolder> footerHolderGenerator = (parent) -> {
                //re-use chapter activity's holder, it is perfect here
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_close_chapter, parent, false);
                return new ChapterActivity.ChapterFooterHolder(footerView);
            };
            BiConsumer<RecyclerView.ViewHolder, Integer> footerHolderBinder = (holder, position) -> {
                ChapterActivity.ChapterFooterHolder footerHolder = (ChapterActivity.ChapterFooterHolder)holder;
                footerHolder.closeButton.setOnClickListener(result.activity::closeButtonOnClick);
            };
            //create adapter from components
            ComponentAdapter adapter = new ComponentAdapter(result.components, result.activity,
                    footerHolderGenerator, footerHolderBinder);
            //attach adapter
            componentsView.setAdapter(adapter);
            //hide loading, show recycler
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            componentsView.setVisibility(View.VISIBLE);
        } else {
            result.activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            LogUtils.showLoadingFailDialog(result.activity, result.activity.getString(R.string.guide));
        }
    }

    static class Result {
        private final GuideActivity activity;
        private final List<Component> components;

        private Result(@NonNull GuideActivity activity, @Nullable List<Component> components) {
            this.activity = activity;
            this.components = components;
        }
    }
}
