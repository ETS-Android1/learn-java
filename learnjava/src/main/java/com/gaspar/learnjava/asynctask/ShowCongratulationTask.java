package com.gaspar.learnjava.asynctask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.Size;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.util.List;

/**
 * A background task that queries the exam statuses from the database, and displays a prompt in the
 * activity if the user passed all of them. Called from {@link CoursesActivity}.
 */
public class ShowCongratulationTask extends LjAsyncTask<ShowCongratulationTask.Result> {

    /**
     * Performs the query from the database.
     * @param objects Expected to be a {@link CoursesActivity}.
     * @return The result.
     */
    @Override
    protected Result doInBackground(@Size(1) Object... objects) {
        CoursesActivity activity = (CoursesActivity) objects[0];
        return new Result(LearnJavaDatabase.getInstance(activity).getExamDao().getAllExamStatus(), activity);
    }

    /**
     * Displays the dialog if all exams were completed.
     * @param result The result of the task.
     */
    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        SharedPreferences prefs = result.activity.getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(!prefs.getBoolean(CoursesActivity.CONGRATULATION_PROMPT, false)) return; //don't show anything is disabled
        int counter = 0;
        for(int status: result.examStatuses) {
            if (status == com.gaspar.learnjava.curriculum.Status.COMPLETED) counter++;
        }
        if(counter == result.examStatuses.size()) { //all exams completed
            AlertDialog.Builder builder = new AlertDialog.Builder(result.activity, ThemeUtils.getThemedDialogStyle());
            View congratulationsView = View.inflate(result.activity, R.layout.view_congratulation_prompt, null);
            builder.setView(congratulationsView);

            AlertDialog dialog = builder.create();
            congratulationsView.findViewById(R.id.congratulationsOkButton).setOnClickListener(v -> {
                CheckBox checkBox = congratulationsView.findViewById(R.id.congratulationsCheckBox);
                prefs.edit().putBoolean(CoursesActivity.CONGRATULATION_PROMPT, checkBox.isChecked()).apply(); //update show policy
                dialog.dismiss(); //close dialog
            });
            dialog.show();
        }
    }

    /**
     * Helper class that stores the result of an {@link ShowCongratulationTask}.
     */
    static class Result {
        private final List<Integer> examStatuses;
        private final CoursesActivity activity;

        Result(List<Integer> examStatuses, CoursesActivity activity) {
            this.examStatuses = examStatuses;
            this.activity = activity;
        }
    }
}
