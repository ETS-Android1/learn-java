package com.gaspar.learnjava.asynctask;

import android.content.Context;

import androidx.annotation.Size;

import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.PlaygroundFile;
import com.gaspar.learnjava.playground.CodeFragment;

import java.util.List;
import java.util.Objects;

/**
 * This class queries the saved {@link PlaygroundFile}s from the database, then displays them inside the
 * received {@link CodeFragment}.
 */
public class QueryPlaygroundFilesTask extends LjAsyncTask<QueryPlaygroundFilesTask.Result> {

    /**
     * Performs the query in the background.
     * @param objects Expected to contain the {@link CodeFragment}.
     * @return The {@link Result}.
     */
    @Override
    protected Result doInBackground(@Size(1) Object... objects) {
        CodeFragment codeFragment = (CodeFragment) objects[0];
        final Context context = codeFragment.getContext();
        Objects.requireNonNull(context);
        List<PlaygroundFile> playgroundFiles = LearnJavaDatabase.getInstance(context).getPlaygroundFileDao().queryPlaygroundFiles();
        return new Result(playgroundFiles, codeFragment);
    }

    /**
     * Calls {@link CodeFragment#displayPlaygroundFiles(List)} after the files have been fetched
     * from the database.
     * @param result The result of the task.
     */
    @Override
    protected void onPostExecute(Result result) {
        result.codeFragment.displayPlaygroundFiles(result.playgroundFiles);
    }

    /**
     * Helper class to group the result of the task.
     */
    static class Result {

        private List<PlaygroundFile> playgroundFiles;

        private CodeFragment codeFragment;

        public Result(List<PlaygroundFile> playgroundFiles, CodeFragment codeFragment) {
            this.playgroundFiles = playgroundFiles;
            this.codeFragment = codeFragment;
        }
    }
}
