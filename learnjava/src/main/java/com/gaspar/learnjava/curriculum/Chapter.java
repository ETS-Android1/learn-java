package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.ChapterActivity;
import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.LearnJavaActivity;
import com.gaspar.learnjava.UpdatableActivity;
import com.gaspar.learnjava.asynctask.ChapterStatusDisplayerTask;
import com.gaspar.learnjava.database.ChapterStatus;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *   Represents a chapter in the curriculum. A Chapter is a sub component of a course,
 *   and it consists of any number of text, code or advanced information components.
 * </p>
 *
 * <p>
 *   Chapters are stored in XML, in the following format: chapter_id.xml
 * </p>
 *
 * {@code
 * <resources>
 *  *     <chapterdata>
 *  *         <id>*id here*</id>
 *  *         <name>*chapter name here*</name>
 *  *     </chapterdata>
 *  *     <text>*text component*</text>
 *  *     <code>*code component*</code>
 *  *     <advanced>*advanced information component*</advanced>
 *  </resources>
 * }
 */
public class Chapter implements Serializable {

    public static final String CHAPTER_PREFERENCE_STRING = "passed_chapter";

    /**
     * This variable stores if a chapter status is being updated (See {@link #markChapterAsCompleted(Context)}. If this is
     * true, then {@link com.gaspar.learnjava.asynctask.ExamStatusDisplayerTask}s will wait until it ends, to avoid showing
     * wrong exam statuses.
     */
    public static volatile boolean chapterStatusUpdatePending = false;

    /**
     * The id of the chapter.
     */
    private final int id;

    /**
     * The name if the chapter.
     */
    private final String name;

    /**
     * Displayable components of the chapter.
     */
    private transient List<Component> components;

    /**
     * Status of this chapter. This can only be unlocked or completed, but the user will not be able
     * to access chapters in locked courses anyways.
     */
    @Status
    private volatile int status;

    public Chapter(int id, String name, List<Component> components) {
        this.id = id;
        this.name = name;
        this.components = components;
        status = Status.NOT_QUERIED;
    }

    /**
     * Creates a chapter object that has no components. This can be used when only the name and ID is
     * important.
     */
    public Chapter(int id, String name) {
        this.id = id;
        this.name = name;
        status = Status.NOT_QUERIED;
    }

    /**
     * Finds the status of tis course in the database, saves it in
     * the status variable, and displays it with the given image view.
     * Uses a background thread.
     */
    public void queryAndDisplayStatus(final ImageView imageView, Context context) {
        new ChapterStatusDisplayerTask(this).execute(imageView, context);
    }

    /**
     * Starts a chapter activity for result for result. It will pass the chapter and the status icon
     * of this chapter. Exam data and view are also needed, as a completed chapter may unlock the exam in its course,
     * if it was the last uncompleted chapter.
     *
     * @param updateView The view that will be updated when the started activity finishes.
     */
    public static void startChapterActivity(AppCompatActivity fromActivity, Chapter chapter, @Nullable View updateView,
                                            @Nullable Exam exam, @Nullable View extraExamView) {
        SharedPreferences prefs = fromActivity.
                getSharedPreferences(LearnJavaActivity.APP_PREFERENCES_NAME, Context.MODE_PRIVATE); //save started chapter
        prefs.edit().putInt(LearnJavaActivity.ACTIVE_CHAPTER_ID_PREFERENCE, chapter.id).apply();
        Intent intent = new Intent(fromActivity, ChapterActivity.class);
        intent.putExtra(CHAPTER_PREFERENCE_STRING, chapter);
        if(exam != null) intent.putExtra(Exam.EXAM_PREFERENCE_STRING, exam); //also pass the exam, as it may be needed for update
        if(fromActivity instanceof UpdatableActivity) {
            ((UpdatableActivity)fromActivity).setUpdateViews(updateView, extraExamView); //save update view, also pass exam view
        }
        fromActivity.startActivityForResult(intent, CoursesActivity.CHAPTER_REQUEST_CODE); //start with chapters code
    }

    /**
     * Updates a chapter in the database to have the {@link Status#COMPLETED} status. After updating
     * it checks if all the chapters in the course have been confirmed, and if so it unlocks the exam of the course.
     */
    @UiThread
    public void markChapterAsCompleted(Context context) {
        chapterStatusUpdatePending = true; //start a chapter status update
        LearnJavaDatabase.DB_EXECUTOR.execute(() -> {
            ChapterStatus newStatus = new ChapterStatus(id,
                    com.gaspar.learnjava.curriculum.Status.COMPLETED);
            LearnJavaDatabase.getInstance(context).getChapterDao().updateChapterStatus(newStatus);

            if(CoursesActivity.coursesNotParsed()) { //check if courses are parsed
                try {
                    CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance().parseCourses(context));
                } catch (Exception e) {
                    Log.e("LearnJava", "Exception", e);
                }
            }
            Course courseOfChapter = null; //find course of chapter
            outerLoop:
            for(Course course: CoursesActivity.getParsedCourses()) {
                for(Chapter chapter: course.getChapters()) {
                    if(chapter.getId() == id) {
                        courseOfChapter = course;
                        break outerLoop;
                    }
                }
            }
            if(courseOfChapter == null) throw new RuntimeException("Resource integrity error!");
            ExamStatus eStatus = LearnJavaDatabase.getInstance(context).getExamDao()
                    .queryExamStatus(courseOfChapter.getExam().getId());
            if(eStatus.getStatus() != Status.LOCKED) return; //if already completed/unlocked the no need to check
            boolean allConfirmed = true;
            List<ChapterStatus> statuses = LearnJavaDatabase.getInstance(context).getChapterDao().getAllChapterStatuses();
            for(ChapterStatus chapterStatus: statuses) {
                if(chapterStatus.getStatus() != Status.COMPLETED && inThisCourse(chapterStatus.getChapterId(), courseOfChapter)) {
                    //found a chapter in the course that is not completed
                    allConfirmed = false;
                    break;
                }
            }
            if(allConfirmed) { //exam still locked, all chapters confirmed
                //unlock exam in database
                LearnJavaDatabase.getInstance(context).getExamDao().
                        updateExamCompletionStatus(courseOfChapter.getExam().getId(), Status.UNLOCKED);
            }
            chapterStatusUpdatePending = false; //when the background thread is done, no more chapter updating
        });
    }

    private boolean inThisCourse(int chapterId, Course course) {
        for(Chapter chapter: course.getChapters()) {
            if(chapter.getId() == chapterId) return true;
        }
        return false;
    }

    /**
     * Checks if there is a chapter in the database with the given id. If not it adds this chapter
     * to the database with default status.
     */
    @WorkerThread
    public static void validateChapterStatus(int chapterId, Context context) {
        Log.d("LearnJava","Validation chapter " + chapterId);
        ChapterStatus status = LearnJavaDatabase.getInstance(context).getChapterDao().queryChapterStatus(chapterId);
        if(status == null) { //not found in the database
            Log.d("LearnJava", "This chapter was not in the database, adding...");
            @Status final int DEF_STATUS = Status.UNLOCKED;
            ChapterStatus newStatus = new ChapterStatus(chapterId, DEF_STATUS);
            LearnJavaDatabase.getInstance(context).getChapterDao().addChapterStatus(newStatus); //add to database
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Component> getComponents() {
        return components;
    }

    public @Status int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }
}
