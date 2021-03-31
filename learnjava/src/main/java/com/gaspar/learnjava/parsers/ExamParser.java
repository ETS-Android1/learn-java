package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.MultiChoiceQuestion;
import com.gaspar.learnjava.curriculum.Question;
import com.gaspar.learnjava.curriculum.SingleChoiceQuestion;
import com.gaspar.learnjava.curriculum.TextQuestion;
import com.gaspar.learnjava.curriculum.TrueOrFalseQuestion;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
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
     * Parses an exam object from XML. The exam is identified by the exam id.
     *
     * @param examId The id of the exam that will be parsed.
     * @param parseQuestions If the question objects should be parsed, or only basic information.
     * @param context Context.
     * @return The parsed exam object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When there is no exam with the specified id.
     */
    public Exam parseExam(int examId, boolean parseQuestions, @NonNull Context context)
        throws IOException, XmlPullParserException, RuntimeException {
        Exam parsedExam = null;
        final AssetManager manager = context.getAssets();
        String examFolder = "exams";
        final String[] examPaths = manager.list(examFolder); //list exam XML-s in the asset folder
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        if(examPaths != null) {
            for(String relExamPath: examPaths) { //check exam XMLs for the correct id
                String examPath = examFolder + "/" + relExamPath;
                if(CourseParser.getInstance().matchingId(examPath, examId, manager, factory)) {
                    //found the exam with the selected id
                    try(final InputStream is = manager.open(examPath)) { //open exam XML as input stream
                        final XmlPullParser parser = factory.newPullParser();
                        parser.setInput(is, "UTF-8");
                        parsedExam = parseExamData(parser, parseQuestions);
                    }
                    break;
                }
            }
        } else {
            throw new IOException("Failed to open exam assets!");
        }
        if(parsedExam == null) throw new RuntimeException("No exam with id " + examId);
        return parsedExam;
    }

    /**
     * Parses exam data into an {@link Exam} object, optionally with questions.
     *
     * @param parser An XML parser that points to the beginning of the exam XML.
     * @param parseQuestions If questions should be parsed, or only basic information.
     * @return The parsed {@link Exam} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException If the exam object is incomplete, for example no name or id.
     */
    public Exam parseExamData(@NonNull XmlPullParser parser, boolean parseQuestions)
        throws XmlPullParserException, IOException, RuntimeException {
        int examId = CourseParser.NO_ID_FOUND;
        int questionAmount = CourseParser.NO_ID_FOUND;
        int timeLimit = CourseParser.NO_ID_FOUND;
        boolean finished = false;
        List<Question> questions = new ArrayList<>();

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
     * Parses {@link Question} objects using an XML parser.
     * @param parser An XML parser pointing to the beginning of the question in the XML file.
     * @return The parsed {@link Question} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When the question is incomplete.
     */
    private Question parseQuestion(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
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

    /**
     * Parses {@link SingleChoiceQuestion} objects using an XML parser.
     * @param parser An XML parser pointing to the beginning of the question in the XML file.
     * @return The parsed {@link SingleChoiceQuestion} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When the question is incomplete.
     */
    private SingleChoiceQuestion parseSingleChoiceQuestion(@NonNull final XmlPullParser parser)
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

    /**
     * Parses {@link MultiChoiceQuestion} objects using an XML parser.
     * @param parser An XML parser pointing to the beginning of the question in the XML file.
     * @return The parsed {@link MultiChoiceQuestion} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When the question is incomplete.
     */
    private MultiChoiceQuestion parseMultiChoiceQuestion(@NonNull final XmlPullParser parser)
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

    /**
     * Parses {@link TrueOrFalseQuestion} objects using an XML parser.
     * @param parser An XML parser pointing to the beginning of the question in the XML file.
     * @return The parsed {@link TrueOrFalseQuestion} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When the question is incomplete.
     */
    private TrueOrFalseQuestion parseTrueOrFalseQuestion(@NonNull final XmlPullParser parser)
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

    /**
     * Parses {@link TextQuestion} objects using an XML parser.
     * @param parser An XML parser pointing to the beginning of the question in the XML file.
     * @return The parsed {@link TextQuestion} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When the question is incomplete.
     */
    private TextQuestion parseTextQuestion(@NonNull final XmlPullParser parser)
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
