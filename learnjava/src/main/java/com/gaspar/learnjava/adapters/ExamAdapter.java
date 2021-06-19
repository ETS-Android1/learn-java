package com.gaspar.learnjava.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.gaspar.learnjava.ExamsActivity;
import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Course;

import java.util.List;

/**
 * Creates a named exam view for each exam available in the application.
 */
public class ExamAdapter extends ArrayAdapter<Course> {

    /**
     * The activity in which the adapter displays views.
     */
    private final ExamsActivity activity;

    /**
     * Creates an exam adapter.
     * @param activity The activity of the adapter.
     * @param courses The list of courses (for each course there is exactly one exam with the same name).
     */
    public ExamAdapter(@NonNull ExamsActivity activity, @Size(min=1) List<Course> courses) {
        super(activity, R.layout.selector_named_exam, courses);
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ExamViewHolder holder;
        Course course = getItem(position);
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            convertView = inflater.inflate(R.layout.selector_named_exam, parent, false);
            holder = new ExamViewHolder();
            holder.examNameView = convertView.findViewById(R.id.examNameView);
            holder.examView = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ExamViewHolder) convertView.getTag();
        }
        if(course != null) {
            holder.examNameView.setText(course.getCourseName());
            course.getExam().queryAndDisplayStatus(holder.examView, activity);
        }
        return convertView;
    }

    private static class ExamViewHolder {
        private TextView examNameView;
        private View examView;
    }
}
