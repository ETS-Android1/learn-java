package com.gaspar.learnjava.curriculum.components;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link Component} that displays formatted text. The formatting can contain lists as well, but
 * this needs to be marked ({@link #displaysList}), because in these cases a special {@link android.text.Html}
 * formatted must be used, {@link com.gaspar.learnjava.utils.ListTagHandler}.
 */
public class TextComponent extends Component {

    /**
     * Stores if this text components displays lists, or not.
     */
    private final boolean displaysList;

    /**
     * Constructor for text component.
     * @param data Data displayed by the component.
     */
    public TextComponent(String data, boolean displaysList) {
        super(ComponentType.TEXT, data);
        this.displaysList = displaysList;
    }

    /**
     * @return If this component displays lists.
     */
    public boolean isDisplaysList() {
        return displaysList;
    }

    /**
     * Functionally equivalent to {@link #getData()}.
     * @return The text that this component displays.
     */
    public String getText() {
        return data;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class TextComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Text view which displays the content of the component.
         */
        public TextView textView;

        /**
         * Creates a view holder for a {@link TextComponent}.
         * @param textComponentView This is expected to be inflated from R.layout.component_text,
         *                          which is just a text view.
         */
        public TextComponentHolder(View textComponentView) {
            super(textComponentView);
            textView = (TextView)textComponentView;
            textView.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
        }

    }
}
