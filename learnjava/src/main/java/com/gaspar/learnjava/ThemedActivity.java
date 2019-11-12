package com.gaspar.learnjava;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The base activity for this application. It supports swapping themes at runtime.
 */
public class ThemedActivity extends AppCompatActivity {

    /**
     * Stores the latest theme id this activity is aware of.
     */
    @StyleRes
    private int themeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeId = ThemeUtils.getTheme(); //save current theme
        setTheme(themeId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int newTheme = ThemeUtils.getTheme();
        if(themeId != newTheme) { //theme was updated while activity was hidden
            themeId = newTheme;
            setTheme(newTheme);
            recreate();
        }
    }

}
