package com.gaspar.learnjava.curriculum.interactive;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A possible answer for an empty space of an interactive component. An answer can have a 'group' (a string).
 * It can also require that an empty space at another place has the answer also in this group.
 * <p>
 * This is so that it's possible to have answer groups which are only accepted together. See the last
 * interactive component of chapter_54 for example.
 */
public class EmptySpaceAnswer {
    /**
     * The actual answer.
     */
    private String answer;
    /**
     * If this is present, then this answer requires the empty spaces at the given places to
     * have the same group as this answer.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<Integer>> requiredPlaces;
    /**
     * Group of this answer. This is only present if {@link #requiredPlaces} contains some places.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> group;

    private EmptySpaceAnswer() {} //use builder to create
    /**
     * Checks if this answer has any requirements about other empty spaces and their answers.
     */
    private boolean hasRequirements() {
        return requiredPlaces.isPresent();
    }
    /**
     * The key method of this class. It takes all empty spaces, and checks their content/answers if
     * they meet the group requirements.
     * <p>
     * If this answer has place N in its required places list, then the N-th empty space must have a
     * correct answer and that answer must be from the group of this answer to be accepted.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    boolean compatibleWithOtherAnswers(@NonNull final List<EmptySpace> emptySpaces) {
        if(!hasRequirements()) return true; //automatically compatible
        boolean compatible = false;
        final List<Integer> reqPlaces = requiredPlaces.get();
        for(int i=0; i<emptySpaces.size(); i++) {
            if(!reqPlaces.contains(i)) continue; //there are no requirements for this place
            //there is a requirement for this place, check
            final EmptySpace emptySpace = emptySpaces.get(i);
            //find answer, check group.
            for(EmptySpaceAnswer answer: emptySpace.getAnswers()) {
                if(answer.toString().equals(emptySpace.getTextInEmptySpace())) { //this is the current answer
                    if(answer.group.isPresent() && answer.group.get().equals(group.get())) {
                        compatible = true; //this answer is compatible with current answer
                    } else {
                        return false; //not compatible
                    }
                }
                //If there is not EmptySpaceAnswer found in this loop that means that the
                //EmptySpace didn't have an acceptable answer at all. The condition remains false.
            }
        }
        return compatible;
    }
    @NonNull
    @Override
    public String toString() {
        return answer;
    }

    /**
     * Builder designed to be convenient when parsing answers.
     */
    public static class AnswerBuilder {

        private EmptySpaceAnswer instance;

        public AnswerBuilder() {
            instance = new EmptySpaceAnswer();
        }

        public void withAnswer(String answer) {
            instance.answer = answer;
        }

        /**
         * Adds some required places to the answer.
         * @param csvRequiredPlaces String of required places, as parsed from XML.
         */
        public void withRequiredPlaces(String csvRequiredPlaces) { //the parameter is what's parsed
            List<Integer> places = null;
            if(csvRequiredPlaces != null) { //make int list from strings separated by commas
                places = Arrays.stream(csvRequiredPlaces.split(","))
                        .map(Integer::parseInt).collect(Collectors.toList());
            }
            instance.requiredPlaces = places==null ? Optional.empty() : Optional.of(places);
        }

        /**
         * Adds a group for the answer.
         * @param group The group.
         */
        public void withGroup(String group) {
            instance.group = group==null ? Optional.empty() : Optional.of(group);
        }

        /**
         * Builds the answer objects with the given properties.
         * @return The answer object.
         */
        public EmptySpaceAnswer build() {
            if(instance.requiredPlaces.isPresent() && !instance.group.isPresent() ||
                    !instance.requiredPlaces.isPresent() && instance.group.isPresent()) {
                throw new RuntimeException("Invalid answer! Must give both group and required places, or none!");
            }
            return instance;
        }
    }
}
