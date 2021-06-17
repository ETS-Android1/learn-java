package com.gaspar.learnjava.curriculum.questions;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.io.Serializable;

/**
 * Represents a true of false question.
 * <p>
 * Example for a true-or-false question XML structure:
 * <pre>
 * {@code
 * <question type="true_false">
 *     <text>*text of the question*</text>
 *     <correct>true OR false</correct>
 * </question>
 * }
 * </pre>
 */
public class TrueOrFalseQuestion extends Question implements Serializable {

    /**
     * Stores if the answer is true or not.
     */
    private final boolean trueAnswer;

    /**
     * Constants for storing the selected value.
     */
    @IntDef({SelectedValues.FALSE, SelectedValues.TRUE, SelectedValues.NOT_SELECTED})
    public @interface SelectedValues {
        int TRUE = 1;
        int FALSE = 0;
        int NOT_SELECTED = -1;
    }

    /**
     * The value of the answer the user has selected. Initially, this is {@value SelectedValues#NOT_SELECTED}.
     */
    @SelectedValues
    private int selectedValue;

    /**
     * Creates a true-false question object.
     * @param text The text of the question.
     * @param trueAnswer If the answer is true.
     */
    public TrueOrFalseQuestion(String text, boolean trueAnswer) {
        super(QuestionType.TRUE_OR_FALSE, text);
        this.trueAnswer = trueAnswer;
        selectedValue = SelectedValues.NOT_SELECTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnswered() {
        return selectedValue != SelectedValues.NOT_SELECTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCorrect() {
        if(trueAnswer) {
            return selectedValue == SelectedValues.TRUE;
        } else {
            return selectedValue == SelectedValues.FALSE;
        }
    }

    /**
     * Highlights the correct answer and marks incorrect answer with red.
     * @param trueTextView Text view that displays TRUE.
     * @param falseTextView Text view that displays FALSE.
     * @param questionIcon Icon of the question.
     */
    public void showCorrectAnswer(@NonNull final TextView trueTextView, @NonNull final TextView falseTextView, @NonNull ImageView questionIcon) {
        (trueAnswer ? trueTextView : falseTextView).setBackgroundResource(R.drawable.correct_answer_background);
        if(!isCorrect() && isAnswered()) { //user marked the wrong answer
            (trueAnswer ? falseTextView : trueTextView).setBackgroundResource(R.drawable.incorrect_background);
        }
        if (isCorrect()) {
            questionIcon.setImageResource(R.drawable.tick_icon);
            questionIcon.setTag(R.drawable.tick_icon);
        } else {
            questionIcon.setImageResource(R.drawable.problem_icon);
            questionIcon.setTag(R.drawable.problem_icon);
        }
    }

    /**
     * Locks the question so the answer can't be changed anymore.
     * @param trueTextView Text view that displays TRUE.
     * @param falseTextView Text view that displays FALSE.
     */
    public void lockQuestion(@NonNull final TextView trueTextView, @NonNull final TextView falseTextView) {
        trueTextView.setEnabled(false);
        falseTextView.setEnabled(false);
    }

    /**
     * Updates the selected value of the true false question.
     * @param selectedValue The selected value, one of {@link SelectedValues} constants.
     */
    public void setSelectedValue(@SelectedValues int selectedValue) {
        this.selectedValue = selectedValue;
    }

    /**
     * @return The selected value, one of {@link SelectedValues}.
     */
    @SelectedValues
    public int getSelectedValue() {
        return selectedValue;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for {@link TrueOrFalseQuestion}. Used in
     * {@link com.gaspar.learnjava.adapters.QuestionAdapter}.
     */
    public static class TrueFalseHolder extends RecyclerView.ViewHolder {

        /**
         * Icon of the question.
         */
        public ImageView questionIcon;

        /**
         * Displays the text of the question.
         */
        public TextView questionTextView;

        /**
         * Text view that displays TRUE.
         */
        public TextView trueTextView;

        /**
         * Text view that displays FALSE.
         */
        public TextView falseTextView;

        /**
         * Creates a view holder.
         * @param view This is expected to be inflated from R.layout.question_true_false.
         */
        public TrueFalseHolder(View view) {
            super(view);
            questionTextView = view.findViewById(R.id.questionTextView);
            questionIcon = view.findViewById(R.id.questionIcon);
            trueTextView = view.findViewById(R.id.trueTextView);
            falseTextView = view.findViewById(R.id.falseTextView);
            //can't change theme in exams, so this can be done once, here
            if(ThemeUtils.isDarkTheme()) { //additional styling on dark theme
                int black = ContextCompat.getColor(view.getContext(), android.R.color.black);
                trueTextView.setTextColor(black);
                falseTextView.setTextColor(black);
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.question_background_dark));
            }
        }

    }
}
