/**
 * The package of background tasks and multithreading. This is extensively used in the application for
 * loading from XML and database, and performing bluetooth and network calls.
 * <p>
 * The core class is {@link com.gaspar.learnjava.asynctask.LjAsyncTask}, which is my own version of {@link android.os.AsyncTask},
 * that was created to avoid using deprecated {@link android.os.AsyncTask}.
 * <p>
 * At first, the application relied heavily on {@link android.os.AsyncTask}, and as it was deprecated I did not want
 * to rewrite the complete multithreading framework, so I created a class that works similarly to the
 * original: {@link com.gaspar.learnjava.asynctask.LjAsyncTask}.
 */
package com.gaspar.learnjava.asynctask;