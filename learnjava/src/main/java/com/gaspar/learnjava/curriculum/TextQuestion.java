package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.ThemeUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a question with text answers.
 *
 <p>
 *  Example for text question XML structure:
 *   {@code
 *   <resources>
 *           <question type="text">
 *           <text>*text of the question*</text>
 *           <correct>*An accepted answer*</correct>
 *           ...
 *           <correct>*Another accepted answer*</correct>
 *    </question>
 *  </resources>
 *  }
 *  </p>
 */
public class TextQuestion extends Question implements Serializable {

    /**
     * The texts that can be accepted as answers. The first one in the list will be shown as
     * an accepted answer.
     */
    private List<String> correctAnswers;

    /**
     * The answer that the user has entered. Initially this is the empty string.
     */
    private String enteredAnswer;

    public TextQuestion(String text, List<String> correctAnswers) {
        super(QuestionType.TEXT, text);
        this.correctAnswers = correctAnswers;
        enteredAnswer = "";
    }

    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.text_question, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text);
        //this wont be visible on start
        ((TextView)questionView.findViewById(R.id.possibleSolutionTextView)).setText(correctAnswers.get(0));
        //TODO: add a listener to the edit text to update entered answer
        EditText answerEditText = questionView.findViewById(R.id.answerEditText);
        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enteredAnswer = charSequence.toString().trim();
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        if(ThemeUtils.isDarkTheme()) {
            recolorSeparators(questionView, context);
            questionView.setBackground(context.getDrawable(R.drawable.question_background_dark));
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

    @Override
    public boolean isAnswered() {
        return !enteredAnswer.equals("");
    }

    @Override
    public boolean isCorrect() {
        /*
        for(String correctAnswer: correctAnswers) { //don't care about lower or upper case
            if(correctAnswer.equalsIgnoreCase(enteredAnswer)) return true;
        }
        return false;
        */
        return correctAnswers.contains(enteredAnswer);
    }

    /**
     * Reveals the correct answer part of the question.
     */
    @Override
    @UiThread
    public void showCorrectAnswer() {
        EditText answerEditText = questionView.findViewById(R.id.answerEditText);
        //using this extra text view is the only way I found to show correct color...
        TextView answerDisplayer = questionView.findViewById(R.id.answerDisplayerTextView);
        final Context context = questionView.getContext();
        if(ThemeUtils.isDarkTheme()) { //recolor text on dark theme for better visibility
            answerDisplayer.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
        answerDisplayer.setText(answerEditText.getText());
        answerDisplayer.setVisibility(View.VISIBLE);
        answerEditText.setVisibility(View.GONE);
        if(isCorrect()) {
            ((ImageView)questionView.findViewById(R.id.questionIcon)).setImageResource(R.drawable.tick_icon);
            answerDisplayer.setBackgroundResource(R.drawable.correct_answer_background);
        } else {
            if(!isAnswered()) answerDisplayer.setText(context.getString(R.string.no_answer_given));
            ((ImageView)questionView.findViewById(R.id.questionIcon)).setImageResource(R.drawable.problem_icon);
            answerDisplayer.setBackgroundResource(R.drawable.incorrect_background);
            questionView.findViewById(R.id.solutionDisplayerLayout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void lockQuestion() {
        questionView.findViewById(R.id.answerEditText).setEnabled(false);
    }
}
