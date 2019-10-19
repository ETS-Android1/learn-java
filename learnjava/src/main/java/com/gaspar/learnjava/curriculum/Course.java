package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.WorkerThread;

import com.gaspar.learnjava.asynctask.CourseStatusDisplayerTask;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *    Represents a course in the curriculum. Courses can have any number of chapters, tasks and
 *    exactly one exam.
 * </p>
 *
 * <p>
 *     Courses are stored in XML, in the following format: course_id.xml
 * </p>
 *
 * {@code
 * <resources>
 *  *     <coursedata>
 *  *         <id>*id here*</id>
 *  *         <name>*name here*</name>
 *  *     </coursedata>
 *  *     <chapter>*id of the chapter*</chapter>
 *  *     ...
 *  *     <chapter>*id of the chapter*</chapter>
 *  *     <task>*id of the task*</task>
 *  *     ...
 *  *     <task>*id of the task*</task>
 *  *     <exam>*id of the exam*</exam>
 *  * </resources>
 * }
 *
 * <p>
 *   The course id's MUST be in the same order as the courses are in the curriculum. For example,
 *   if Course A is before Course B then Course A must have a strictly smaller ID then Course B.
 * </p>
 */
public class Course implements Serializable {

    /**
     * The id of the course.
     */
    private int id;

    /**
     * Name of the course.
     */
    private String courseName;

    /**
     * The chapters of the course.
     */
    private List<Chapter> chapters;

    /**
     * The tasks that belong to this course.
     */
    private List<Task> tasks;

    /**
     * The exam belonging to this course.
     */
    private Exam exam;

    private transient Context context;

    /**
     * The completion/unlock status of this course. Queried from the database.
     */
    @Status
    private volatile int status;

    public Course(int id, String name, List<Chapter> chapters,
                  List<Task> tasks, Exam exam, Context context) {
        this.id = id;
        this.courseName = name;
        this.chapters = chapters;
        this.tasks = tasks;
        this.exam = exam;
        this.context = context;
        status = Status.NOT_QUERIED;
    }

    /**
     * Sets the status view for this object. This is done some time after creation.
     * This will also start querying the database for the status of this course, and then
     * displaying the status using the image view.
     */
    public void queryAndDisplayStatus(final ImageView imageView) {
        new CourseStatusDisplayerTask(this).execute(imageView, context);
    }

    /**
     * Same as the method above but with an additional runnable that will be called when the task
     * ends, on the UI thread.
     */
    public void queryAndDisplayStatus(final ImageView imageView, Runnable callAtEnd) {
        new CourseStatusDisplayerTask(this, callAtEnd).execute(imageView, context);
    }

    /**
     * Checks if there is a course in the database with the given id. If not it adds this course
     * to the database.
     */
    @WorkerThread
    public static void validateCourseStatus(int courseId, Context context) {
        CourseStatus status = LearnJavaDatabase.getInstance(context).getCourseDao().queryCourseStatus(courseId);
        if(status == null) { //not found in the database
            CourseStatus newStatus;
            if(CourseStatus.getCourseCount() == 0) { //this is first course to be added to the database
                newStatus = new CourseStatus(courseId, Status.UNLOCKED); //first one is unlocked
            } else { //there are other courses already
                int DEFAULT_STATUS = Status.LOCKED;
                newStatus = new CourseStatus(courseId, DEFAULT_STATUS);
            }
            LearnJavaDatabase.getInstance(context).getCourseDao().addCourseStatus(newStatus); //add to database
            CourseStatus.incrementCourseCount();
        }
    }

    public int getId() {
        return id;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Exam getExam() {
        return exam;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
    }
}
