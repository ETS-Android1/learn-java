package com.gaspar.learnjava.playground;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.SettingsActivity;
import com.gaspar.learnjava.ThemedActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity of the playground, where the user can edit and run code samples. This is a tabbed activity, where
 * the 3 main parts are the input (left), code (center) and output (right). This is managed by a
 * {@link ViewPager2} and a {@link TabLayout}.
 */
public class PlaygroundActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playground);
        setUpUi();
    }

    /**
     * Initializes the activity's user interface.
     */
    private void setUpUi() {
        //set up toolbar
        toolbar = findViewById(R.id.toolbarPlayground);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //connect TabLayout and ViewPager2
        final TabLayout tabLayout = findViewById(R.id.playgroundTabLayout);
        final ViewPager2 viewPager = findViewById(R.id.playgroundViewPager);
        viewPager.setAdapter(new PlaygroundAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (TabLayout.Tab tab, @PlaygroundTab int position) -> {
            switch (position) {
                case PlaygroundTab.TAB_CODE:
                    tab.setText(R.string.playground_code);
                    break;
                case PlaygroundTab.TAB_INPUT:
                    tab.setText(R.string.playground_input);
                    break;
                case PlaygroundTab.TAB_OUTPUT:
                    tab.setText(R.string.playground_output);
                    break;
            }
        }).attach();
        //start on code tab
        viewPager.setCurrentItem(PlaygroundTab.TAB_CODE);
    }

    /**
     * Launches {@link com.gaspar.learnjava.SettingsActivity} after the user clicked the settins
     * icon on the toolbar.
     * @param view The settings icon.
     */
    public void settingsOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the back button is clicked.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Called when the back button is clicked on the toolbar.
     * @return True.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}