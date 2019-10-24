package com.gaspar.learnjava.parsers;

import androidx.annotation.StringDef;

/**
 * Tag name constants.
 */
@StringDef
@interface TagName {
    String COURSE = "course";
    String CHAPTER = "chapter";
    String TASK = "task";
    String EXAM = "exam";
    String GUIDE = "guide";

    String ID = "id";
    String NAME = "name";
    String TEXT = "text";
    String CODE  = "code";
    String TITLE = "title";
    String ADVANCED = "advanced";
    String BOXED = "boxed";
    String IMAGE = "image";
    String LIST = "list";
    String SOLUTION = "solution";

    String QUESTION = "question";
    String QUESTION_AMOUNT = "questionAmount";
    String TIME_LIMIT = "timeLimit";
    String TYPE = "type";
    String ANSWER = "answer";
    String CORRECT = "correct";
}
