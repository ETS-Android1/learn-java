package com.gaspar.learnjava.curriculum.components;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;

/**
 * A {@link Component} that displays advanced information to the user on an elevated background.
 * Similar to {@link BoxedComponent}, but with a red background to clarify that the information is more difficult ot understand.
 */
public class AdvancedComponent extends Component {

    /**
     * Title of the advanced component.
     */
    @NonNull
    private final String title;

    /**
     * Constructor for advanced component.
     * @param data Data displayed by the component.
     * @param title Title of the component.
     */
    public AdvancedComponent(@NonNull String data, @NonNull String title) {
        super(ComponentType.ADVANCED, data);
        this.title = title;
    }

    /**
     * @return The title of the component.
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
    public static class AdvancedComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Just shows "advanced".
         */
        public TextView advancedTextView;

        /**
         * Text view which displays the title.
         */
        public TextView titleTextView;

        /**
         * The text view which displays the content.
         */
        public TextView contentTextView;

        /**
         * Creates a view holder for a {@link AdvancedComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_advanced.
         */
        public AdvancedComponentHolder(View componentView) {
            super(componentView);
            titleTextView = componentView.findViewById(R.id.title);
            contentTextView = componentView.findViewById(R.id.advancedArea);
            advancedTextView = componentView.findViewById(R.id.advancedDisplayer);
            contentTextView.setMovementMethod(LinkMovementMethod.getInstance()); //allow links
        }

    }
}
