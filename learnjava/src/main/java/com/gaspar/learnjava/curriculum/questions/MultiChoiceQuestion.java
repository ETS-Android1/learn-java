package com.gaspar.learnjava.curriculum.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a multi choice question.
 * <p>
 * Example for multi choice question XML structure:
 * <pre>
 * {@code
 * <question type="multi_choice">
 *      <text>*text of the question*</text>
 *      <answer>*an answer*</answer>
 *      ...
 *      <answer>*an answer*</answer>
 *      <correct>*index of correct answer*</correct>
 *      ...
 *      <correct>*index of another correct answer*</correct>
 * </question>
 * }
 * </pre>
 */
public class MultiChoiceQuestion extends Question implements Serializable {

    /**
     * Answers to this question.
     */
    private final List<String> answers;

    /**
     * Indices of the correct answers.
     */
    private final Set<Integer> correctAnswerIndices;

    /**
     * Indices of the selected answers. Initially empty.
     */
    private final Set<Integer> selectedAnswerIndices;

    /**
     * Creates a multi choice question object.
     * @param text Text of the question.
     * @param answers Possible answers to this question.
     * @param correctAnswerIndices Indices of the correct answers.
     * @throws IllegalArgumentException If the question is invalid, for example no correct answers.
     */
    public MultiChoiceQuestion(String text, List<String> answers, Set<Integer> correctAnswerIndices)
        throws IllegalArgumentException {
        super(QuestionType.MULTI_CHOICE, text);
        if(correctAnswerIndices.size()==0 || correctAnswerIndices.size() > answers.size() ) {
            throw new IllegalArgumentException("Invalid question!");
        }
        this.answers = answers;
        this.correctAnswerIndices = correctAnswerIndices;
        selectedAnswerIndices = new HashSet<>();
    }

    /**
     * Loads the individual answer views into the answer layout.
     * @param answersLayout The answer layout.
     */
    public void fillAnswersLayout(@NonNull final LinearLayout answersLayout) {
        answersLayout.removeAllViews();
        final LayoutInflater inflater = LayoutInflater.from(answersLayout.getContext());
        for(int i=0; i<answers.size(); i++) {
            CheckBox answer = (CheckBox) inflater.inflate(R.layout.answer_multi_choice, answersLayout, false);
            answer.setText(answers.get(i));
            answersLayout.addView(answer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnswered() {
        return !selectedAnswerIndices.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCorrect() {
        return selectedAnswerIndices.equals(correctAnswerIndices);
    }

    /**
     * @return A list of indices, of the currently selected answers.
     */
    public Set<Integer> getSelectedAnswerIndices() {
        return selectedAnswerIndices;
    }

    /**
     * Updates the view of this question to show the correct answer, and mark the user's answer
     * with red, if it was incorrect.
     * @param questionIcon The icon of the question.
     * @param answersLayout The layout where the answer views are.
     */
    public void showCorrectAnswer(@NonNull final ImageView questionIcon, @NonNull final LinearLayout answersLayout) {
        if(isCorrect()) {
            questionIcon.setImageResource(R.drawable.tick_icon);
            questionIcon.setTag(R.drawable.tick_icon);
        } else {
            questionIcon.setImageResource(R.drawable.problem_icon);
            questionIcon.setTag(R.drawable.problem_icon);
        }
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            if(correctAnswerIndices.contains(i)) { //mark the correct answers with green
                CheckBox checkBox = (CheckBox)answersLayout.getChildAt(i);
                checkBox.setBackgroundResource(R.drawable.correct_answer_background);
                checkBox.setTextColor(ContextCompat.getColor(checkBox.getContext(), android.R.color.black));
                checkBox.setButtonTintList(ContextCompat.getColorStateList(checkBox.getContext(), R.color.black_color_list));
            } else { //this is not a correct answer
                if(selectedAnswerIndices.contains(i)) { //wrong answer marked
                    CheckBox checkBox = (CheckBox)answersLayout.getChildAt(i);
                    checkBox.setBackgroundResource(R.drawable.incorrect_background);
                    checkBox.setTextColor(ContextCompat.getColor(checkBox.getContext(), android.R.color.black));
                    checkBox.setButtonTintList(ContextCompat.getColorStateList(checkBox.getContext(), R.color.black_color_list));
                }
            }
        }
    }

    /**
     * Disables all answer checkboxes.
     * @param answersLayout The layout where the answer views are.
     */
    public void lockQuestion(@NonNull final LinearLayout answersLayout) {
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            answersLayout.getChildAt(i).setEnabled(false);
        }
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for {@link MultiChoiceQuestion}. Used in
     * {@link com.gaspar.learnjava.adapters.QuestionAdapter}.
     */
    public static class MultiChoiceHolder extends RecyclerView.ViewHolder {

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
        public LinearLayout answersLayout;

        /**
         * Creates a view holder.
         * @param view This is expected to be inflated from R.layout.question_multi_choice.
         */
        public MultiChoiceHolder(View view) {
            super(view);
            questionTextView = view.findViewById(R.id.questionTextView);
            answersLayout = view.findViewById(R.id.answersLayout);
            questionIcon = view.findViewById(R.id.questionIcon);
            //can't change theme in exams, so this can be done once, here
            if(ThemeUtils.isDarkTheme()) {
                int accent = ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark_Dark);
                view.findViewById(R.id.questionSep1).setBackgroundColor(accent);
                view.findViewById(R.id.questionSep2).setBackgroundColor(accent);
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.question_background_dark));
            }
        }

    }
}
