package com.gaspar.learnjava.curriculum.questions;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.utils.ThemeUtils;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a question with text answers.
 * If the ignoreCase tag is present, then the question is not case sensitive.
 * <p>
 * Text question XML structure:
 * <pre>
 * {@code
 * <question type="text">
 *      <text>*text of the question*</text>
 *      <correct>*An accepted answer*</correct>
 *      ...
 *      <correct>*Another accepted answer*</correct>
 *      <ignoreSpace/> (optional)
 *      <ignoreCase/> (optional)
 * </question>
 * }
 * </pre>
 * <p>
 * <h2>ignoreSpace tag</h2>
 * If this tag is present, then spaces at certain positions are ignored. The positions where the user
 * is allowed to place any number of spaces are marked with a space in the correct answer. For example:
 * <br><br>
 * {@code <correct>int[s]x = 5;</correct>}
 * <br><br>
 * In this case the user can place as many spaces between x, =, and 5 as they want. Some spaces in the answer
 * can be made mandatory using {@link #DO_NOT_IGNORE_SPACE} ([s]). In the example above, the user must write
 * a space between int and x.
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

    /**
     * Creates a text question object.
     * @param text Text of the question.
     * @param correctAnswers Accepted answers.
     * @param ignoreSpace If the question ignores space differences, except where it is important.
     * @param ignoreCase If the question ignores case differences.
     * @throws RuntimeException If the question is invalid.
     */
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

    /**
     * {@inheritDoc}
     */
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
     * @param holder An object which caches all the views required.
     */
    @UiThread
    public void showCorrectAnswer(@NonNull final TextQuestionHolder holder) {
        //format the correct answer to be displayable, and display it
        String correctText = correctAnswers.get(0);
        correctText = correctText.replace("[s]", " ");
        TextView possibleSolutionView = holder.possibleSolutionLayout.findViewById(R.id.possibleSolutionTextView);
        possibleSolutionView.setText(correctText);

        final Context context = holder.answerEditText.getContext();
        if(ThemeUtils.isDarkTheme()) { //recolor text on dark theme for better visibility
            holder.answerDisplayer.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
        holder.answerDisplayer.setText(holder.answerEditText.getText());
        holder.answerDisplayer.setVisibility(View.VISIBLE);
        holder.answerEditText.setVisibility(View.GONE);
        if(isCorrect()) {
            holder.questionIcon.setImageResource(R.drawable.tick_icon);
            holder.answerDisplayer.setBackgroundResource(R.drawable.correct_answer_background);
            holder.answerDisplayer.setTag(R.drawable.correct_answer_background); //tag is used in testing
        } else {
            if(!isAnswered()) holder.answerDisplayer.setText(context.getString(R.string.no_answer_given));
            holder.questionIcon.setImageResource(R.drawable.problem_icon);
            holder.answerDisplayer.setBackgroundResource(R.drawable.incorrect_background);
            holder.answerDisplayer.setTag(R.drawable.incorrect_background); //tag is used in testing
            holder.possibleSolutionLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Disables the answer edit text.
     * @param answerEditText The edit text.
     */
    public void lockQuestion(@NonNull final EditText answerEditText) {
        answerEditText.setEnabled(false);
    }

    /**
     * Updates the answer that this text question is saving.
     * @param enteredAnswer The new answer.
     */
    public void setEnteredAnswer(String enteredAnswer) {
        this.enteredAnswer = enteredAnswer;
    }

    /**
     * @return The answer that the user entered.
     */
    public String getEnteredAnswer() {
        return enteredAnswer;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for {@link TextQuestion}. Used in
     * {@link com.gaspar.learnjava.adapters.QuestionAdapter}.
     */
    public static class TextQuestionHolder extends RecyclerView.ViewHolder {

        /**
         * Icon of the question.
         */
        public ImageView questionIcon;

        /**
         * Displays the text of the question.
         */
        public TextView questionTextView;

        /**
         * The user types in this to enter answer.
         */
        public EditText answerEditText;

        /**
         * Helper text view.
         */
        public TextView answerDisplayer;

        /**
         * Displays a correct solution.
         */
        public LinearLayout possibleSolutionLayout;

        /**
         * Creates a view holder.
         *
         * @param view This is expected to be inflated from R.layout.question_text.
         */
        public TextQuestionHolder(View view) {
            super(view);
            questionTextView = view.findViewById(R.id.questionTextView);
            questionIcon = view.findViewById(R.id.questionIcon);
            answerEditText = view.findViewById(R.id.answerEditText);
            answerDisplayer = view.findViewById(R.id.answerDisplayerTextView);
            possibleSolutionLayout = view.findViewById(R.id.solutionDisplayerLayout);
            //can't change theme in exams, so this can be done once, here
            if (ThemeUtils.isDarkTheme()) {
                int accent = ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark_Dark);
                view.findViewById(R.id.questionSep1).setBackgroundColor(accent);
                view.findViewById(R.id.questionSep2).setBackgroundColor(accent);
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.question_background_dark));
            }
        }
    }
}
