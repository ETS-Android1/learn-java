package com.gaspar.learnjava;

import android.view.View;

/**
 * Activity that stores  updatable views, for example the icon of a chapter, task, etc...
 * <br><br>
 * When the other activity that modifies these views finishes, the views are updated to reflect changes.
 *
 * For example: The {@link CoursesActivity} implements this and when a {@link ChapterActivity} is opened
 * from there, the status icon of that activity will be an updatable view. If the user completed the
 * chapter, the status icon will change to a green tick, without reloading the courses activity.
 */
public interface UpdatableActivity {

    void setUpdateViews(View... updateView);

}
