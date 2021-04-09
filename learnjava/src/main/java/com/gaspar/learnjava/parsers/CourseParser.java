package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.curriculum.interactive.EmptySpace;
import com.gaspar.learnjava.curriculum.interactive.EmptySpaceAnswer;
import com.gaspar.learnjava.curriculum.interactive.InteractiveComponent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parses {@link Course} and {@link Chapter} objects from XML files.
 */
@WorkerThread
public class CourseParser {

    private static CourseParser instance;

    static final int NO_ID_FOUND = Integer.MIN_VALUE;

    @CheckResult
    public static CourseParser getInstance() {
        if(instance == null) {
            instance = new CourseParser();
        }
        return instance;
    }

    private CourseParser() {}

    /**
     * Parses all course XML files. The returned course list is sorted by course id-s, so when
     * it is displayed they will be in order.
     *
     * @return A list of {@link Course} objects.
     */
    public List<Course> parseCourses(@NonNull Context context) throws XmlPullParserException, IOException {
        List<Course> parsedCourses = new ArrayList<>();
        final AssetManager manager = context.getAssets();
        String courseFolder = "courses";
        final String[] coursePaths = manager.list(courseFolder); //list course XML-s in the courses asset folder
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        if(coursePaths != null) {
            for(String relCoursePath: coursePaths) { //check every course XML for the correct id
                String coursePath = courseFolder + "/" + relCoursePath;
                try(final InputStream is = manager.open(coursePath)) { //open course XML as input stream
                    final XmlPullParser parser = factory.newPullParser();
                    parser.setInput(is, "UTF-8");
                    final Course course = parseCourse(parser, context); //parse this course
                    parsedCourses.add(course); //add to list
                }
            }
        } else {
            throw new IOException("Failed to open course assets!");
        }
        parsedCourses.sort(courseComparator); //sort by id
        if(parsedCourses.size() == 0) throw new RuntimeException("No courses found!");
        return parsedCourses;
    }

    /**
     * A comparator that orders courses using their id-s.
     */
    private static final Comparator<Course> courseComparator = (course1, course2) -> Integer.compare(course1.getId(), course2.getId());

