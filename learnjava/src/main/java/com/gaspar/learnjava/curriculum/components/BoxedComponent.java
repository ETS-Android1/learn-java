package com.gaspar.learnjava.curriculum.components;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;

/**
 * A {@link Component} that displays a a title and text in an elevated, highlighted background.
 * Useful for separating text ({@link TextComponent}) to make the chapter or task look not like a wall of text.
 * Similar to {@link AdvancedComponent}, but without the red background.
 */
public class BoxedComponent extends Component {

    /**
     * Title of the boxed component.
     */
    @NonNull
    private final String title;

    /**
     * Constructor for boxed component.
     * @param data Data displayed by the component.
     * @param title Title of the component.
     */
    public BoxedComponent(@NonNull String data, @NonNull String title) {
        super(ComponentType.BOXED, data);
        this.title = title;
    }

    /**
     * @return The title.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * Functionally equivalent to {@link #getData()}
     * @return The content this component displays.
     */
    public String getContent() {
        return data;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class BoxedComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Text view which displays the title.
         */
        public TextView titleTextView;

        /**
         * The text view which displays the content.
         */
        public TextView contentTextView;

        /**
         * Creates a view holder for a {@link BoxedComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_boxed.
         */
        public BoxedComponentHolder(View componentView) {
            super(componentView);
            titleTextView = componentView.findViewById(R.id.title);
            contentTextView = componentView.findViewById(R.id.boxedArea);
            contentTextView.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
        }

    }
}
