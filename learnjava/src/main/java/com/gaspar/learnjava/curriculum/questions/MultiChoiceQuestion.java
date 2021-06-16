package com.gaspar.learnjava.curriculum.questions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
     * {@inheritDoc}
     */
    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.question_multi_choice, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text);
        LinearLayout answersLayout = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answers.size(); i++) {
            View answerView = inflater.inflate(R.layout.answer_multi_choice, answersLayout, false);
            CheckBox cb = answerView.findViewById(R.id.answer);
            cb.setText(answers.get(i));
            final int fixedI = i;
            cb.setOnCheckedChangeListener((compoundButton,isChecked) -> {
                if(isChecked) { //remove or add to set depending on checked state
                    selectedAnswerIndices.add(fixedI);
                } else {
                    selectedAnswerIndices.remove(fixedI);
                }
            });
            answersLayout.addView(answerView);
        }
        if(ThemeUtils.isDarkTheme()) {
            recolorSeparators(questionView, context);
            questionView.setBackground(ContextCompat.getDrawable(context, R.drawable.question_background_dark));
        }
        return questionView;
    }

    /**
     * Sets a better color for separator lines, only if in dark mode.
     */
    private void recolorSeparators(View questionView, final Context context) {
        int accent = ContextCompat.getColor(context, R.color.colorAccent_Dark);
        questionView.findViewById(R.id.questionSep1).setBackgroundColor(accent);
        questionView.findViewById(R.id.questionSep2).setBackgroundColor(accent);
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
     * {@inheritDoc}
     */
    @Override
    public void showCorrectAnswer() {
        ImageView questionIcon = questionView.findViewById(R.id.questionIcon);
        if(isCorrect()) {
            questionIcon.setImageResource(R.drawable.tick_icon);
            questionIcon.setTag(R.drawable.tick_icon);
        } else {
            questionIcon.setImageResource(R.drawable.problem_icon);
            questionIcon.setTag(R.drawable.problem_icon);
        }
        LinearLayout answersLayout = questionView.findViewById(R.id.answersLayout);
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
     * {@inheritDoc}
     */
    @Override
    public void lockQuestion() {
        LinearLayout answersLayout = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            answersLayout.getChildAt(i).setEnabled(false);
        }
    }
}
