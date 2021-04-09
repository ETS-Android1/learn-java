package com.gaspar.learnjava.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Status;
import com.gaspar.learnjava.curriculum.Task;

import java.util.List;

/**
 * Fills the list view in {@link com.gaspar.learnjava.CoursesActivity} with views for each
 * parsed course object.
 */
public class CourseAdapter extends ArrayAdapter<Course> {

    /**
     * The activity, in which the list view is shown.
     */
    private final CoursesActivity activity;

    /**
     * Alpha animation applied to selectors on click.
     */
    private final Animation clickAnimation;

    public CourseAdapter(CoursesActivity activity, @Size(min=1) List<Course> courses) {
        super(activity, R.layout.course_selector_view, courses);
        this.activity = activity;
        clickAnimation = AnimationUtils.loadAnimation(activity, R.anim.click);
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CourseViewHolder viewHolder;
        Course course = getItem(position);
        if(convertView == null) { //first time
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.course_selector_view, parent, false);
            viewHolder = new CourseViewHolder();
            viewHolder.courseNameView = convertView.findViewById(R.id.courseNameView); //cache views
            viewHolder.statusIcon = convertView.findViewById(R.id.statusIconView);
            viewHolder.chaptersView = convertView.findViewById(R.id.chaptersView);
            viewHolder.tasksView = convertView.findViewById(R.id.tasksView);
            viewHolder.examView = convertView.findViewById(R.id.examSelector);
            viewHolder.showHideView = convertView.findViewById(R.id.slideInView);
            viewHolder.courseNameBar = convertView.findViewById(R.id.courseNameBar);
            convertView.setTag(viewHolder);
        } else { //recycling
            viewHolder = (CourseViewHolder)convertView.getTag();
        }
        if(course != null) { //fill data here using view holder
            viewHolder.courseNameView.setText(course.getCourseName());
            //register listener that shows or hides contents
            viewHolder.courseNameBar.setOnClickListener(v -> onCourseNameClick(position, viewHolder.statusIcon, viewHolder.showHideView));
            course.queryAndDisplayStatus(viewHolder.statusIcon, activity);
            addHiddenViews(viewHolder, course); //add chapter, task exam selectors
        }
        return convertView;
    }

    /**
     * Called when the user taps the name of one of the courses. Will show the contents if the course is unlocked.
     * In debug mode, it will always show the contents.
     * @param position Position of the item that was clicked. Used to find which course this view belongs to.
     * @param iconView Status icon of the course. Animated when a locked course is tapped.
     * @param showHideView This is the part of the course item view that is shown/hidden on click.
     */
    private void onCourseNameClick(int position, @NonNull final View iconView, @NonNull final View showHideView) {
        Log.e("LearnJava","Clicked on course name! " + LearnJavaActivity.DEBUG);
        Course c = CoursesActivity.getParsedCourses().get(position);
        if(!LearnJavaActivity.DEBUG) { //only some shaking happens on locked, except in debug
            if(c.getStatus() == Status.LOCKED || c.getStatus()==Status.NOT_QUERIED) {
                iconView.findViewById(R.id.statusIconView).
                        startAnimation(android.view.animation.AnimationUtils.loadAnimation(iconView.getContext(), R.anim.shake));
                return;
            }
        }
        if(showHideView.getVisibility() == View.GONE) {
            com.gaspar.learnjava.utils.AnimationUtils.slideIn(showHideView);
        } else { //visible
            com.gaspar.learnjava.utils.AnimationUtils.slideOut(showHideView);
        }
    }

    /**
     * Adds the initially hidden views of chapter, task and exam selectors to the course view.
     *
     * @param viewHolder The cache object of this course selector view.
     * @param course The course object of this view.
     */
    private void addHiddenViews(CourseViewHolder viewHolder, Course course) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        viewHolder.chaptersView.removeAllViews(); //remove previous chapters
        for(Chapter chapter: course.getChapters()) { //add all chapters
            View chapterView = inflater.inflate(R.layout.chapter_selector_view, viewHolder.chaptersView, false);
            chapter.queryAndDisplayStatus(chapterView.findViewById(R.id.chapterStatusIcon), activity); //query and show status
            viewHolder.chaptersView.addView(chapterView);
            setUpChapterView(chapterView, chapter, course.getExam(), viewHolder.examView);
        }
        viewHolder.tasksView.removeAllViews(); //remove previous tasks
        for(Task task: course.getTasks()) { //add all tasks
            View taskView = inflater.inflate(R.layout.task_selector_view, viewHolder.tasksView, false);
            task.queryAndDisplayStatus(taskView.findViewById(R.id.taskStatusIcon), activity);
            viewHolder.tasksView.addView(taskView);
            setUpTaskView(taskView, task);
        }
        course.getExam().queryAndDisplayStatus(viewHolder.examView, activity); //set up exam selector
    }

    /**
     * Sets the text, listeners and the status icon of a chapter view. See {@link Chapter#startChapterActivity(AppCompatActivity, Chapter, View, Exam, View)}
     * for explanation on why exam components are needed.
     */
    private void setUpChapterView(final View chapterView, @NonNull Chapter chapter, @Nullable Exam exam, final View extraExamView) {
        TextView chapterNameView = chapterView.findViewById(R.id.chapterNameView);
        chapterNameView.setText(chapter.getName());
        chapterView.setOnClickListener(view -> { //redirect to chapter activity
            Chapter.startChapterActivity(activity, chapter, chapterView, exam, extraExamView);
            view.startAnimation(clickAnimation);
        });
    }

    /**
     * Sets the text, listeners and the status icon of a task view.
     */
    private void setUpTaskView(final View taskView, @NonNull Task task) {
        TextView taskNameView = taskView.findViewById(R.id.taskNameView);
        taskNameView.setText(task.getName());
        taskView.setOnClickListener(view -> { //redirect to task activity if not locked.
            Task.startTaskActivity(activity, task, taskView);
            view.startAnimation(clickAnimation);
        });
    }

    /**
     * This class caches a course selector view.
     */
    static class CourseViewHolder {
        ImageView statusIcon;
        TextView courseNameView;
        LinearLayout chaptersView;
        LinearLayout tasksView;
        View examView;
        View showHideView;
        View courseNameBar;
    }

}
