package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.gaspar.learnjava.R;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that parses images from the raw folder.
 */
public abstract class RawParser {

    /**
     * Parses an image by name.
     *
     * @param imageName The name of the image in the raw folder.
     * @return The drawable of the image.
     */
    public static Drawable parseImage(String imageName, @NonNull final Context context) {
        Drawable imageDrawable = null;
        final Field[] fields = R.raw.class.getDeclaredFields();
        for (Field field : fields) {
            final int rawResourceID;
            try {
                rawResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(rawResourceID);
            if(resourceName.equals(imageName)) { //found the image
                InputStream is = context.getResources().openRawResource(rawResourceID);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageDrawable = new BitmapDrawable(context.getResources(), bitmap);
            }
        }
        if(imageDrawable == null) throw new RuntimeException("Image not found!");
        return imageDrawable;
    }

    private static final String CODE_IMAGE_NAME = "code_image";

    /**
     * Loads all code background images.
     * @return The code images as a list of {@link Drawable}.
     */
    public static List<Drawable> parseCodeImages(@NonNull final Context context) {
        final Field[] fields = R.raw.class.getDeclaredFields();
        List<Drawable> drawables = new ArrayList<>();
        for (Field field : fields) {
            final int rawResourceID;
            try {
                rawResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(rawResourceID);
            if(resourceName.startsWith(CODE_IMAGE_NAME)) { //found a code image id
                InputStream is = context.getResources().openRawResource(rawResourceID);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                drawables.add(new BitmapDrawable(context.getResources(), bitmap));
            }
        }
       return drawables;
    }
}
