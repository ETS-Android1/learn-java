package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.XmlResourceParser;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.annotation.XmlRes;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Task;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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
     * Parses all course XML files.
     *
     * @return A list of {@link Course} objects.
     */
    public List<Course> parseCourses(@NonNull Context context) throws XmlPullParserException, IOException {
        List<Course> parsedCourses = new ArrayList<>();
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith(TagName.COURSE + "_")) { //found a course
                parsedCourses.add(parseCourse(xmlResourceID, context));
            }
            Collections.sort(parsedCourses, courseComparator); //sort by id
        }
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
     * @param xmlResId The resource ID of the course XML file.
     * @param context App context.
     * @return The parsed course object.
     */
    private Course parseCourse(@XmlRes int xmlResId, @NonNull Context context)
            throws XmlPullParserException, IOException, NumberFormatException {
        XmlResourceParser parser = context.getResources().getXml(xmlResId);

        List<Chapter> chapters = new ArrayList<>(); //these will be filled during XML parsing
        List<Task> tasks = new ArrayList<>();
        Exam exam = null;
        int courseID = NO_ID_FOUND;
        String courseName = null;

        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) { //a tag is starting
                if(tagName.equalsIgnoreCase(TagName.ID)) { //id tag
                    courseID = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.NAME)) { //course name tag
                    courseName = parser.nextText();
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
        return new Course(courseID, courseName, chapters, tasks, exam, context);
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
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith(TagName.CHAPTER + "_") && resourceName.endsWith("_" + chapterID)) {
                parsedChapter = parseChapterData(xmlResourceID, parseComponents, context);
                break;
            }
        }
        if(parsedChapter == null) throw new RuntimeException();
        return parsedChapter;
    }

    /**
     * Parses the first chapter in the curriculum (without components).
     */
    public Chapter parseFirstChapter(@NonNull Context context) throws XmlPullParserException, IOException {
        Chapter firstChapter = null;
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith(TagName.COURSE + "_")) { //select the first course
                Course firstCourse = parseCourse(xmlResourceID, context);
                firstChapter = firstCourse.getChapters().get(0);
                break;
            }
        }
        if(firstChapter == null) throw new RuntimeException();
        return firstChapter;
    }

    /**
     * Helper method for chapter parsing.
     */
    private Chapter parseChapterData(@XmlRes int xmlResourceId, boolean parseComponents, @NonNull Context context)
        throws XmlPullParserException, IOException, RuntimeException {
        int chapterId = NO_ID_FOUND;
        String chapterName = null;
        List<Component> components = new ArrayList<>();

        XmlResourceParser parser = context.getResources().getXml(xmlResourceId);
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
     * Parses the components of the application guide from XML.
     */
    public List<Component> parseGuide(@NonNull Context context) throws XmlPullParserException, IOException {
        List<Component> components = null;
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.equalsIgnoreCase(TagName.GUIDE)) {
                //use chapter parses method
                components = parseChapterData(xmlResourceID, true, context).getComponents();
                break;
            }
        }
        if(components == null) throw new RuntimeException();
        return components;
    }
}
