package com.gaspar.learnjava.curriculum.questions;

import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;

import java.io.Serializable;

/**
 * Represents an exam question. Exam questions can have the following types:
 * <ul>
 *         <li>Single choice (single_choice, {@value SINGLE_CHOICE_TAG})</li>
 *         <li>Multi choice (multi_choice, {@value MULTI_CHOICE_TAG})</li>
 *         <li>True of false (true_or_false, {@value TRUE_FALSE_TAG})</li>
 *         <li>Text (text, {@value TEXT_TAG})</li>
 * </ul>
 * Each type has it's own subclass.
 * <p>
 * Questions are stored in XML, in the exam XML files, inside question tags. To see the XML
 * structure for each type of questions, see the subclasses' documentations.
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
    private final int type;

    /**
     * Question text.
     */
    protected final String text;

    /**
     * Stores if this questions was locked and if it should display the answer. When the
     * {@link com.gaspar.learnjava.adapters.QuestionAdapter} sees this flag as true, it will
     * show the corrected version of a question. The exact meaning of showing corrected is
     * implemented in the subclasses, for example {@link MultiChoiceQuestion#showCorrectAnswer(ImageView, LinearLayout)}.
     */
    protected boolean displayAnswer;

    /**
     * Constructor for subclasses.
     * @param type Type of the question.
     * @param text Text of the question.
     */
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

    @QuestionType
    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    /**
     * @return True only if the user has selected or typed an answer.
     */
    public abstract boolean isAnswered();

    /**
     * @return True only if the correct answer has been selected or typed.
     */
    public abstract boolean isCorrect();

    /**
     * Constants for the question type tags.
     */
    private static final String SINGLE_CHOICE_TAG = "single_choice",
    MULTI_CHOICE_TAG = "multi_choice", TRUE_FALSE_TAG = "true_false", TEXT_TAG = "text";

    /**
     * Converts strings to question type constants.
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

    /**
     * @return If this question should display it's answer.
     */
    public boolean isDisplayAnswer() {
        return displayAnswer;
    }

    /**
     * Set if this question should be locked and display its answer.
     * @param displayAnswer True if it should display answer.
     */
    public void setDisplayAnswer(boolean displayAnswer) {
        this.displayAnswer = displayAnswer;
    }
}
