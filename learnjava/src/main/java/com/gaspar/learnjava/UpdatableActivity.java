package com.gaspar.learnjava;

import android.view.View;

/**
 * Interface for an activity that stores  updatable views, for example the icon of a chapter, task, etc...
 * <br><br>
 * When the other activity that modifies these views finishes, the views are updated to reflect changes.
 *
 * For example: The {@link CoursesActivity} implements this and when a {@link ChapterActivity} is opened
 * from there, the status icon of that activity will be an updatable view. If the user completed the
 * chapter, the status icon will change to a green tick, without reloading the full courses activity.
 */
public interface UpdatableActivity {

    /**
     * Registers {@link View}s as update views. These are going to be modified in the future.
     * @param updateView The views.
     */
    void setUpdateViews(View... updateView);

}
