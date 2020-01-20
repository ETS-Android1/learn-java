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
import com.gaspar.learnjava.ThemeUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a single choice question.
 *
 * <p>
 *  Example for single choice question XML structure:
 *  {@code
 *  <resources>
 *          <question type="single_choice">
 *          <text>*text of the question*</text>
 *          <answer>*an answer*</answer>
 *          ...
 *          <answer>*an answer*</answer>
 *          <correct>*index of correct answer*</correct>
 *     </question>
 *  </resources>
 *  }
 * </p>
 *
 * @see Question
 */
public class SingleChoiceQuestion extends Question implements Serializable {

    /**
     * Answers of the question.
     */
    private List<String> answers;

    /**
     * Index of the correct answer.
     */
    private final int correctAnswerIndex;

    /**
     * Constant that indicated that no answer has been selected.
     */
    private static final int NO_ANSWER_SELECTED = -1;

    /**
     * The index of the answer that the user has selected. By default this is {@value #NO_ANSWER_SELECTED}.
     */
    private int selectedAnswerIndex;

    public SingleChoiceQuestion(String text, List<String> answers, int correctAnswerIndex) {
        super(QuestionType.SINGLE_CHOICE, text);
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
        this.selectedAnswerIndex = NO_ANSWER_SELECTED;
    }

    /**
     * Creates a question view specially for single choice questions.
     *
     * @param parent The view group this question view will be added. This method DOES NOT add the
     *               inflated view.
     */
    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.single_choice_question, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text); //set question text
        RadioGroup answersGroup = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answers.size(); i++) { //add answer radio buttons
            View inflatedAnswer = inflater.inflate(R.layout.single_choice_answer, answersGroup, false);
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
            questionView.setBackground(context.getDrawable(R.drawable.question_background_dark));
        }
        return questionView;
    }

    /**
     * Sets a better color for separator lines, only if in dark mode.
     */
    private void recolorSeparator(View questionView, final Context context) {
        int accent = context.getResources().getColor(R.color.colorAccent_Dark);
        questionView.findViewById(R.id.questionSep1).setBackgroundColor(accent);
    }

    @Override
    public boolean isAnswered() {
        return selectedAnswerIndex != NO_ANSWER_SELECTED;
    }

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
            } else { //incorrect
                iconView.setImageResource(R.drawable.problem_icon);
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

    @Override
    public void lockQuestion() {
        RadioGroup answersLayout = questionView.findViewById(R.id.answersLayout);
        for(int i=0; i<answersLayout.getChildCount(); i++) {
            answersLayout.getChildAt(i).setEnabled(false);
        }
    }
}
