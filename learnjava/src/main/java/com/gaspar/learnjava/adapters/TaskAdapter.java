package com.gaspar.learnjava.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;

import java.util.List;

/**
 * Creates a list of task selector views (grouped by courses). Each element of the adapter
 * will create a 'tasks_of_course' view.
 */
public class TaskAdapter extends ArrayAdapter<Course> {

    private AppCompatActivity activity;

    /**
     * Alpha animation applied to task selectors on click.
     */
    private final Animation clickAnimation;

    public TaskAdapter(AppCompatActivity activity, @Size(min=1) List<Course> courses) {
        super(activity, R.layout.tasks_of_course, courses);
        this.activity = activity;
        clickAnimation = AnimationUtils.loadAnimation(activity, R.anim.click);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TaskViewHolder holder;
        final Course course = getItem(position);
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.tasks_of_course, parent, false);
            holder = new TaskViewHolder();
            holder.courseNameView = convertView.findViewById(R.id.courseNameView);
            holder.courseStatusIcon = convertView.findViewById(R.id.statusIconView);
            holder.tasksLayout = convertView.findViewById(R.id.tasksOfCourse);
            convertView.setTag(holder);
        } else { //recycling
            holder = (TaskViewHolder)convertView.getTag();
        }
        if(course != null) { //fill data here using view holder
            holder.courseNameView.setText(course.getCourseName());
            Runnable showTasksAtEnd = () -> { //runnable that shows the tasks for unlocked courses
                if(course.getStatus() != Status.LOCKED) {
                    holder.tasksLayout.setVisibility(View.VISIBLE);
                }
            };
            course.queryAndDisplayStatus(holder.courseStatusIcon, showTasksAtEnd, activity);
            addTasksToCourse(course, holder.tasksLayout); //add tasks selectors
        }
        return convertView;
    }

    /**
     * Adds 'task_selector_view' layouts to the linear layout.
     *
     * @param course The course to which these tasks belong to.
     * @param tasksLayout The layout the views will be added to.
     */
    private void addTasksToCourse(final Course course, ViewGroup tasksLayout) {
        tasksLayout.removeAllViews();
        for(Task task: course.getTasks()) {
            View taskSelectorView = View.inflate(activity, R.layout.task_selector_view, null);
            taskSelectorView.setOnClickListener(view -> {
                view.startAnimation(clickAnimation);
                taskSelectorOnClick(course, task, view);
            });
            TextView taskNameView = taskSelectorView.findViewById(R.id.taskNameView); //set status and name
            taskNameView.setText(task.getName());
            task.queryAndDisplayStatus(taskSelectorView.findViewById(R.id.taskStatusIcon), activity);
            tasksLayout.addView(taskSelectorView);
        }
    }

    /**
     * Handles what happens when a click is made on a task selector view.
     */
    private void taskSelectorOnClick(final Course course, final Task task, View taskView) {
        if(course.getStatus() == Status.NOT_QUERIED) return;
        if(course.getStatus() == Status.LOCKED) { //notify user that course must be unlocked.
            buildDialog().show();
        } else { //unlocked or completed
           Task.startTaskActivity(activity, task, taskView);
        }
    }

    @NonNull
    @CheckResult
    private AlertDialog buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.must_unlock_course_before_task);
        builder.setPositiveButton(R.string.ok, (dialog,id) -> dialog.dismiss());
        return builder.create();
    }

    private static class TaskViewHolder {
        private TextView courseNameView;
        private ImageView courseStatusIcon;
        private ViewGroup tasksLayout;
    }
}
