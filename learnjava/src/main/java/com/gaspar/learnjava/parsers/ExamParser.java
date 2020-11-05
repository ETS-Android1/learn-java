package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.XmlResourceParser;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.annotation.XmlRes;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.MultiChoiceQuestion;
import com.gaspar.learnjava.curriculum.Question;
import com.gaspar.learnjava.curriculum.SingleChoiceQuestion;
import com.gaspar.learnjava.curriculum.TextQuestion;
import com.gaspar.learnjava.curriculum.TrueOrFalseQuestion;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gaspar.learnjava.curriculum.Question.QuestionType.MULTI_CHOICE;
import static com.gaspar.learnjava.curriculum.Question.QuestionType.SINGLE_CHOICE;
import static com.gaspar.learnjava.curriculum.Question.QuestionType.TRUE_OR_FALSE;

/**
 * Parses {@link Exam} and {@link Question} objects from XML files.
 */
@WorkerThread
public class ExamParser {

    private static ExamParser instance;

    @CheckResult
    public static ExamParser getInstance() {
        if(instance == null) instance = new ExamParser();
        return instance;
    }

    private ExamParser() {}

    /**
     * Parses an exam object from XML.
     *
     * @param examId The id of the exam that will be parsed.
     * @param parseQuestions If the question objects should be parsed.
     * @return The parsed exam object.
     */
    public Exam parseExam(int examId, boolean parseQuestions, @NonNull Context context)
        throws IOException, XmlPullParserException, RuntimeException {
        Exam parsedExam = null;
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith(TagName.EXAM + "_") && resourceName.endsWith(String.valueOf(examId))) {
                parsedExam = parseExamData(xmlResourceID, parseQuestions, context);
            }
        }
        if(parsedExam == null) throw new RuntimeException();
        return parsedExam;
    }

    /**
     * Helper method.
     */
    private Exam parseExamData(@XmlRes int xmlResourceId, boolean parseQuestions, @NonNull Context context)
        throws XmlPullParserException, IOException, RuntimeException {
        int examId = CourseParser.NO_ID_FOUND;
        int questionAmount = CourseParser.NO_ID_FOUND;
        int timeLimit = CourseParser.NO_ID_FOUND;
        boolean finished = false;
        List<Question> questions = new ArrayList<>();
        XmlResourceParser parser = context.getResources().getXml(xmlResourceId);

        int eventType = parser.getEventType();
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) { //a tag is starting
                if(tagName.equalsIgnoreCase(TagName.ID)) { //id tag
                    examId = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.QUESTION_AMOUNT)) {
                    questionAmount = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.TIME_LIMIT)) {
                    timeLimit = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.QUESTION) && parseQuestions) {
                    questions.add(parseQuestion(parser));
                } else if(tagName.equalsIgnoreCase(TagName.FINISHED)) {
                    finished = Boolean.parseBoolean(parser.nextText());
                }
            }
            eventType = parser.next();
        }
        if(examId == CourseParser.NO_ID_FOUND || questionAmount == CourseParser.NO_ID_FOUND
                 || timeLimit == CourseParser.NO_ID_FOUND) {
            throw new RuntimeException("Exam attributes not found; id: " + examId + ", questionAmount: " +
                    questionAmount + ", timeLimit: " + timeLimit);
        }
        if(parseQuestions) {
            return new Exam(examId, questions, questionAmount, timeLimit, finished);
        } else {
            return new Exam(examId, null, questionAmount, timeLimit, finished);
        }
    }

    /**
     * Parses question objects using an XML parser set to an exam XML file.
     *
     * @param parser The xml parser.
     * @return The parsed question object.
     */
    private Question parseQuestion(XmlResourceParser parser) throws XmlPullParserException, IOException {
        Question question = null;
        @Question.QuestionType
        int type = Question.findTypeFromString(parser.getAttributeValue(null, TagName.TYPE));
        if(type == -1) throw new RuntimeException(); //question tag has no type attribute or is invalid
        switch (type) {
            case SINGLE_CHOICE:
                question = parseSingleChoiceQuestion(parser);
                break;
            case MULTI_CHOICE:
                question = parseMultiChoiceQuestion(parser);
                break;
            case TRUE_OR_FALSE:
                question = parseTrueOrFalseQuestion(parser);
                break;
            case Question.QuestionType.TEXT:
                question = parseTextQuestion(parser);
                break;
        }
        return question;
    }

    private SingleChoiceQuestion parseSingleChoiceQuestion(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        String questionText = null;
        List<String> answers = new ArrayList<>();
        int correctAnswerIndex = CourseParser.NO_ID_FOUND;
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.QUESTION)) {
            if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.TEXT)) {
                questionText = parser.nextText();
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.ANSWER)) {
                answers.add(parser.nextText());
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.CORRECT)) {
                correctAnswerIndex = Integer.parseInt(parser.nextText());
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        if(questionText==null || answers.isEmpty() || correctAnswerIndex == CourseParser.NO_ID_FOUND) {
            throw new RuntimeException("Invalid single choice question!");
        }
        return new SingleChoiceQuestion(questionText, answers, correctAnswerIndex);
    }

    private MultiChoiceQuestion parseMultiChoiceQuestion(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        String questionText = null;
        List<String> answers = new ArrayList<>();
        Set<Integer> correctIndexes = new HashSet<>();
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.QUESTION)) {
            if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.TEXT)) {
                questionText = parser.nextText();
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.ANSWER)) {
                answers.add(parser.nextText());
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.CORRECT)) {
                correctIndexes.add(Integer.parseInt(parser.nextText()));
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        if(questionText==null || answers.isEmpty() || correctIndexes.isEmpty() || correctIndexes.size()>answers.size()) {
            throw new RuntimeException("Invalid multi choice question!");
        }
        return new MultiChoiceQuestion(questionText, answers, correctIndexes);
    }

    private TrueOrFalseQuestion parseTrueOrFalseQuestion(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        String questionText = null;
        boolean isTrue = false;
        boolean correctInit = false;
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.QUESTION)) { //end of question tag
            if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.TEXT)) {
                questionText = parser.nextText();
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.CORRECT)) {
                isTrue = Boolean.valueOf(parser.nextText()); //will return false for anything not "true"
                correctInit = true;
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        if(questionText == null || !correctInit) throw new RuntimeException("Invalid true or false question");
        return new TrueOrFalseQuestion(questionText, isTrue);
    }

    private TextQuestion parseTextQuestion(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        String questionText = null;
        List<String> acceptedAnswers = new ArrayList<>();
        boolean ignoreSpace = false, ignoreCase = false;
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.QUESTION)) { //end of question tag
            if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.TEXT)) {
                questionText = parser.nextText();
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.CORRECT)) {
                acceptedAnswers.add(parser.nextText());
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.IGNORE_SPACE)) {
                ignoreSpace = true; //found ignore space tag
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.IGNORE_CASE)) {
                ignoreCase = true; //found ignore case tag
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        if(questionText == null || acceptedAnswers.isEmpty()) throw new RuntimeException("Invalid text question!");
        return new TextQuestion(questionText, acceptedAnswers, ignoreSpace, ignoreCase);
    }
}
