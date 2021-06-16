package com.gaspar.learnjava.curriculum.components;

import androidx.annotation.IntDef;

import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Task;

import java.io.Serializable;

/**
 * Represents a component of a {@link Chapter} or a {@link Task}.
 * <p>
 * The following components are supported:
 * <ul>
 *     <li>{@link TextComponent}: normal text.</li>
 *     <li>{@link CodeComponent}: code component in black background.</li>
 *     <li>{@link InteractiveComponent}: similar to code component, but can be edited to some degree.</li>
 *     <li>{@link AdvancedComponent}: red background, can have title.</li>
 *     <li>{@link BoxedComponent}: information with title.</li>
 *     <li>{@link ImageComponent}: displays a full width image in with a colored border.</li>
 *     <li>{@link TitleComponent}: displays a title text with some vertical margins.</li>
 * </ul>
 * This class needs to be subclassed for each type of component.
 */
public abstract class Component implements Serializable {

    /**
     * Type of the component. One of {@link ComponentType} constants.
     */
    @ComponentType
    protected final int type;

    /**
     * A string that contains information about the components. Some components display this ({@link TextComponent}, others
     * load some information with it ({@link ImageComponent}). Some components
     * may need more information, that needs to be added in their subclass.
     */
    protected final String data;

    /**
     * Constructor for the abstract component, used by subclasses.
     * @param type Type of the component, one of {@link ComponentType} constants.
     * @param data Data displayed by the component.
     */
    public Component(@ComponentType int type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Component type constants.
     */
    @IntDef({
            ComponentType.TEXT,
            ComponentType.CODE,
            ComponentType.ADVANCED,
            ComponentType.BOXED,
            ComponentType.IMAGE,
            ComponentType.TITLE,
            ComponentType.INTERACTIVE})
    public @interface ComponentType {
        int TEXT = 0;
        int CODE = 1;
        int ADVANCED = 2;
        int BOXED = 3;
        int IMAGE = 4;
        int TITLE = 5;
        int INTERACTIVE = 6;
    }

    /**
     * @return The type of the component. This is important for {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public int getType() {
        return type;
    }

    /**
     * @return The data of the component.
     */
    public String getData() {
        return data;
    }
}
