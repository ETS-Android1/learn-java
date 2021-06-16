package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.curriculum.components.AdvancedComponent;
import com.gaspar.learnjava.curriculum.components.BoxedComponent;
import com.gaspar.learnjava.curriculum.components.CodeComponent;
import com.gaspar.learnjava.curriculum.components.Component;
import com.gaspar.learnjava.curriculum.Task;
import com.gaspar.learnjava.curriculum.components.ImageComponent;
import com.gaspar.learnjava.curriculum.components.TextComponent;
import com.gaspar.learnjava.curriculum.components.TitleComponent;
import com.gaspar.learnjava.utils.LocalizationUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
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
     * Parses a {@link Task} object from an XML file using the task Id.
     * @param taskId The ID of the task that will be parsed.
     * @param parseComponents If the components should be parsed as well.
     * @return The parsed {@link Task} object.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     * @throws RuntimeException When there is no task with the specified id.
     */
    public Task parseTask(int taskId, boolean parseComponents, @NonNull final Context context)
            throws XmlPullParserException, RuntimeException, IOException {
        Task parsedTask = null;
        final AssetManager manager = context.getAssets();
        String localizedAssets = LocalizationUtils.getLocalizedAssetPath();
        String taskFolder = localizedAssets + "/tasks";
        final String[] taskPaths = manager.list(taskFolder); //list task XML-s in the asset folder
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        if(taskPaths != null) {
            for(String relTaskPath: taskPaths) { //check task XMLs for the correct id
                String taskPath = taskFolder + "/" + relTaskPath;
                if(CourseParser.getInstance().matchingId(taskPath, taskId, manager, factory)) {
                    //found the task with the selected id
                    try(final InputStream is = manager.open(taskPath)) { //open task XML as input stream
                        final XmlPullParser parser = factory.newPullParser();
                        parser.setInput(is, "UTF-8");
                        parsedTask = parseTaskData(parser, parseComponents, context);
                    }
                    break;
                }
            }
        } else {
            throw new IOException("Failed to open exam assets!");
        }
        if(parsedTask == null) throw new RuntimeException();
        return parsedTask;
    }

    /**
     * Parses a {@link Task} object using an XML parser.
     * @param parser The XML parser, which points to the beginning of the task XML.
     * @param parseComponents If the components should be parsed or only basic information such as name or id.
     * @param context Context.
     * @return The parsed {@link Task} object.
     * @throws RuntimeException When there is no task with the specified id.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     */
    public Task parseTaskData(@NonNull final XmlPullParser parser, boolean parseComponents, @NonNull Context context)
        throws RuntimeException, XmlPullParserException, IOException {
        int taskId = CourseParser.NO_ID_FOUND;
        String taskName = null;
        List<Component> components = parseComponents ? new ArrayList<>() : null;
        List<Component> solutionComponents = null;
        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if(eventType == XmlPullParser.START_TAG) {
                if(tagName.equalsIgnoreCase(TagName.ID)) {
                    taskId = Integer.parseInt(parser.nextText());
                } else if(tagName.equalsIgnoreCase(TagName.NAME)) {
                    taskName = parser.nextText();
                    if(!parseComponents) {
                        break; //no need to go further if we don't need components
                    }
                } else if(tagName.equalsIgnoreCase(TagName.TEXT) && parseComponents) {
                    components.add(new TextComponent(parser.nextText(), false));
                } else if(tagName.equalsIgnoreCase(TagName.CODE) && parseComponents) {
                    components.add(new CodeComponent(parser.nextText()));
                } else if(tagName.equalsIgnoreCase(TagName.ADVANCED) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    components.add(new AdvancedComponent(parser.nextText(), title));
                } else if(tagName.equalsIgnoreCase(TagName.BOXED) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    components.add(new BoxedComponent(parser.nextText(), title));
                } else if(tagName.equalsIgnoreCase(TagName.LIST) && parseComponents) {
                    components.add(new TextComponent(parser.nextText(), true));
                } else if(tagName.equalsIgnoreCase(TagName.SOLUTION) && parseComponents) {
                    solutionComponents = parseSolution(parser, context); //will advance parser through solution tag
                } else if(tagName.equalsIgnoreCase(TagName.IMAGE) && parseComponents) {
                    String imageName = parser.getAttributeValue(null, TagName.NAME);
                    //need to parse the image drawable as well
                    Drawable imageDrawable = RawParser.parseImage(imageName, context);
                    components.add(new ImageComponent(imageName, imageDrawable));
                } else if(tagName.equalsIgnoreCase(TagName.TITLE) && parseComponents) {
                    String title = parser.getAttributeValue(null, TagName.TEXT);
                    components.add(new TitleComponent(title));
                }
            }
            eventType = parser.next();
        }
        if(taskId == CourseParser.NO_ID_FOUND || taskName == null) throw new RuntimeException();
        return new Task(taskId, taskName, components, solutionComponents);
    }

    /**
     * Parses the solution part of a task.
     * @param parser XML parsers which points to the start of the solution.
     * @param context Context.
     * @return A list of components that make up the solution.
     * @throws XmlPullParserException When the XML could not be parsed.
     * @throws IOException When the XML could not be parsed.
     */
    private List<Component> parseSolution(@NonNull final XmlPullParser parser, @NonNull Context context)
            throws XmlPullParserException, IOException {
        List<Component> solutionComponents = new ArrayList<>();
        int eventType = parser.getEventType();
        String tagName = parser.getName();
        while(eventType != XmlResourceParser.END_TAG || !tagName.equalsIgnoreCase(TagName.SOLUTION)) {
            if(eventType == XmlPullParser.START_TAG) {
                if(tagName.equalsIgnoreCase(TagName.TEXT)) {
                    solutionComponents.add(new TextComponent(parser.nextText(), false));
                } else if(tagName.equalsIgnoreCase(TagName.CODE)) {
                    solutionComponents.add(new CodeComponent(parser.nextText()));
                } else if(tagName.equalsIgnoreCase(TagName.ADVANCED)) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    solutionComponents.add(new AdvancedComponent(parser.nextText(), title));
                } else if(tagName.equalsIgnoreCase(TagName.BOXED)) {
                    String title = parser.getAttributeValue(null, TagName.TITLE);
                    solutionComponents.add(new BoxedComponent(parser.nextText(), title));
                } else if(tagName.equalsIgnoreCase(TagName.LIST)) {
                    solutionComponents.add(new TextComponent(parser.nextText(), true));
                } else if(tagName.equalsIgnoreCase(TagName.IMAGE)) {
                    String imageName = parser.getAttributeValue(null, TagName.NAME);
                    //need to parse the image drawable as well
                    Drawable imageDrawable = RawParser.parseImage(imageName, context);
                    solutionComponents.add(new ImageComponent(imageName, imageDrawable));
                } else if(tagName.equalsIgnoreCase(TagName.TITLE)) {
                    String title = parser.getAttributeValue(null, TagName.TEXT);
                    solutionComponents.add(new TitleComponent(title));
                }
            }
            eventType = parser.next(); //advance parser
            tagName = parser.getName();
        }
        return solutionComponents;
    }
}
