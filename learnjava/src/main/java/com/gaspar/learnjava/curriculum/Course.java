package com.gaspar.learnjava.curriculum;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.gaspar.learnjava.CoursesActivity;
import com.gaspar.learnjava.asynctask.CourseStatusDisplayerTask;
import com.gaspar.learnjava.database.CourseStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.utils.LogUtils;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *    Represents a course in the curriculum. Courses can have any number of chapters, tasks and
 *    exactly one exam. They may be finished or unfinished. If a course is unlocked, but unfinished then the user
 *    will see that, and it's exam can't be started.
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
 *            <finished>true</finished>
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
    private final int id;

    /**
     * Name of the course.
     */
    private final String courseName;

    /**
     * The chapters of the course.
     */
    private final List<Chapter> chapters;

    /**
     * The tasks that belong to this course.
     */
    private final List<Task> tasks;

    /**
     * The exam belonging to this course.
     */
    private final Exam exam;

    /**
     * Stores if this course is finished.
     */
    private final boolean finished;

    /**
     * The completion/unlock status of this course. Queried from the database.
     */
    @Status
    private volatile int status;

    public Course(int id, String name, List<Chapter> chapters, List<Task> tasks, Exam exam, boolean finished) {
        this.id = id;
        this.courseName = name;
        this.chapters = chapters;
        this.tasks = tasks;
        this.exam = exam;
        this.finished = finished;
        status = Status.NOT_QUERIED;
    }

    /**
     * Sets the status view for this object. This is done some time after creation.
     * This will also start querying the database for the status of this course, and then
     * displaying the status using the image view.
     * @param imageView The status icon.
     * @param activity The activity in which the status wil be displayed.
     * @param autoOpenView The contents of the course view, which can be auto opened,
     *                     depending on settings and if the course is unlocked.
     */
    public void queryAndDisplayStatus(final ImageView imageView, final AppCompatActivity activity, View autoOpenView) {
        new CourseStatusDisplayerTask(this).execute(imageView, activity, autoOpenView);
    }
    
    /**
     * Same as the method above but with an additional runnable that will be called when the task
     * ends, on the UI thread.
     * @param imageView The status icon.
     * @param activity The activity in which the status wil be displayed.
     */
    public void queryAndDisplayStatus(final ImageView imageView, Runnable callAtEnd, final AppCompatActivity activity) {
        new CourseStatusDisplayerTask(this, callAtEnd).execute(imageView, activity);
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
            LogUtils.log("Validating new course, count: " + CourseStatus.getCourseCount());
            if(CourseStatus.getCourseCount() == 0) { //this is first course to be added to the database
                newStatus = new CourseStatus(courseId, Status.UNLOCKED); //first one is unlocked
            } else { //there are other courses already
                int DEFAULT_STATUS = Status.LOCKED;
                newStatus = new CourseStatus(courseId, DEFAULT_STATUS);
            }
            LearnJavaDatabase.getInstance(context).getCourseDao().addCourseStatus(newStatus); //add to database
        }
        CourseStatus.incrementCourseCount(); //increment course counter variable
    }

    /**
     * Finds the course AFTER the exam's course. This is called when the exam is completed, and
     * the next course is unlocked. Assumes the courses are already parsed.
     *
     * @param examId The id of the completed exam.
     * @return The next course.
     */
    @Nullable
    public static Course findNextCourse(int examId) {
        for(int i=0; i<CoursesActivity.getParsedCourses().size(); i++) {
            Course course = CoursesActivity.getParsedCourses().get(i);
            if(course.getExam().getId() == examId) { //found CURRENT course
                try {
                    return CoursesActivity.getParsedCourses().get(i+1); //if there is more
                } catch (IndexOutOfBoundsException e) {
                    return null; //no more course
                }
            }
        }
        return null; //should not get here
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

    public boolean isFinished() {
        return finished;
    }
}
