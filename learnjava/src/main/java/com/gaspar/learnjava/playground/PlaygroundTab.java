package com.gaspar.learnjava.playground;

import androidx.annotation.IntDef;

import com.google.android.material.tabs.TabLayout;

/**
 * Constants for the tabs inside {@link PlaygroundActivity}.
 */
@IntDef({PlaygroundTab.TAB_INPUT, PlaygroundTab.TAB_CODE, PlaygroundTab.TAB_OUTPUT})
public @interface PlaygroundTab {

    /**
     * The position of the input tab inside the {@link TabLayout}.
     */
    int TAB_INPUT = 0;

    /**
     * The position of the code tab inside the {@link TabLayout}.
     */
    int TAB_CODE = 1;

    /**
     * The position of the output tab inside the {@link TabLayout}.
     */
    int TAB_OUTPUT = 2;

}
