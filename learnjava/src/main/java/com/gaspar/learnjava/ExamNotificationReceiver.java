package com.gaspar.learnjava;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.UiThread;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.gaspar.learnjava.curriculum.Course;
import com.gaspar.learnjava.curriculum.Exam;
import com.gaspar.learnjava.database.ExamStatus;
import com.gaspar.learnjava.database.LearnJavaDatabase;
import com.gaspar.learnjava.parsers.CourseParser;

import java.util.concurrent.Executors;

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
    private static final String NOTIFICATION_CHANNEL_ID = String.valueOf(19970828);

    /**
     * Name of the string passed with the intent.
     */
    public static final String PASSED_EXAM_NAME = "passed_exam_name";

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        String examName = passedIntent.getStringExtra(PASSED_EXAM_NAME);
        if(examName == null) { //maybe not passed
            Log.d("LearnJava", "No exam name string passed with intent!");
            examName = "";
        }
        Intent intent = new Intent(context, ExamsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.test_icon_round))
                .setSmallIcon(R.drawable.exam_icon)
                .setContentTitle(context.getString(R.string.notification_exam_ready))
                .setContentText(context.getString(R.string.notification_exam_name_message, examName))
                .setVibrate(VIBRATE_PATTERN)
                .setColorized(true)
                .setColor(ContextCompat.getColor(context, ThemeUtils.getBackgroundColor()))
                .setAutoCancel(true);
        mBuilder.setContentIntent(pi);
        mBuilder.setAutoCancel(true);
        mBuilder.setLights(ContextCompat.getColor(context, ThemeUtils.getPrimaryColor()), 500, 500);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager == null) {
            Log.d("LearnJava", "Can't post notification...");
        } else {
            mNotificationManager.notify(NOTIFICATION_REQUEST_CODE, mBuilder.build());
        }
    }

    /**
     * Registers an exam notification. Called when the user has failed the exam. This will create
     * it's own background thread, no need to call it inside one.
     *
     * @param failedExam The exam that failed. The id of this is used.
     */
    @UiThread
    static void postExamNotification(Exam failedExam, Context context) {
        if(!SettingsActivity.examNotificationsEnabled(context)) return; //do nothing if notifications are disabled
        Executors.newSingleThreadExecutor().execute(() -> {
            if(CoursesActivity.coursesNotParsed()) {
                try {
                    CoursesActivity.getParsedCourses().addAll(CourseParser.getInstance().parseCourses(context));
                } catch (Exception e) {
                    Log.e("LearnJava", "Exception", e);
                    return;
                }
            }
            String examName = "";
            for(Course course: CoursesActivity.getParsedCourses()) {
                if(course.getExam().equals(failedExam)) { //found course of the exam
                    examName = course.getCourseName();
                    break;
                }
            }
            ExamStatus examStatus = LearnJavaDatabase.getInstance(context).getExamDao().queryExamStatus(failedExam.getId());
            if(examStatus == null) { //should not happen as database is validated on start
                Log.d("LearnJava", "Database error!");
                return;
            }
            long examTime = System.currentTimeMillis() - examStatus.getLastStarted(); //time it took the user to finish exam
            long displayInMillis = Exam.EXAM_COOL_DOWN_TIME - examTime;

            Intent intent = new Intent(context, ExamNotificationReceiver.class);
            intent.putExtra(PASSED_EXAM_NAME, examName); //pass exam name
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE, intent, 0);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if(am == null) {
                Log.d("LearnJava", "No alarm service found!");
            } else {
                am.set(AlarmManager.RTC, displayInMillis, pendingIntent); //will not wake up phone
            }
        });
    }
}
