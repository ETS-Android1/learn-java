package com.gaspar.learnjava;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.annotation.UiThread;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;
import com.gaspar.learnjava.utils.LogUtils;
import com.gaspar.learnjava.utils.ThemeUtils;

/**
 * A background service that shows a notification when it is created.
 */
public class ExamNotificationReceiver extends BroadcastReceiver {

    /**
     * Notification vibrate pattern.
     */
    private static final long[] VIBRATE_PATTERN = {0, 200, 100, 400, 100, 200};

    /**
     * Notification request code for creating pending intent and posting notification.
     */
    private static final int NOTIFICATION_REQUEST_CODE = 24315;

    /**
     * Notification channel id for creating builder object.
     */
    static final String NOTIFICATION_CHANNEL_ID = String.valueOf(19970828);

    /**
     * Name of the string passed with the intent.
     */
    public static final String PASSED_EXAM_NAME = "passed_exam_name";

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        String examName = passedIntent.getStringExtra(PASSED_EXAM_NAME);
        if(examName == null) { //maybe not passed
            LogUtils.logError( "No exam name string passed with intent!");
            examName = "";
        }
        Intent intent = new Intent(context, CoursesActivity.class); //intent of activity
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(NOTIFICATION_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.learn_java_icon_round))
                .setSmallIcon(R.drawable.exam_icon)
                .setContentTitle(context.getString(R.string.notification_exam_ready))
                .setContentText(context.getString(R.string.notification_exam_name_message, examName))
                .setVibrate(VIBRATE_PATTERN)
                .setColorized(true)
                .setColor(ContextCompat.getColor(context, ThemeUtils.getBackgroundColor()))
                .setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setLights(ContextCompat.getColor(context, ThemeUtils.getPrimaryColor()), 500, 500);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager == null) {
            LogUtils.logError("Can't post notification, system service is null!");
        } else {
            mNotificationManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build());
        }
    }

    /**
     * Registers an exam notification. Called when the user has failed the exam. This will create
     * it's own background thread, no need to call it inside one.
     * @param failedExam The exam that failed. The id of this is used.
     */
    @UiThread
    static void postExamNotification(Exam failedExam, Context context) {
        if(!SettingsActivity.examNotificationsEnabled(context)) return; //do nothing if notifications are disabled
        LearnJavaDatabase.DB_EXECUTOR.execute(() -> {
            if(CoursesActivity.coursesNotParsed()) {
                try {
                    CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance().parseCourses(context));
                } catch (Exception e) {
                    LogUtils.logError("Exception while parsing courses!", e);
                    return;
                }
            }
            String examName = "UNKNOWN"; //it will stay unknown for test exam for example
            for(Course course: CoursesActivity.getParsedCourses()) {
                if(course.getExam().equals(failedExam)) { //found course of the exam
                    examName = course.getCourseName();
                    break;
                }
            }
            ExamStatus examStatus = LearnJavaDatabase.getInstance(context).getExamDao().queryExamStatus(failedExam.getId());
            if(examStatus == null) { //should not happen as database is validated on start
                LogUtils.logError("Database error!");
                return;
            }
            long examTime = System.currentTimeMillis() - examStatus.getLastStarted(); //time it took the user to finish exam
            long displayInMillis = (1000*Exam.EXAM_COOL_DOWN_TIME) - examTime;

            Intent intent = new Intent(context, ExamNotificationReceiver.class);
            intent.putExtra(PASSED_EXAM_NAME, examName); //pass exam name
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE, intent, 0);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if(am == null) {
                LogUtils.logError("No alarm service found!");
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + displayInMillis, pendingIntent);
            }
        });
    }
}
