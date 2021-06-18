package com.gaspar.learnjava.asynctask;

import android.view.View;

import androidx.annotation.Size;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.ExamActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.adapters.QuestionAdapter;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.parsers.ExamParser;
import com.gaspar.learnjava.utils.LogUtils;

import java.util.Collections;
import java.util.Random;

import cn.iwgang.countdownview.CountdownView;

/**
 * Loads the {@link com.gaspar.learnjava.curriculum.questions.Question}s for an exam inside an {@link ExamActivity},
 * and randomly selects the ones that will be included in the exam. On completion it hides the progress indicator and shows the questions.
 */
public class LoadExamQuestionsTask extends LjAsyncTask<ExamActivity> {

    /**
     * This flag determines if the questions will be shuffled to a random order. For testing this
     * is not ideal, so it can be disabled here.
     */
    @VisibleForTesting
    public static boolean shuffleQuestions = true;

    /**
     * Id of the exam that must be parsed.
     */
    private final int examId;

    /**
     * Creates an exam loading task.
     * @param examId The exam id.
     */
    public LoadExamQuestionsTask(int examId) {
        this.examId = examId;
    }

    /**
     * Performs the question parsing in the background.
     * @param objects Expected to be an {@link ExamActivity}.
     * @return The result.
     */
    @Override
    protected ExamActivity doInBackground(@Size(1) Object... objects) {
        ExamActivity activity = (ExamActivity) objects[0];
        activity.setLoadSuccessful(true);
        try {
            Exam parsedExam = ExamParser.getInstance().parseExam(examId, true, activity);
            Random rand = new Random(); //remove questions so only 'questionAmount' remains
            while(parsedExam.getQuestions().size() > parsedExam.getQuestionAmount()) {
                int randomIndex = rand.nextInt(parsedExam.getQuestions().size()); //question at this index is removed
                parsedExam.getQuestions().remove(randomIndex);
            }
            if(shuffleQuestions) {
                Collections.shuffle(parsedExam.getQuestions()); //shuffle question order
            }
            //save questions
            activity.getExam().setQuestions(parsedExam.getQuestions());
        } catch (Exception e) {
            LogUtils.logError("Exception while loading exam questions!", e);
            activity.setLoadSuccessful(false);
        }
        return activity;
    }

    /**
     * Sets up the {@link RecyclerView} that displays the questions.
     * @param activity The activity.
     */
    @Override
    protected void onPostExecute(ExamActivity activity) {
        if(activity.isLoadSuccessful()) {
            CountdownView countdownView = activity.findViewById(R.id.countdownView);
            countdownView.setOnCountdownEndListener(activity::onExamTimeExpired); //add countdown listeners
            countdownView.setOnCountdownIntervalListener(1000, activity::onExamTimeTicked);

            //set up recycler view
            RecyclerView questionsLayout = activity.findViewById(R.id.questionsLayout);
            QuestionAdapter adapter = new QuestionAdapter(activity.getExam().getQuestions(), activity);
            questionsLayout.setAdapter(adapter);

            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading
            activity.findViewById(R.id.examActivityContent).setVisibility(View.VISIBLE); //show questions
            if(SettingsActivity.isOnChallengingDifficulty(activity)) {
                long reducedTime = (activity.getExam().getTimeLimit() * 60 * 1000) - Exam.REDUCED_EXAM_TIME;
                countdownView.start(reducedTime); //reduced time in challenging difficulty
            } else {
                countdownView.start(activity.getExam().getTimeLimit() * 60 * 1000); //start timer
            }
        } else {
            activity.findViewById(R.id.loadingIndicator).setVisibility(View.GONE); //hide loading
            LogUtils.showLoadingFailDialog(activity, activity.getString(R.string.exam));
        }
    }
}
