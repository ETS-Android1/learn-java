package com.gaspar.learnjava.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.iwgang.countdownview.CountdownView;

public class ExamStatusDisplayerTask extends AsyncTask<Object, Void, ExamStatusDisplayerTask.Result> {

    private final Exam exam;

    public ExamStatusDisplayerTask(Exam exam) {
        this.exam = exam;
    }

    @Override
    protected Result doInBackground(@Size(2) Object... objects) {
        View examView = (View)objects[0];
        AppCompatActivity activity = (AppCompatActivity) objects[1];

        while(Chapter.chapterStatusUpdatePending) { //if needed, wait until chapter is updated
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ExamStatus queriedExamStatus = LearnJavaDatabase.getInstance(activity).
                getExamDao().queryExamStatus(exam.getId());
        //variables needed for result
        @com.gaspar.learnjava.curriculum.Status int status;
        long lastStarted;
        boolean onCoolDown;
        long secondsRemaining; //this is not important if it's not on cool down
        if(queriedExamStatus == null) { //exam not in database, should not happen, validated on start
            throw new RuntimeException("Database error!");
        }
        status = queriedExamStatus.getStatus();
        lastStarted = queriedExamStatus.getLastStarted();
        if(LearnJavaActivity.DEBUG) { //if we are in debug mode just unlock the exam no matter what
            onCoolDown = false;
            secondsRemaining = -1;
        } else if(status == com.gaspar.learnjava.curriculum.Status.COMPLETED) { //exam completed before, no cool down
            onCoolDown = false;
            secondsRemaining = -1; // not important
        } else if (queriedExamStatus.getLastStarted() == Exam.EXAM_NEVER_STARTED) { //was never started, not on cool down
            onCoolDown = false;
            secondsRemaining = -1; //not important
        } else if(Exam.coolDownTimeAgo(lastStarted)) { // not on cool down
            onCoolDown = false;
            secondsRemaining = -1; //not important
        } else { //cool down time not yet passed, on cool down
            onCoolDown = true;
            secondsRemaining = ((long)(lastStarted/1000.0) + Exam.EXAM_COOL_DOWN_TIME) - (long)(System.currentTimeMillis()/1000.0);
        }
        return new Result(activity, examView, status, secondsRemaining, onCoolDown, queriedExamStatus.getTopScore());
    }

    @Override
    protected void onPostExecute(Result result) {
        final Button takeExamButton = result.examView.findViewById(R.id.takeExamButton);
        ImageView examStatusIcon = result.examView.findViewById(R.id.examStatusIcon);
        hideExamComponents(result.examView); //in case something was already visible

        if(!exam.isFinished()) { //unfinished exams will look similar to locked exams
            result.examView.findViewById(R.id.unfinishedLayout).setVisibility(View.VISIBLE);
            addDialogOnClick(result.activity, result.examView);
        } else if(result.status == com.gaspar.learnjava.curriculum.Status.LOCKED && !LearnJavaActivity.DEBUG) { //show locked view
            result.examView.findViewById(R.id.lockedLayout).setVisibility(View.VISIBLE);
            addShakeOnClick(result.activity, result.examView);
        } else if(!result.onCoolDown) { //exam not locked, on not cool down, show button view

            result.examView.findViewById(R.id.unlockedLayout).setVisibility(View.VISIBLE);
            examStatusIcon.setImageResource(result.status == com.gaspar.learnjava.curriculum.Status.COMPLETED
                    ? R.drawable.completed_icon : R.drawable.unlocked_icon); //cant be locked here

        }  else { //exam is on cool down, show cool down view

            final View countdownLayout = result.examView.findViewById(R.id.countdownLayout);
            countdownLayout.setVisibility(View.VISIBLE);
            CountdownView countdownView = result.examView.findViewById(R.id.countdownView);
            countdownView.setOnCountdownEndListener(cv -> { //on countdown end hide this and show start button
                countdownLayout.setVisibility(View.GONE);
                result.examView.findViewById(R.id.unlockedLayout).setVisibility(View.VISIBLE);
                result.examView.setOnClickListener(null); //remove shake
            });
            countdownView.start(result.secondsRemaining * 1000);
            addShakeOnClick(result.activity, result.examView);
        }

        if(result.topScore != Exam.EXAM_NEVER_STARTED) { //if there is a displayable top score
            TextView scoreDisplayer = result.examView.findViewById(R.id.topScoreDisplayer);
            String text = result.topScore + "/" + exam.getQuestionAmount() + " " + result.activity.getString(R.string.points);
            scoreDisplayer.setText(text);
            result.examView.findViewById(R.id.topScoreView).setVisibility(View.VISIBLE);
        }

        takeExamButton.setOnClickListener(view -> { //button always gets a listener
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(result.activity, ThemeUtils.getThemedDialogStyle());
            builder.setTitle(R.string.exam);
            builder.setMessage(R.string.confirm_exam_start);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> Exam.startExamActivity(result, exam));
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    private void hideExamComponents(View examView) {
        examView.setOnClickListener(null);
        examView.findViewById(R.id.unlockedLayout).setVisibility(View.GONE);
        examView.findViewById(R.id.countdownLayout).setVisibility(View.GONE);
        examView.findViewById(R.id.lockedLayout).setVisibility(View.GONE);
        examView.findViewById(R.id.topScoreView).setVisibility(View.GONE);
        examView.findViewById(R.id.unfinishedLayout).setVisibility(View.GONE);
    }

    //adds a shake animation. to not collapse the course view
    private void addShakeOnClick(Context context, View examViewPart) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.shake);
        examViewPart.setOnClickListener(v -> examViewPart.startAnimation(animation));
    }

    //adds a dialog that notifies about unfinished exams on click
    private void addDialogOnClick(AppCompatActivity activity, View examView) {
        examView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, ThemeUtils.getThemedDialogStyle());
            builder.setTitle(R.string.in_development);
            builder.setMessage(R.string.exam_development_info);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    public static class Result {
        public AppCompatActivity activity;
        public View examView;
        public @com.gaspar.learnjava.curriculum.Status int status;
        private final long secondsRemaining;
        private final boolean onCoolDown;
        private final int topScore;

        Result(AppCompatActivity activity ,View examView, @com.gaspar.learnjava.curriculum.Status int status,
               long secondsRemaining, boolean onCoolDown, int topScore) {
            this.examView = examView;
            this.activity = activity;
            this.status = status;
            this.secondsRemaining = secondsRemaining;
            this.onCoolDown = onCoolDown;
            this.topScore = topScore;
        }
    }
}
