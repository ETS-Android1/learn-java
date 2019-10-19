package com.gaspar.learnjava.curriculum;

import androidx.annotation.IntDef;

/**
 * Completion/Unlocked constants for courses, exams, etc.
 */
@IntDef({Status.NOT_QUERIED, Status.LOCKED, Status.UNLOCKED, Status.COMPLETED})
public @interface Status {

    /**
     * This is the default on all objects. Updated when the real status is queried from
     * the database
     */
    int NOT_QUERIED = -1;

    /**
     * Status of locked curriculum parts.
     */
    int LOCKED = 0;

    /**
     * Status of unlocked, but not completed parts.
     */
    int UNLOCKED = 1;

    /**
     * Status of unlocked and completed parts.
     */
    int COMPLETED = 2;
}
