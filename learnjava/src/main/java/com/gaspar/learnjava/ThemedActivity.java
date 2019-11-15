package com.gaspar.learnjava;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * The base activity for this application. It supports swapping themes at runtime.
 */
public class ThemedActivity extends AppCompatActivity {

    /**
     * Stores the latest theme id this activity is aware of.
     */
    @StyleRes
    private int themeId;

    /**
     * Component of the drawer. Needed for toolbar recoloring.
     */
    protected ActionBarDrawerToggle toggle;

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
            recreate();
        }
        recolorToolbar();
    }

    /**
     * Updates the colors of the toolbar, if there is one. For some reason this must be done
     * programmatically.
     */
    private void recolorToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, ThemeUtils.getPrimaryColor()));
            toolbar.setTitleTextColor(ContextCompat.getColor(this, ThemeUtils.getTextColor()));
            ImageButton settingsIcon = toolbar.findViewById(R.id.settings_icon);
            if(settingsIcon != null) {
                settingsIcon.setImageTintList(ThemeUtils.getImageButtonTintList(this));
                settingsIcon.setBackgroundTintList(ThemeUtils.getImageButtonBackgroundTintList(this));
            }
            if(toggle != null) {
                toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, ThemeUtils.getTextColor()));
            } else { //no drawer, there is a back arrow
                Drawable arrow = toolbar.getNavigationIcon();
                if(arrow != null) arrow.setTint(ContextCompat.getColor(this, ThemeUtils.getTextColor()));
            }
        }
    }
}
