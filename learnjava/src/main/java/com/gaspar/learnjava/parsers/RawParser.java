package com.gaspar.learnjava.parsers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.gaspar.learnjava.utils.LocalizationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that parses images from the raw folder.
 */
public abstract class RawParser {

    /**
     * Parses an image by name.
     * @param imageName The name of the image in the raw folder.
     * @return The drawable of the image.
     */
    public static Drawable parseImage(String imageName, @NonNull final Context context) {
        Drawable imageDrawable = null;
        final AssetManager manager = context.getAssets();
        String localizedAssets = LocalizationUtils.getLocalizedAssetPath();
        String imagesFolder = localizedAssets + "/images";
        try {
            final String[] imagePaths = manager.list(imagesFolder); //list images in the asset folder
            if(imagePaths == null) throw new IOException();
            for(String imagePath: imagePaths) { //find image with name
                String foundImageName = imagePath.substring(0, imagePath.lastIndexOf('.'));
                if(foundImageName.equals(imageName)) {
                    //image found
                    try(InputStream is = manager.open(imagesFolder + "/" + imagePath)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageDrawable = new BitmapDrawable(context.getResources(), bitmap);
                    }
                    break;
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to load image!");
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
        List<Drawable> drawables = new ArrayList<>();
        final AssetManager manager = context.getAssets();
        String localizedAssets = LocalizationUtils.getLocalizedAssetPath();
        String imagesFolder = localizedAssets + "/images";
        try {
            final String[] imagePaths = manager.list(imagesFolder); //list images in the asset folder
            if(imagePaths == null) throw new IOException();
            for(String imagePath: imagePaths) { //find code background images
                if(imagePath.startsWith(CODE_IMAGE_NAME)) {
                    //found a background image
                    try(InputStream is = manager.open(imagesFolder + "/" + imagePath)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        Drawable imageDrawable = new BitmapDrawable(context.getResources(), bitmap);
                        drawables.add(imageDrawable);
                    }
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("Failed to load background image!");
        }
        return drawables;
    }
}
