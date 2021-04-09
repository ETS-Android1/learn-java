package com.gaspar.learnjava.parsers;

import androidx.annotation.StringDef;

/**
 * Tag name constants.
 */
@StringDef
@interface TagName {
    String CHAPTER = "chapter";
    String TASK = "task";
    String EXAM = "exam";

    String ID = "id";
    String NAME = "name";
    String FINISHED = "finished";
    String TEXT = "text";
    String CODE  = "code";
    String TITLE = "title";
    String ADVANCED = "advanced";
    String BOXED = "boxed";
    String IMAGE = "image";
    String LIST = "list";
    String SOLUTION = "solution";
    String INTERACTIVE = "interactive";
    String PLACE = "place";
    String INSTRUCTION = "instruction";
    String DATA = "data";
    String REQUIRED_PLACES = "required_places";
    String GROUP = "group";
    String DEFAULT = "default";

    String QUESTION = "question";
    String QUESTION_AMOUNT = "questionAmount";
    String TIME_LIMIT = "timeLimit";
    String TYPE = "type";
    String ANSWER = "answer";
    String CORRECT = "correct";
    String IGNORE_SPACE = "ignoreSpace";
    String IGNORE_CASE = "ignoreCase";
}
