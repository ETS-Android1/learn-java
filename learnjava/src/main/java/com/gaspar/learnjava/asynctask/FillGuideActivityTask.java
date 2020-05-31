package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.gaspar.learnjava.GuideActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.parsers.CourseParser;

import java.util.List;

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
            Log.e("LearnJava","Exception", e);
            activity.successfulLoad = false;
        }

        return new Result(activity, components);
    }

    @Override
    protected void onPostExecute(Result result) {
        if(result.activity.successfulLoad) {
            LinearLayout componentsLayout = result.activity.findViewById(R.id.guideComponents);
            for(Component component: result.components) { //add component views
                componentsLayout.addView(component.createComponentView(result.activity, componentsLayout));
            }
            final LayoutInflater inflater = LayoutInflater.from(result.activity);
            View closeButton = inflater.inflate(R.layout.close_buide_button, componentsLayout, false);
            closeButton.setOnClickListener(v -> result.activity.closeButtonOnClick(v));
            componentsLayout.addView(closeButton);
        } else {
            FillCourseActivityTask.showFailDialog(result.activity, result.activity.getString(R.string.guide));
        }
    }

    class Result {
        private GuideActivity activity;
        private List<Component> components;

        private Result(@NonNull GuideActivity activity, @Nullable List<Component> components) {
            this.activity = activity;
            this.components = components;
        }
    }
}
