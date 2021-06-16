package com.gaspar.learnjava.curriculum.components;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gaspar.learnjava.R;

/**
 * A {@link Component} that displays a title. Useful for marking the start of a new topic inside
 * a task or a chapter. The contents of {@link #data} will be displayed in the title.
 */
public class TitleComponent extends Component {

    /**
     * Constructor for title component.
     * @param data Data displayed by the component, which is the title text.
     */
    public TitleComponent(String data) {
        super(ComponentType.TITLE, data);
    }

    /**
     * Functionally equivalent to {@link #getData()}
     * @return The title that this component displays.
     */
    public String getTitle() {
        return data;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class TitleComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Text view which displays the title.
         */
        public TextView titleTextView;

        /**
         * Creates a view holder for a {@link TitleComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_title.
         */
        public TitleComponentHolder(View componentView) {
            super(componentView);
            titleTextView = componentView.findViewById(R.id.title);
        }

    }
}
