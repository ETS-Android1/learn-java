package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
     * {@inheritDoc}
     */
    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.question_single_choice, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text); //set question text
        RadioGroup answersGroup = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answers.size(); i++) { //add answer radio buttons
            View inflatedAnswer = inflater.inflate(R.layout.answer_single_choice, answersGroup, false);
            RadioButton answerButton = inflatedAnswer.findViewById(R.id.answer);
            answerButton.setText(answers.get(i));
            answerButton.setId(View.generateViewId());
            final int fixedI = i;
            answerButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if(isChecked) selectedAnswerIndex = fixedI;
            });
            answersGroup.addView(answerButton, i);
        }
        if(ThemeUtils.isDarkTheme()) {
            recolorSeparator(questionView, context);
            questionView.setBackground(ContextCompat.getDrawable(context, R.drawable.question_background_dark));
        }
        return questionView;
    }

    /**
     * Sets a better color for separator lines, only if in dark mode.
     */
    private void recolorSeparator(View questionView, final Context context) {
        int accent = ContextCompat.getColor(context, R.color.colorAccent_Dark);
        questionView.findViewById(R.id.questionSep1).setBackgroundColor(accent);
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
     * Highlights the correct answer in the question view with green. If the user has selected an
     * incorrect answer, that will be highlighted with red.
     */
    @Override
    public void showCorrectAnswer() {
        ImageView iconView = questionView.findViewById(R.id.questionIcon);
        RadioGroup answersLayout = questionView.findViewById(R.id.answersLayout);
        //correct answer marked regardless on answer
        RadioButton correctButton = (RadioButton) answersLayout.getChildAt(correctAnswerIndex);
        correctButton.setBackgroundResource(R.drawable.correct_answer_background);
        correctButton.setTextColor(ContextCompat.getColor(correctButton.getContext(), android.R.color.black));
        correctButton.setButtonTintList(ContextCompat.getColorStateList(correctButton.getContext(), R.color.black_color_list));
        if(isAnswered()) { //question as been answered
            if(isCorrect()) { //answered correctly
                iconView.setImageResource(R.drawable.tick_icon);
                iconView.setTag(R.drawable.tick_icon);
            } else { //incorrect
                iconView.setImageResource(R.drawable.problem_icon);
                iconView.setTag(R.drawable.problem_icon);
                //mark incorrect answer with red
                RadioButton wrongButton = (RadioButton)answersLayout.getChildAt(selectedAnswerIndex);
                wrongButton.setBackgroundResource(R.drawable.incorrect_background);
                wrongButton.setTextColor(ContextCompat.getColor(correctButton.getContext(), android.R.color.black));
                wrongButton.setButtonTintList(ContextCompat.getColorStateList(correctButton.getContext(), R.color.black_color_list));
            }
        } else { //question has not been answered
            iconView.setImageResource(R.drawable.problem_icon);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lockQuestion() {
        RadioGroup answersLayout = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            answersLayout.getChildAt(i).setEnabled(false);
        }
    }
}
