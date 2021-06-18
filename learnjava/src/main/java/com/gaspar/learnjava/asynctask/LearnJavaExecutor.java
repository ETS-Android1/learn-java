package com.gaspar.learnjava.asynctask;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

/**
 * A singleton class that can execute tasks on the UI thread, and on background threads as well,
 * from a managed thread pool. Use {@link #getInstance()} to access the singleton.
 * <p>
 * Uses a simple third party, <a href="https://github.com/techyourchance/thread-poster">ThreadPoster</a>.
 */
public class LearnJavaExecutor {

    /**
     * The instance.
     */
    private static LearnJavaExecutor instance;

    /**
     * @return The instance
     */
    public static synchronized LearnJavaExecutor getInstance() {
        if(instance == null) {
            instance = new LearnJavaExecutor();
        }
        return instance;
    }

    /**
     * An object that can execute {@link Runnable}s on the UI thread.
     */
    private final UiThreadPoster uiThreadPoster;

    /**
     * An object that can execute {@link Runnable}s on a managed pool of background threads.
     */
    private final BackgroundThreadPoster backgroundThreadPoster;

    /**
     * Private constructor.
     */
    private LearnJavaExecutor() {
        uiThreadPoster = new UiThreadPoster();
        backgroundThreadPoster = new BackgroundThreadPoster();
    }

    /**
     * Executes the {@link Runnable} on the UI thread.
     * @param runnable The runnable.
     */
    @AnyThread
    public void executeOnUiThread(@NonNull final Runnable runnable) {
        uiThreadPoster.post(runnable);
    }

    /**
     * Executes the {@link Runnable} on a background thread.
     * @param runnable The runnable.
     */
    @AnyThread
    public void executeOnBackgroundThread(@NonNull final Runnable runnable) {
        backgroundThreadPoster.post(runnable);
    }

}
