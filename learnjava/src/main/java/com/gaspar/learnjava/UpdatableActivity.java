package com.gaspar.learnjava;

import android.view.View;

/**
 * Activity that stores an updatable view, for example the icon of a chapter, task, ets...
 * <br><br>
 * When the other activity finishes, this view is updated to reflect changes.
 */
public interface UpdatableActivity {

    void setUpdateView(View updateView);

    /**
     * Chapter activities also set this, as they may need to update the exam status from locked to unlocked, not
     * only their own view.
     */
    void setExtraExamView(View extraExamView);

}
