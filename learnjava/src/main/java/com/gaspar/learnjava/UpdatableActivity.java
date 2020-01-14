package com.gaspar.learnjava;

import android.view.View;

/**
 * Activity that stores  updatable views, for example the icon of a chapter, task, etc...
 * <br><br>
 * When the other activity finishes, these views are updated to reflect changes.
 */
public interface UpdatableActivity {

    void setUpdateViews(View... updateView);

}
