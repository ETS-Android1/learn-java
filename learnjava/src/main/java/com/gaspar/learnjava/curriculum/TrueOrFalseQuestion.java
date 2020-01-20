package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.ThemeUtils;

import java.io.Serializable;

/**
 * Represents a true of false question.
 *
 * <p>
 *  Example for a true-or-false question XML structure:
 *  {@code
 *  <resources>
 *         <question type="true_false">
 *         <text>*text of the question*</text>
 *         <correct>true OR false</correct>
 *     </question>
 *  </resources>
 *  }
 *  </p>
 */
public class TrueOrFalseQuestion extends Question implements Serializable {

    /**
     * Stores if the answer is true or not.
     */
    private boolean trueAnswer;

    /**
     * Constants for storing the selected value.
     */
    @IntDef({SelectedValues.FALSE, SelectedValues.TRUE, SelectedValues.NOT_SELECTED})
    @interface SelectedValues {
        int TRUE = 1;
        int FALSE = 0;
        int NOT_SELECTED = -1;
    }

    /**
     * The value of the answer the user has selected. Initially, this is {@value SelectedValues#NOT_SELECTED}.
     */
    @SelectedValues
    private int selectedValue;

    public TrueOrFalseQuestion(String text, boolean trueAnswer) {
        super(QuestionType.TRUE_OR_FALSE, text);
        this.trueAnswer = trueAnswer;
        selectedValue = SelectedValues.NOT_SELECTED;
    }

    /**
     * Played when selecting an answer.
     */
    private Animation tickAnimation;

    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.true_false_question, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text);

        tickAnimation = AnimationUtils.loadAnimation(context, R.anim.tick);
        TextView trueTextView = questionView.findViewById(R.id.trueTextView);
        TextView falseTextView = questionView.findViewById(R.id.falseTextView);
        trueTextView.setOnClickListener((view) -> {
            selectedValue = SelectedValues.TRUE;
            trueTextView.setBackgroundResource(R.drawable.selected_background);
            falseTextView.setBackgroundResource(R.drawable.unselected_background);
            trueTextView.startAnimation(tickAnimation);
        });
        falseTextView.setOnClickListener((view) -> {
            selectedValue = SelectedValues.FALSE;
            falseTextView.startAnimation(tickAnimation);
            falseTextView.setBackgroundResource(R.drawable.selected_background);
            trueTextView.setBackgroundResource(R.drawable.unselected_background);
        });
        if(ThemeUtils.isDarkTheme()) { //additional styling on dark theme
            int black = ContextCompat.getColor(context, android.R.color.black);
            trueTextView.setTextColor(black);
            falseTextView.setTextColor(black);
            questionView.setBackground(context.getDrawable(R.drawable.question_background_dark));
        }
        return questionView;
    }

    @Override
    public boolean isAnswered() {
        return selectedValue != SelectedValues.NOT_SELECTED;
    }

    @Override
    public boolean isCorrect() {
        if(trueAnswer) {
            return selectedValue == SelectedValues.TRUE;
        } else {
            return selectedValue == SelectedValues.FALSE;
        }
    }

    @Override
    public void showCorrectAnswer() {
        TextView trueTextView = questionView.findViewById(R.id.trueTextView);
        TextView falseTextView = questionView.findViewById(R.id.falseTextView);
        (trueAnswer ? trueTextView : falseTextView).setBackgroundResource(R.drawable.correct_answer_background);
        if(!isCorrect() && isAnswered()) { //user marked the wrong answer
            (trueAnswer ? falseTextView : trueTextView).setBackgroundResource(R.drawable.incorrect_background);
        }
    }

    @Override
    public void lockQuestion() {
        TextView trueTextView = questionView.findViewById(R.id.trueTextView);
        TextView falseTextView = questionView.findViewById(R.id.falseTextView);
        trueTextView.setEnabled(false);
        falseTextView.setEnabled(false);
    }
}
