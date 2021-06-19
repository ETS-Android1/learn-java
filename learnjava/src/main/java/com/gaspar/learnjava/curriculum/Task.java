package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.TaskActivity;
import com.gaspar.learnjava.UpdatableActivity;
import com.gaspar.learnjava.asynctask.TaskStatusDisplayerTask;
import com.gaspar.learnjava.curriculum.components.Component;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.database.TaskStatus;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a more advanced task (belongs to a course) in the curriculum. Task have a description,
 * which consists of any number of text, code and advanced information components.
 * <p>
 * Tasks also have a {@code <solution>} tag that contains the task solution, using additional
 * component tags.
 * <p>
 * Tasks are stored in XML in the assets folder.
 * <pre>
 * {@code
 * <taskdata>
 *    <id>*id of task here*</id>
 *    <name>*name of the task*</name>
 * </taskdata>
 * ...
 * component tags here...
 * ...
 * <solution>
 *    ...
 *    component tags here...
 *    ...
 * </solution>
 * }
 * </pre>
 * @see Component
 */
public class Task implements Serializable {

    /**
     * Id of the task.
     */
    private final int id;

    /**
     * Name of the task.
     */
    private final String name;

    /**
     * Displayable components of the task description.
     */
    @Nullable
    private transient List<Component> descriptionComponents;

    /**
     * Displayable components of the solution of this task.
     */
    @Nullable
    private transient List<Component> solutionComponents;

    /**
     * Status of this task. Tasks can only have an unlocked or a completed status, but the user won't
     * be able to access the tasks of a locked course.
     */
    @Status
    private int taskStatus;

    /**
     * Creates a task with components.
     * @param id The id.
     * @param name The name.
     * @param descriptionComponents The list of {@link Component}s that make up the task.
     * @param solutionComponents The list of {@link Component}s that make up the task's solution.
     */
    public Task(int id, String name, @Nullable List<Component> descriptionComponents,
                @Nullable List<Component> solutionComponents) {
        this.id = id;
        this.name = name;
        this.descriptionComponents = descriptionComponents;
        this.solutionComponents = solutionComponents;
        taskStatus = Status.NOT_QUERIED;
    }

    /**
     * Creates a task object that has no components. This can be used when only the name and ID is
     * important.
     * @param id The id.
     * @param name The name.
     */
    public Task(int id, String name) {
        this.id = id;
        this.name = name;
        taskStatus = Status.NOT_QUERIED;
    }

    /**
     * Queries the status of this task from the database, if not present the adds it. The queried
     * status is used to update the image view with an icon. Uses background thread.
     */
    public void queryAndDisplayStatus(final ImageView imageView, Context context) {
        new TaskStatusDisplayerTask(this).execute(imageView, context);
    }

    /**
     * This string identifies the task passed to a {@link TaskActivity}.
     */
    public static final String TASK_PREFERENCE_STRING = "passed_task";

    /**
     * Starts a task activity where the given task will be displayed.
     * @param fromActivity The activity that will launch the task activity.
     * @param launcher An object which can start a {@link TaskActivity} and handle the result. This can be null,
     *                 which means that we dont care about the result.
     * @param task The task that will be shown (this task does not need to have parsed
     *             components).
     * @param updateView The view that will be updated when the started activity finishes.
     */
    public static void startTaskActivity(@NonNull AppCompatActivity fromActivity, @Nullable ActivityResultLauncher<Intent> launcher, Task task, View updateView) {
        Intent intent = new Intent(fromActivity, TaskActivity.class);
        intent.putExtra(TASK_PREFERENCE_STRING, task);
        if(fromActivity instanceof UpdatableActivity) {
            ((UpdatableActivity)fromActivity).setUpdateViews(updateView); //save update view
        }
        if(launcher != null) {
            launcher.launch(intent);
        } else {
            //we dont care about the result
            fromActivity.startActivity(intent);
        }
    }

    /**
     * Checks if there is a task in the database with the given id. If not it adds this task
     * to the database with default status.
     */
    @WorkerThread
    public static void validateTaskStatus(int taskId, Context context) {
        TaskStatus status = LearnJavaDatabase.getInstance(context).getTaskDao().queryTaskStatus(taskId);
        if(status == null) { //not found in the database
            @Status final int DEF_STATUS = Status.UNLOCKED;
            TaskStatus newStatus = new TaskStatus(taskId, DEF_STATUS);
            LearnJavaDatabase.getInstance(context).getTaskDao().addTaskStatus(newStatus); //add to database
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable List<Component> getDescriptionComponents() {
        return descriptionComponents;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(@Status int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public @Nullable List<Component> getSolutionComponents() {
        return solutionComponents;
    }
}
