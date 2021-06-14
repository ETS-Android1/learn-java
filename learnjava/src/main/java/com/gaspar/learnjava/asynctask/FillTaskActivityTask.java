package com.gaspar.learnjava.asynctask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Size;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.TaskActivity;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;
import com.gaspar.learnjava.parsers.TaskParser;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;

/**
 * Loads the task components into a task activity.
 */
public class FillTaskActivityTask extends AsyncTask<TaskActivity, Void, TaskActivity> {

    /**
     * This task was passed to the activity. It has no components.
     */
    private final Task passedTask;

    public FillTaskActivityTask(Task passedTask) {
        this.passedTask = passedTask;
    }

    @Override
    protected TaskActivity doInBackground(@Size(1) TaskActivity... taskActivities) {
        TaskActivity activity = taskActivities[0];
        activity.successfulLoad = true;
        try {
            Task parsedTask = TaskParser.getInstance().parseTask(passedTask.getId(),
                    true, activity);
            TaskStatus taskStatus = LearnJavaDatabase.getInstance(activity).getTaskDao()
                    .queryTaskStatus(parsedTask.getId()); //get status from DB
            if(taskStatus == null) {
                /*
                This should not happen in the app, however, it
                does happen when running TaskActivityTest UI tests. So for that case,
                we will use a "not completed" status.
                 */
                LogUtils.logError("Task was not found in the database! This is a problem if NOT testing!");
                taskStatus = new TaskStatus();
                taskStatus.setStatus(com.gaspar.learnjava.curriculum.Status.UNLOCKED);
                taskStatus.setTaskId(passedTask.getId());
            }
            parsedTask.setTaskStatus(taskStatus.getStatus()); //save task status
            activity.setDisplayedTask(parsedTask); //parse and save task, now with components and status
        } catch (Exception e) {
            LogUtils.logError("Exception while loading task!", e);
            activity.successfulLoad = false;
        }
        return activity;
    }

    @Override
    protected void onPostExecute(TaskActivity activity) {
        if(activity.successfulLoad) {
            if(activity.getDisplayedTask().getDescriptionComponents() == null ||
            activity.getDisplayedTask().getSolutionComponents() == null) return; //should not happen

            LinearLayout componentsLayout = activity.findViewById(R.id.taskComponents);
            for(Component component: activity.getDisplayedTask().getDescriptionComponents()) {
                componentsLayout.addView(component.createComponentView(activity, componentsLayout)); //add component views
            }
            LinearLayout solutionComponentsView = activity.findViewById(R.id.solutionComponents);
            for(Component component: activity.getDisplayedTask().getSolutionComponents()) {
                View componentView = component.createComponentView(activity, solutionComponentsView);
                solutionComponentsView.addView(componentView); //add solution views
            }
            CheckBox completedCheckBox = activity.findViewById(R.id.taskCompletedCheckBox);
            boolean taskCompleted = (activity.getDisplayedTask().getTaskStatus() ==
                    com.gaspar.learnjava.curriculum.Status.COMPLETED);
            completedCheckBox.setChecked(taskCompleted); //show checked if task is completed
            completedCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) ->
                    activity.updateTaskStatus(isChecked, activity.getDisplayedTask().getId()));

            //ask about dark theme
            ThemeUtils.showDarkThemePromptIfNeeded(activity);
        } else {
            FillCourseActivityTask.showFailDialog(activity, activity.getString(R.string.tasks));
        }
    }
}
