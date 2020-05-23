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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A singleton class representing the app database.
 */
@Database(entities = {CourseStatus.class, ChapterStatus.class, TaskStatus.class, ExamStatus.class},
        version = 1, exportSchema = false)
@WorkerThread
public abstract class LearnJavaDatabase extends RoomDatabase {

    private static LearnJavaDatabase instance;

    /**
     * Will be used to execute queries in the background.
     */
    public static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

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
        List<Integer> courseIdList = new ArrayList<>();
        for (Field field : fields) {
            final int xmlResourceID;
            try {
                xmlResourceID = field.getInt(R.xml.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            String resourceName = context.getResources().getResourceEntryName(xmlResourceID);
            if(resourceName.startsWith("course_")) {
                courseIdList.add(parseId(resourceName)); //courses must be sorted by id before validating, see below
            } else if(resourceName.startsWith("chapter_")) {
                Chapter.validateChapterStatus(parseId(resourceName), context);
            } else if(resourceName.startsWith("task_")) {
                Task.validateTaskStatus(parseId(resourceName), context);
            } else if(resourceName.startsWith("exam_")) {
                Exam.validateExamStatus(parseId(resourceName), context);
            } //other resource like guide is not tracked in the database
        }
        /* important, as the one with the smallest id is the first course, and that must get unlocked by default. */
        Collections.sort(courseIdList);
        for(int courseId: courseIdList) {
            Course.validateCourseStatus(courseId, context);
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
        CourseStatus.initCourseCount(0, context); //also reset course counter variable
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
