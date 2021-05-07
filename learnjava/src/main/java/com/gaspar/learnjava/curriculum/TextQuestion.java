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
import com.gaspar.learnjava.utils.ThemeUtils;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a question with text answers.
 * If the ignoreCase tag is present, then the question is not case sensitive.
 <p>
 *  Text question XML structure:
 *   {@code
 *   <resources>
 *           <question type="text">
 *           <text>*text of the question*</text>
 *           <correct>*An accepted answer*</correct>
 *           ...
 *           <correct>*Another accepted answer*</correct>
 *           <ignoreSpace/> (optional)
 *           <ignoreCase/> (optional)
 *    </question>
 *  </resources>
 *  }
 *  </p>
 *  <p>
 *      <h2>ignoreSpace tag</h2>
 *      If this tag is present, then spaces at certain positions are ignored. The positions where the user
 *      is allowed to place any number of spaces are marked with a space in the correct answer. For example:
 *      <br><br>
 *      {@code <correct>int[s]x = 5;</correct>}
 *      <br><br>
 *      In this case the user can place as many spaces between x, =, and 5 as they want. Some spaces in the answer
 *      can be made mandatory using {@link #DO_NOT_IGNORE_SPACE} ([s]). In the example above, the user must write
 *      a space between int and x.
 *  </p>
 */
public class TextQuestion extends Question implements Serializable {

    /**
     * If this character is in the answer, it will be treated as a required space even if spaces are
     * ignored. Useful for something like this: 'int[s]x = 5;
     * <br>
     * Must only be used in questions where spaces are ignored to mark a space as important.
     */
    private static final String DO_NOT_IGNORE_SPACE = Pattern.quote("[s]");

    /**
     * The texts that can be accepted as answers. The first one in the list will be shown as
     * an accepted answer.
     */
    private final List<String> correctAnswers;

    /**
     * The answer that the user has entered. Initially this is the empty string.
     */
    private String enteredAnswer;

    /**
     * Determines if this answer will ignore spaces or not.
     */
    private final boolean ignoreSpace;

    /**
     * Determines if the answer is case sensitive or not.
     */
    private final boolean ignoreCase;

    public TextQuestion(String text, List<String> correctAnswers, boolean ignoreSpace, boolean ignoreCase) {
        super(QuestionType.TEXT, text);
        if(!ignoreSpace && correctAnswers.stream().anyMatch(answer -> answer.contains(DO_NOT_IGNORE_SPACE))) {
            throw new RuntimeException("Important space marker used when not ignoring spaces!");
        }
        this.correctAnswers = correctAnswers;
        this.ignoreSpace = ignoreSpace;
        this.ignoreCase = ignoreCase;
        enteredAnswer = "";
    }

    @Override
    public View createQuestionView(Context context, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        questionView = inflater.inflate(R.layout.text_question, parent, false);
        ((TextView)questionView.findViewById(R.id.questionTextView)).setText(text);
        //this wont be visible on start
        //replace important space markers with actual space
        String correctText = correctAnswers.get(0);
        correctText = correctText.replace("[s]", " ");
        ((TextView)questionView.findViewById(R.id.possibleSolutionTextView)).setText(correctText);
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

    @Override
    public boolean isAnswered() {
        return !enteredAnswer.equals("");
    }

    /**
     * Checks if answer is correct taking into account possible ignore space and case.
     * @return True if the answer is correct.
     */
    @Override
    public boolean isCorrect() {
        //if we're ignoring spaces, remove them from the accepted answers as well when checking
        for(String correctAnswer: correctAnswers) {
            if(ignoreSpace) {
                //create a regex based on this possible correct answer.
                final StringBuilder regexBuilder = new StringBuilder();
                //split up the answer between the 'words'
                String[] splitAnswer = correctAnswer.split("\\s+"); //split on spaces (on ignorable spaces)
                for(String part: splitAnswer) {
                    regexBuilder.append(Pattern.quote(part)).append(" *");
                }
                regexBuilder.setLength(regexBuilder.length()-2); //cut last unnecessary " *"
                String answerRegexString = regexBuilder.toString().replaceAll(DO_NOT_IGNORE_SPACE, " ");
                //compile regex pattern
                Pattern answerRegex;
                if(ignoreCase) {
                    answerRegex = Pattern.compile(answerRegexString, Pattern.CASE_INSENSITIVE);
                } else {
                    answerRegex = Pattern.compile(answerRegexString);
                }
                //possible case insensitivity is taken care of
                if(answerRegex.matcher(enteredAnswer).matches()) return true;
            } else {
                if(ignoreCase) {
                    if(correctAnswer.equalsIgnoreCase(enteredAnswer)) return true;
                } else {
                    if(correctAnswer.equals(enteredAnswer)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Displays the result. If the answer is correct, then highlights it with green. If it is incorrect, then
     * highlights it with red and reveals the 'correct answer' part of the question.
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
