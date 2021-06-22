package com.gaspar.learnjava.asynctask;

import android.content.Context;

import androidx.annotation.Size;

import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.PlaygroundFile;
import com.gaspar.learnjava.database.PlaygroundFileDao;

import java.util.List;

/**
 * This background task saves the list of {@link com.gaspar.learnjava.database.PlaygroundFile}s stored in
 * the {@link com.gaspar.learnjava.playground.CodeFragment} to the database.
 */
public class SavePlaygroundFilesTask extends LjAsyncTask<Void> {

    /**
     * List of files to be saved.
     */
    private final List<PlaygroundFile> playgroundFiles;

    /**
     * Creates a playground file save task.
     * @param playgroundFiles List of files to be saved.
     */
    public SavePlaygroundFilesTask(List<PlaygroundFile> playgroundFiles) {
        this.playgroundFiles = playgroundFiles;
    }

    /**
     * Performs the database update in the background.
     * @param objects Expected to be a {@link android.content.Context} object.
     * @return Unused.
     */
    @Override
    protected Void doInBackground(@Size(1) Object... objects) {
        final Context context = (Context) objects[0];
        final PlaygroundFileDao playgroundFileDao = LearnJavaDatabase.getInstance(context).getPlaygroundFileDao();
        //remove previous save
        playgroundFileDao.deleteRecords();
        //insert new save
        for(PlaygroundFile playgroundFile: playgroundFiles) {
            playgroundFileDao.insertOrUpdatePlaygroundFile(playgroundFile);
        }
        return null;
    }

}
