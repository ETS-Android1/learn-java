package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gaspar.learnjava.R;

import java.io.InputStream;
import java.lang.reflect.Field;

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
    public static Drawable parseImage(String imageName, Context context) {
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

}
