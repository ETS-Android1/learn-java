package com.gaspar.learnjava.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gaspar.learnjava.R;
import com.gaspar.learnjava.curriculum.Chapter;
import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.curriculum.Task;

import java.lang.reflect.Field;

/**
 * A singleton class representing the app database.
 */
@Database(entities = {CourseStatus.class, ChapterStatus.class, TaskStatus.class, ExamStatus.class},
        version = 1, exportSchema = false)
@WorkerThread
public abstract class LearnJavaDatabase extends RoomDatabase {

    private static LearnJavaDatabase instance;

    public static LearnJavaDatabase getInstance(@NonNull Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context, LearnJavaDatabase.class, "learn_java_database")
                    .build();
        }
        return instance;
    }

    public abstract CourseDao getCourseDao();

    public abstract ChapterDao getChapterDao();

    public abstract TaskDao getTaskDao();

    public abstract ExamDao getExamDao();

    /**
     * Goes through all XML resource files and checks if the curriculum elements (course, task, ...)
     * are added to the database or not. If not it adds them.
     */
    public static void validateDatabase(@NonNull Context context) {
        final Field[] fields = R.xml.class.getDeclaredFields();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith("course_")) {
                Course.validateCourseStatus(parseId(resourceName), context);
            } else if(resourceName.startsWith("chapter_")) {
                Chapter.validateChapterStatus(parseId(resourceName), context);
            } else if(resourceName.startsWith("task_")) {
                Task.validateTaskStatus(parseId(resourceName), context);
            } else if(resourceName.startsWith("exam_")) {
                Exam.validateExamStatus(parseId(resourceName), context);
            } //other resource like guide is not tracked in the database
        }
    }

    /**
     * Deletes all records from the database.
     */
    public static void resetDatabase(@NonNull Context context) {
        LearnJavaDatabase database = LearnJavaDatabase.getInstance(context);
        database.getCourseDao().deleteRecords();
        database.getChapterDao().deleteRecords();
        database.getExamDao().deleteRecords();
        database.getTaskDao().deleteRecords();
        CourseStatus.setCourseCount(0, context); //also reset course counter variable
    }

    private static int parseId(String resourceName) throws IllegalArgumentException {
        String[] parts = resourceName.split("_");
        if(parts.length != 2) throw new IllegalArgumentException();
        return Integer.parseInt(parts[1]);
    }

    /*
     * ----------------------------- MIGRATIONS ----------------------------------------------------
     */
    /*
    private static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };
    */
}
