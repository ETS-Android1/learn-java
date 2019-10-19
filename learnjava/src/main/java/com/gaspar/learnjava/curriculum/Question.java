package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;

import java.io.Serializable;

/**
 * <p>
 *   Represents an exam question. Exam questions can have the following types:
 *    <ul>
 *         <li>Single choice (single_choice, {@value SINGLE_CHOICE_TAG})</li>
 *         <li>Multi choice (multi_choice, {@value MULTI_CHOICE_TAG})</li>
 *         <li>True of false (true_or_false, {@value TRUE_FALSE_TAG})</li>
 *         <li>Text (text, {@value TEXT_TAG})</li>
 *    </ul>
 *   Each type has it's own subclass.
 * </p>
 *
 *  <p>
 *      Questions are stored in XML, in the exam XML files, inside question tags. To see the XML
 *      structure for each type of questions, see the subclasses' documentations.
 *  </p>
 *
 * @see SingleChoiceQuestion
 * @see MultiChoiceQuestion
 * @see TrueOrFalseQuestion
 * @see TextQuestion
 */
public abstract class Question implements Serializable {

    /**
     * Type of the question.
     */
    @QuestionType
    private int type;

    /**
     * Question text.
     */
    protected String text;

    /**
     * The view that displays this question. This isn't created when the question is, only later,
     * when the exam activity is loaded.
     */
    View questionView;

    Question(@QuestionType int type, String text) {
        this.type = type;
        this.text = text;
    }

    /**
     * Question type constants.
     */
    @IntDef({QuestionType.SINGLE_CHOICE, QuestionType.MULTI_CHOICE,
        QuestionType.TEXT, QuestionType.TRUE_OR_FALSE})
    public @interface QuestionType {
        int SINGLE_CHOICE = 0;
        int MULTI_CHOICE = 1;
        int TRUE_OR_FALSE = 2;
        int TEXT = 3;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    /**
     * Creates a view that displays this question and the possible answers. Selecting answers
     * using the view will modify the Question object. This method should assign {@link #questionView}.
     *
     * @param parent The view group this question view will be added. This method DOES NOT add the
     *               inflated view.
     * @return The created view.
     */
    public abstract View createQuestionView(Context context, ViewGroup parent);

    /**
     * @return True only if the user has selected an answer.
     */
    public abstract boolean isAnswered();

    /**
     * @return True only if the correct answer has been selected.
     */
    public abstract boolean isCorrect();

    /**
     * Uses {@link #questionView} to show if the selected answer was correct or not.
     */
    public abstract void showCorrectAnswer();

    /**
     * Locks the view of this question so the user can't change the selected answer anymore.
     */
    public abstract void lockQuestion();

    /**
     * Constants for the question type tags.
     */
    private static final String SINGLE_CHOICE_TAG = "single_choice",
    MULTI_CHOICE_TAG = "multi_choice", TRUE_FALSE_TAG = "true_false", TEXT_TAG = "text";

    /**
     * Converts strings to question type constants.
     *
     * @param typeString The string that will be converted.
     * @return The question type, or null if no type was found for the string.
     */
    public static int findTypeFromString(String typeString) {
        try {
            if(typeString.equalsIgnoreCase(SINGLE_CHOICE_TAG)) {
                return QuestionType.SINGLE_CHOICE;
            } else if(typeString.equalsIgnoreCase(MULTI_CHOICE_TAG)) {
                return QuestionType.MULTI_CHOICE;
            }  else if(typeString.equalsIgnoreCase(TRUE_FALSE_TAG)) {
                return QuestionType.TRUE_OR_FALSE;
            }  else if(typeString.equalsIgnoreCase(TEXT_TAG)) {
                return QuestionType.TEXT;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