    /**
     * Parses a {@link Course} object from an xml file. The chapters and tasks of this object will
     * not have components, only names and ID's.
     *
     * @param parser An XML pull parser that points to the course file.
     * @param context App context.
     * @return The parsed course object.
     */
    public Course parseCourse(@NonNull final XmlPullParser parser, @NonNull Context context)
            throws XmlPullParserException, IOException, NumberFormatException {
        List<Chapter> chapters = new ArrayList<>(); //these will be filled during XML parsing
        List<Task> tasks = new ArrayList<>();
        Exam exam = null;
        int courseID = NO_ID_FOUND;
        String courseName = null;
        boolean finished = false;

        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) { //a tag is starting
                if(tagName.equalsIgnoreCase(TagName.ID)) { //id tag
                    courseID = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.NAME)) { //course name tag
                    courseName = parser.nextText();
                } else if(tagName.equalsIgnoreCase(TagName.FINISHED)) {
                    finished = Boolean.parseBoolean(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.CHAPTER)) { //chapter tag
                    int chapterId = Integer.parseInt(parser.nextText());
                    chapters.add(parseChapter(chapterId,false, context));
                } else if(tagName.equalsIgnoreCase(TagName.TASK)) { //task tag
                    int taskId = Integer.parseInt(parser.nextText());
                    tasks.add(TaskParser.getInstance().parseTask(taskId, false, context));
                } else if(tagName.equalsIgnoreCase(TagName.EXAM)) { //the exam tag.
                    int examId = Integer.parseInt(parser.nextText());
                    exam = ExamParser.getInstance().parseExam(examId, false, context);
                }
            }
            eventType = parser.next();
        }
        if(courseID == NO_ID_FOUND || courseName == null || exam == null ||
            chapters.size() == 0 || tasks.size() == 0) throw new RuntimeException();
        return new Course(courseID, courseName, chapters, tasks, exam, finished);
    }

    /**
     * Parses a chapter from xml, using the chapter ID.
     *
     * @param chapterID The Id of the chapter (this is not the resource id!)
     * @param parseComponents Indicates if the components need to be parsed or not.
     * @return The parsed chapter object.
     */
    public Chapter parseChapter(int chapterID, boolean parseComponents, @NonNull Context context)
        throws XmlPullParserException, IOException {
        Chapter parsedChapter = null;
        //open chapter asset folder
        final AssetManager manager = context.getAssets();
        String chapterFolder = "chapters";
        final String[] chapterPaths = manager.list(chapterFolder); //list chapter XML-s in the chapter asset folder
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        if(chapterPaths != null) {
            for(String relChapterPath: chapterPaths) { //loop chapters and find one with matching id
                String chapterPath = chapterFolder + "/" + relChapterPath;
                if(matchingId(chapterPath, chapterID, manager, factory)) {
                    //found the chapter with this id
                    XmlPullParser parser = factory.newPullParser();
                    try(final InputStream is = manager.open(chapterPath)) { //open file
                        parser.setInput(is, "UTF-8"); //set input stream to XML parser
                        parsedChapter = parseChapterData(parser, parseComponents);
                    }
                    break; //no need to continue searching chapters
                }
            }
        } else {
            throw new IOException("Failed to open chapter assets!");
        }
        if(parsedChapter == null) throw new RuntimeException("Internal error: chapter not found!");
        return parsedChapter;
    }

    /**
     * Peeks an XML file and checks if the content of the id tag inside matches the given id.
     *
     * @param path Path to the file.
     * @param manager Asset manager.
     * @return True only if the id inside matches.
     * @throws IOException If the file could not be opened.
     */
    public boolean matchingId(@NonNull String path, int id, final AssetManager manager, final XmlPullParserFactory factory)
            throws IOException, XmlPullParserException {
        XmlPullParser parser = factory.newPullParser();
        try(final InputStream is = manager.open(path)) { //open file
            parser.setInput(is, "UTF-8"); //set input stream to XML parser
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) { //loop XML tags
                String tagName = parser.getName();
                if(tagName != null && tagName.equalsIgnoreCase(TagName.ID)) { //id tag was found
                    int idFound = Integer.parseInt(parser.nextText());
                    return id == idFound; //check equality
                }
                eventType = parser.next();
            }
        }
        return false;
    }

    /**
     * Parses the components from a chapter XML file.
     *
     * @param parser XML parses pointed to the chapter file.
     * @param parseComponents Determines or the components will be parsed, or only general information,
     *                        such as id and chapter name.
     * @return The parsed {@link Chapter} object.
     * @throws XmlPullParserException If problem arises during XML parsing.
     * @throws IOException If problem arises during XML parsing.
     * @throws RuntimeException If the chapter file is incorrect, such as no id or name.
     */
    public Chapter parseChapterData(@NonNull XmlPullParser parser, boolean parseComponents)
        throws RuntimeException, XmlPullParserException, IOException {
        int chapterId = NO_ID_FOUND;
        String chapterName = null;
        List<Component> components = new ArrayList<>();
        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if(tagName.equalsIgnoreCase(TagName.ID)) {
                    chapterId = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.NAME)) {
                    chapterName = parser.nextText();
                } else if(tagName.equalsIgnoreCase(TagName.TEXT) && parseComponents) {
                    components.add(new Component(Component.ComponentType.TEXT, parser.nextText()));
                } else if(tagName.equalsIgnoreCase(TagName.CODE) && parseComponents) {
                    components.add(new Component(Component.ComponentType.CODE, parser.nextText()));
                } else if(tagName.equalsIgnoreCase(TagName.ADVANCED) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    Component c = new Component(Component.ComponentType.ADVANCED, parser.nextText());
                    c.setTitle(title);
                    components.add(c);
                } else if(tagName.equalsIgnoreCase(TagName.BOXED) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    Component c = new Component(Component.ComponentType.BOXED, parser.nextText());
                    c.setTitle(title);
                    components.add(c);
                } else if(tagName.equalsIgnoreCase(TagName.LIST) && parseComponents) {
                    components.add(new Component(Component.ComponentType.LIST, parser.nextText()));
                } else if(tagName.equalsIgnoreCase(TagName.IMAGE) && parseComponents) {
                    String imageName = parser.getAttributeValue(null, TagName.NAME);
                    Component c = new Component(Component.ComponentType.IMAGE, imageName);
                    components.add(c);
                } else if(tagName.equalsIgnoreCase(TagName.TITLE) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TEXT);
                    Component component = new Component(Component.ComponentType.TITLE, "");  //data not important
                    component.setTitle(title);
                    components.add(component);
                } else if(tagName.equalsIgnoreCase(TagName.INTERACTIVE) && parseComponents) {
                    components.add(parseInteractiveComponent(parser));
                }
            }
            eventType = parser.next();
        }
        if(chapterId == NO_ID_FOUND || chapterName == null ||
                (parseComponents && components.size() == 0)) throw new RuntimeException();
        if(parseComponents) {
            return new Chapter(chapterId, chapterName, components);
        } else {
            return new Chapter(chapterId, chapterName);
        }
    }

    /**
     * Parses an interactive_component code component. This means the parsing of the "data" (code +
     * empty spaces), and the possible answers for each empty space. These are within ANSWER tags.
     *
     * @param parser XML parses pointed to the beginning of the interactive components.
     * @return The parsed component.
     */
    private InteractiveComponent parseInteractiveComponent(@NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        String instruction = parser.getAttributeValue(null, TagName.INSTRUCTION);
        String data = null;
        EmptySpace.EmptySpaceListBuilder builder = new EmptySpace.EmptySpaceListBuilder();
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.INTERACTIVE)) {
            //found an answer
            if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.ANSWER)) {
                //read place and answer text
                int place = Integer.parseInt(parser.getAttributeValue(null, TagName.PLACE));
                EmptySpaceAnswer.AnswerBuilder answerBuilder = new EmptySpaceAnswer.AnswerBuilder();
                answerBuilder.withGroup(parser.getAttributeValue(null, TagName.GROUP)); //may be null!
                answerBuilder.withRequiredPlaces(parser.getAttributeValue(null, TagName.REQUIRED_PLACES));
                answerBuilder.withAnswer(parser.nextText());
                builder.addEmptySpaceAnswer(place, answerBuilder.build());
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.DATA)) {
                data = parser.nextText();
            } else if(eventType == XmlResourceParser.START_TAG && tagName.equalsIgnoreCase(TagName.DEFAULT)) {
                int place = Integer.parseInt(parser.getAttributeValue(null, TagName.PLACE));
                builder.addDefaultText(place, parser.nextText());
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        //validate
        if(data == null || instruction == null) throw new RuntimeException("Invalid interactive_component component!");
        return new InteractiveComponent(instruction, data, builder.finishBuilding());
    }

    /**
     * Parses the components of the application guide from XML.
     */
    public List<Component> parseGuide(@NonNull Context context) throws XmlPullParserException, IOException {
        List<Component> components;
        String guidePath = "guide.xml";
        //open chapter asset folder
        final AssetManager manager = context.getAssets();
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        try(InputStream is = manager.open(guidePath)) { //open guide file
            final XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");
            //using chapter parser method for this
            components = parseChapterData(parser, true).getComponents();
        }
        if(components == null) throw new RuntimeException("Failed to load guide!");
        return components;
    }
}