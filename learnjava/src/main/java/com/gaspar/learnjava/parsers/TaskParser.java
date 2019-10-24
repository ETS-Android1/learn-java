package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.XmlResourceParser;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.annotation.XmlRes;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Component;
import com.gaspar.learnjava.curriculum.Task;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses {@link Task} objects from XML.
 */
@WorkerThread
public class TaskParser {

    private static TaskParser instance;

    @CheckResult
    public static TaskParser getInstance() {
        if(instance == null) instance = new TaskParser();
        return instance;
    }

    private TaskParser() {}

    /**
     * Parses a task object from an XML file using the task Id.
     *
     * @param taskId The ID of the task that will be parsed.
     * @param parseComponents If the components should be parsed as well.
     * @return The parsed task object.
     */
    public Task parseTask(int taskId, boolean parseComponents, @NonNull final Context context)
         throws XmlPullParserException, RuntimeException, IOException {
            Task parsedTask = null;
            final Field[] fields = R.xml.class.getDeclaredFields();
            for (Field field : fields) {
                final int xmlResourceID;
                try {
                    xmlResourceID = field.getInt(R.xml.class);
                } catch (Exception e) {
                    throw new RuntimeException();
                }
                String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
                if(resourceName.startsWith(TagName.TASK + "_") && resourceName.endsWith(String.valueOf(taskId))) {
                    parsedTask = parseTaskData(xmlResourceID, parseComponents, context);
                }
            }
            if(parsedTask == null) throw new RuntimeException();
            return parsedTask;
    }

    /**
     * Helper method.
     */
    private Task parseTaskData(@XmlRes int xmlResourceId, boolean parseComponents, @NonNull Context context)
        throws RuntimeException, XmlPullParserException, IOException {
        int taskId = CourseParser.NO_ID_FOUND;
        String taskName = null;
        List<Component> components = parseComponents ? new ArrayList<>() : null;
        List<Component> solutionComponents = null;

        XmlResourceParser parser = context.getResources().getXml(xmlResourceId);
        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if(tagName.equalsIgnoreCase(TagName.ID)) {
                    taskId = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.NAME)) {
                    taskName = parser.nextText();
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
                } else if(tagName.equalsIgnoreCase(TagName.SOLUTION) && parseComponents) {
                    solutionComponents = parseSolution(parser); //will advance parser through solution tag
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
        if(taskId == CourseParser.NO_ID_FOUND || taskName == null) throw new RuntimeException();
        return new Task(taskId, taskName, components, solutionComponents);
    }

    /**
     * Parses the task solution.
     */
    private List<Component> parseSolution(XmlResourceParser parser) throws XmlPullParserException, IOException {
        List<Component> solutionComponents = new ArrayList<>();
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.SOLUTION)) {
            if(tagName.equalsIgnoreCase(TagName.TEXT)) {
                solutionComponents.add(new Component(Component.ComponentType.TEXT, parser.nextText()));
            } else if(tagName.equalsIgnoreCase(TagName.CODE)) {
                solutionComponents.add(new Component(Component.ComponentType.CODE, parser.nextText()));
            } else if(tagName.equalsIgnoreCase(TagName.ADVANCED)) {
                String title = parser.getAttributeValue(null, TagName.TITLE);
                Component c = new Component(Component.ComponentType.ADVANCED, parser.nextText());
                c.setTitle(title);
                solutionComponents.add(c);
            } else if(tagName.equalsIgnoreCase(TagName.BOXED)) {
                String title = parser.getAttributeValue(null, TagName.TITLE);
                Component c = new Component(Component.ComponentType.BOXED, parser.nextText());
                c.setTitle(title);
                solutionComponents.add(c);
            } else if(tagName.equalsIgnoreCase(TagName.LIST)) {
                solutionComponents.add(new Component(Component.ComponentType.LIST, parser.nextText()));
            } else if(tagName.equalsIgnoreCase(TagName.IMAGE)) {
                String imageName = parser.getAttributeValue(null, TagName.NAME);
                Component c = new Component(Component.ComponentType.IMAGE, imageName);
                solutionComponents.add(c);
            } else if(tagName.equalsIgnoreCase(TagName.TITLE)) {
                String title = parser.getAttributeValue(null, TagName.TEXT);
                Component component = new Component(Component.ComponentType.TITLE, "");  //data not important
                component.setTitle(title);
                solutionComponents.add(component);
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        return solutionComponents;
    }
}
