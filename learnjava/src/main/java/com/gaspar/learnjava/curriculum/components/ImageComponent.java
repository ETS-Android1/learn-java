package com.gaspar.learnjava.curriculum.components;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link Component} that displays an image. The {@link #data} stores the name of the images
 * which can be passed to the {@link com.gaspar.learnjava.parsers.RawParser} to parse the image from
 * the assets.
 */
public class ImageComponent extends Component {

    /**
     * The drawable that this component displays.
     */
    @NonNull
    private final Drawable imageDrawable;

    /**
     * Constructor for an image component.
     * @param data Data of the component, which is an image name.
     */
    public ImageComponent(@NonNull String data, @NonNull Drawable imageDrawable) {
        super(ComponentType.IMAGE, data);
        this.imageDrawable = imageDrawable;
    }

    /**
     * Functionally equivalent to {@link #getData()}.
     * @return The name of the image this component displays.
     */
    public String getImageName() {
        return data;
    }

    /**
     * Gets the {@link Drawable} that this component displays.
     * @return The drawable.
     */
    @NonNull
    public Drawable getImageDrawable() {
        return imageDrawable;
    }

    /**
     * A {@link RecyclerView.ViewHolder} implementation for this component, that is used by
     * {@link com.gaspar.learnjava.adapters.ComponentAdapter}.
     */
    public static class ImageComponentHolder extends RecyclerView.ViewHolder {

        /**
         * Image view which displays the image of the component.
         */
        public ImageView imageView;

        /**
         * Creates a view holder for a {@link ImageComponent}.
         * @param componentView This is expected to be inflated from R.layout.component_image,
         *                      which is just an {@link ImageView}.
         */
        public ImageComponentHolder(View componentView) {
            super(componentView);
            imageView = (ImageView)componentView;
        }

    }
}
