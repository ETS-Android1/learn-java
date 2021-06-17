package com.gaspar.learnjava.curriculum.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a single choice question.
 * <p>
 * Example for single choice question XML structure:
 * <pre>
 * {@code
 * <question type="single_choice">
 *      <text>*text of the question*</text>
 *      <answer>*an answer*</answer>
 *      ...
 *      <answer>*an answer*</answer>
 *      <correct>*index of correct answer*</correct>
 * </question>
 * }
 * </pre>
 * @see Question
 */
public class SingleChoiceQuestion extends Question implements Serializable {

    /**
     * Answers of the question.
     */
    private final List<String> answers;

    /**
     * Index of the correct answer, 0 based.
     */
    private final int correctAnswerIndex;

    /**
     * Constant that indicates that no answer has been selected.
     */
    private static final int NO_ANSWER_SELECTED = -1;

    /**
     * The index of the answer that the user has selected. By default this is {@value #NO_ANSWER_SELECTED}.
     */
    private int selectedAnswerIndex;

    /**
     * Creates a single choice question object.
     * @param text Text of the question.
     * @param answers List of possible answers.
     * @param correctAnswerIndex The index of the current answer, 0 based.
     */
    public SingleChoiceQuestion(String text, List<String> answers, int correctAnswerIndex) {
        super(QuestionType.SINGLE_CHOICE, text);
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
        this.selectedAnswerIndex = NO_ANSWER_SELECTED;
    }

    /**
     * Loads the individual answer views into the answer layout.
     * @param answersLayout The answer layout.
     */
    public void fillAnswersLayout(@NonNull final RadioGroup answersLayout) {
        answersLayout.removeAllViews();
        final LayoutInflater inflater = LayoutInflater.from(answersLayout.getContext());
        for(int i=0; i<answers.size(); i++) { //add answer radio buttons
            RadioButton answer = (RadioButton) inflater.inflate(R.layout.answer_single_choice, answersLayout, false);
            answer.setText(answers.get(i));
            answer.setId(View.generateViewId());
            answersLayout.addView(answer, i);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnswered() {
        return selectedAnswerIndex != NO_ANSWER_SELECTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCorrect() {
        return selectedAnswerIndex == correctAnswerIndex;
    }

    /**
     * @return The index of the currently selected answer.
     */
    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }

    /**
     * Updates the saved selected answer.
     * @param selectedAnswerIndex The new answer index.
     */
    public void setSelectedAnswerIndex(int selectedAnswerIndex) {
        this.selectedAnswerIndex = selectedAnswerIndex;
    }

    /**
     * Updates the view of this question to show the correct answer, and mark the user's answer
     * with red, if it was incorrect.
     * @param questionIcon The icon of the question.
     * @param answersLayout The layout where the answer views are.
     */
    public void showCorrectAnswer(@NonNull final ImageView questionIcon, @NonNull final RadioGroup answersLayout) {
        //correct answer marked regardless on answer
        RadioButton correctButton = (RadioButton) answersLayout.getChildAt(correctAnswerIndex);
        correctButton.setBackgroundResource(R.drawable.correct_answer_background);
        correctButton.setTextColor(ContextCompat.getColor(correctButton.getContext(), android.R.color.black));
        correctButton.setButtonTintList(ContextCompat.getColorStateList(correctButton.getContext(), R.color.black_color_list));
        if(isAnswered()) { //question as been answered
            if(isCorrect()) { //answered correctly
                questionIcon.setImageResource(R.drawable.tick_icon);
                questionIcon.setTag(R.drawable.tick_icon);
            } else { //incorrect
                questionIcon.setImageResource(R.drawable.problem_icon);
                questionIcon.setTag(R.drawable.problem_icon);
                //mark incorrect answer with red
                RadioButton wrongButton = (RadioButton)answersLayout.getChildAt(selectedAnswerIndex);
                wrongButton.setBackgroundResource(R.drawable.incorrect_background);
                wrongButton.setTextColor(ContextCompat.getColor(correctButton.getContext(), android.R.color.black));
                wrongButton.setButtonTintList(ContextCompat.getColorStateList(correctButton.getContext(), R.color.black_color_list));
            }
        } else { //question has not been answered
            questionIcon.setImageResource(R.drawable.problem_icon);
        }
    }

    /**
     * Disables all answer checkboxes.
     * @param answersLayout The layout where the answer views are.
     */
    public void lockQuestion(@NonNull final RadioGroup answersLayout) {
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            answersLayout.getChildAt(i).setEnabled(false);
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for {@link SingleChoiceQuestion}. Used in
     * {@link com.gaspar.learnjava.adapters.QuestionAdapter}.
     */
    public static class SingleChoiceHolder extends RecyclerView.ViewHolder {

        /**
         * Icon of the question.
         */
        public ImageView questionIcon;

        /**
         * Displays the text of the question.
         */
        public TextView questionTextView;

        /**
         * Displays the answers of the question.
         */
        public RadioGroup answersLayout;

        /**
         * Creates a view holder.
         * @param view This is expected to be inflated from R.layout.question_single_choice.
         */
        public SingleChoiceHolder(View view) {
            super(view);
            questionTextView = view.findViewById(R.id.questionTextView);
            answersLayout = view.findViewById(R.id.answersLayout);
            questionIcon = view.findViewById(R.id.questionIcon);
            //in dark theme some extra formatting is needed
            if(ThemeUtils.isDarkTheme()) {
                int accent = ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark_Dark);
                view.findViewById(R.id.questionSep1).setBackgroundColor(accent);
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.question_background_dark));
            }
        }

    }
}
