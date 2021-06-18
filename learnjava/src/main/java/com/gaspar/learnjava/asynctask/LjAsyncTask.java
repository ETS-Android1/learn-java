package com.gaspar.learnjava.asynctask;

import androidx.annotation.AnyThread;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.utils.LogUtils;

/**
 * This class is a replacement for the standard {@link android.os.AsyncTask}, since that one is deprecated.
 * It is designed to be work in the same way, so existing code that used {@link android.os.AsyncTask} could
 * be easily updated to this class, no big refactoring and new multithreading framework needed.
 * <p>
 * Some functions of {@link android.os.AsyncTask} are not supported, such as the progress update.
 * {@link Object} is used as parameter type, instead of generic type to avoid unsafe vararg situations.
 * @param <Result> The result of the background work.
 */
public abstract class LjAsyncTask<Result> {

    /**
     * For synchronization, and thread interactions.
     */
    private final Object monitor;

    /**
     * Stores if this task is currently running or not.
     */
    private volatile boolean running;

    /**
     * Creates a task.
     */
    protected LjAsyncTask() {
        monitor = new Object();
        running = false;
    }

    /**
     * Begins the execution of this task. At first, {@link #doInBackground(Object[])} will be called on
     * a worker thread, then its result is passed to {@link #onPostExecute(Object)}, which is run on the
     * UI thread.
     * @param objects Any number of parameters, of any type.
     */
    @AnyThread
    public final void execute(final Object... objects) {
        setRunning(true);
        LearnJavaExecutor.getInstance().executeOnBackgroundThread(() -> {
            //first run onPreExecute and wait for it to complete
            synchronized (monitor) {
                LearnJavaExecutor.getInstance().executeOnUiThread(() -> {
                    onPreExecute();
                    synchronized (monitor) {
                        monitor.notify();
                    }
                });
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    LogUtils.logError("Failed to wait for onPreExecute!", e);
                }
            }
            //on pre execute completed on the UI thread, start the background work
            Result result = doInBackground(objects);
            //run on post execute on the UI thread and wait for it to complete
            synchronized (monitor) {
                LearnJavaExecutor.getInstance().executeOnUiThread(() -> {
                    onPostExecute(result);
                    synchronized (monitor) {
                        monitor.notify();
                    }
                });
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    LogUtils.logError("Failed to wait for onPostExecute!", e);
                }
            }
            setRunning(false);
        });
    }

    /**
     * Called on the UI thread to perform some initialization before the background task starts. Does
     * nothing by default.
     */
    @UiThread
    protected void onPreExecute() {}

    /**
     * Performs the work intensive part of this task, on a background thread. The result is then
     * passed to {@link #onPostExecute(Object)}.
     * @param objects Any number of parameters, of any type.
     * @return The result of the task, any type.
     */
    @WorkerThread
    protected abstract Result doInBackground(Object... objects);

    /**
     * Performs non-intensive operations on the UI thread, using the result of {@link #doInBackground(Object...)}.
     * Does nothing by default.
     * @param result The result of the task, any type.
     */
    @UiThread
    protected void onPostExecute(Result result) {}

    /**
     * @return If the task is running.
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * Update the running flag.
     * @param running The new flag.
     */
    private synchronized void setRunning(boolean running) {
        this.running = running;
    }
}
