package com.gaspar.learnjava.curriculum.interactive;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.gaspar.learnjava.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents a spot where the user can enter input inside an interactive code sample.
 * It can have an optional default value instead of the empty start.
 */
public class EmptySpace {

    /**
     * The "index" of this empty space, starts from 0. It's use to later bind the EmptySpace objects and their views.
     */
    private final int place;
    /**
     * Contains the answers accepted by this empty space.
     */
    private final List<EmptySpaceAnswer> answers;
    /**
     * The view of this empty space. This is bound later, is created from "empty_space_view.xml".
     */
    private View emptySpaceView;
    /**
     * The optional default text. If it's present than this will be shown by default, not an empty EditText.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> defaultText;

    private EmptySpace(int place, EmptySpaceAnswer answer) { //use list builder below to create
        this.place = place;
        this.answers = new ArrayList<>();
        answers.add(answer);
        defaultText = Optional.empty();
    }

    int getPlace() {
        return place;
    }

    public List<EmptySpaceAnswer> getAnswers() {
        return answers;
    }
    /**
     * Called when the view for this empty space is being created.
     */
    void bindView(final View view) {
        this.emptySpaceView = view;
    }
    /** Returns the text that is currently in the edit text of this empty space */
    String getTextInEmptySpace() {
        final EditText input = emptySpaceView.findViewById(R.id.inputField);
        return input.getText().toString();
    }
    /** checks if the edit text has an acceptable answer. Takes the compatibility with other answers into account. */
    private boolean isCorrect(@NonNull final List<EmptySpace> emptySpaces) {
        final EditText input = emptySpaceView.findViewById(R.id.inputField);
        for(EmptySpaceAnswer answer: answers) {
            if(answer.toString().equals(input.getText().toString())) { //this is a correct answer. But is it compatible?
                return answer.compatibleWithOtherAnswers(emptySpaces);
            }
        }
        return false;
    }
    /** checks if the entered answer is correct (also disables input) */
    boolean checkSolution(@NonNull final Context context, @NonNull final List<EmptySpace> emptySpaces) {
        if(emptySpaceView == null) throw new RuntimeException("View not bound!");
        final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
        inputField.setEnabled(false);
        if(isCorrect(emptySpaces)) {
            inputField.setTextColor(context.getColor(android.R.color.black));
            inputField.setBackgroundResource(R.drawable.correct_answer_background);
            return true;
        } else { //incorrect, show red background and help icon
            inputField.setTextColor(context.getColor(android.R.color.black));
            inputField.setBackgroundResource(R.drawable.incorrect_background);
            emptySpaceView.findViewById(R.id.showSolutionButton).setVisibility(View.VISIBLE);
            return false;
        }
    }
    /** clears text and colored background (also enables it) */
    void resetEmptySpace(@NonNull final Context context) {
        if(emptySpaceView == null) throw new RuntimeException("View not bound!");
        final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
        inputField.setText("");
        //reset to original style
        inputField.setBackgroundResource(R.drawable.edit_text_line_background);
        inputField.setTextColor(context.getColor(R.color.code_text_color));
        inputField.setEnabled(true);
        emptySpaceView.findViewById(R.id.showSolutionButton).setVisibility(View.GONE);
    }
    /** shows correct solution (the first one) */
    void showSolution(@NonNull final Context context) {
        resetEmptySpace(context);
        final EditText inputField = emptySpaceView.findViewById(R.id.inputField);
        inputField.setText(answers.get(0).toString());
    }
    /** appends a correct answer to this empty space's answer list */
    private void addAnswer(EmptySpaceAnswer answer) {
        answers.add(answer);
    }
    /** set the default text */
    private void setDefaultText(String defaultText) {
        this.defaultText = Optional.of(defaultText);
    }
    Optional<String> getDefaultText() {
        return defaultText;
    }

    /**
     * Can build a list of EmptySpace objects from XML answer tags. Used when parsing interactive components.
     */
    public static class EmptySpaceListBuilder {

        private List<EmptySpace> emptySpaces;

        public EmptySpaceListBuilder() {
            emptySpaces = new ArrayList<>();
        }

        /**
         * Register a possible answer for an empty space.
         * @param place Place of the empty space.
         * @param answer The possible answer.
         */
        public void addEmptySpaceAnswer(int place, @NonNull final EmptySpaceAnswer answer) {
            boolean wasPresent = false;
            for(EmptySpace emptySpace: emptySpaces) {
                if(emptySpace.getPlace() == place) { //there is an empty space with this place already
                    emptySpace.addAnswer(answer);
                    wasPresent = true;
                    break;
                }
            }
            if(!wasPresent) { //did not see this place before
                emptySpaces.add(new EmptySpace(place, answer));
            }
        }

        /**
         * Set a default answer for an empty space.
         * @param place Place of the empty space.
         * @param defaultAnswer The default text.
         */
        public void addDefaultText(int place, @NonNull String defaultAnswer) {
            boolean wasPresent = false;
            for(EmptySpace emptySpace: emptySpaces) {
                if(emptySpace.getPlace() == place) { //this is the empty space
                    emptySpace.setDefaultText(defaultAnswer);
                    wasPresent = true;
                    break;
                }
            }
            if(!wasPresent) { //this place had no matching empty space
                throw new RuntimeException("DEFAULT tags must be after the answer tags!");
            }
        }

        /**
         * Stops the parsing of an interactive components.
         * @return The empty spaces found while parsing.
         */
        public List<EmptySpace> finishBuilding() {
            return emptySpaces;
        }
    }
}
