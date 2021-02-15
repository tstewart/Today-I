package io.github.tstewart.todayi.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.github.tstewart.todayi.R;

/* Sends notifications to the user, with title and content as defined in the function */
public class NotificationSender {

    /* Default notification channel, for daily reminder notifications
    * The application initialises this channel on startup, and since there are no other notifications
    * in the application, this channel is enforced. */
    public static final String DAILY_REMINDERS_CHANNEL_ID = "daily-reminders";

    /* Application context */
    private Context mContext;

    public NotificationSender(Context context) {
        mContext = context;
    }

    /**
     * Send notification to user
     * @param launchIntent Action to be called on click
     * @param showWhenRunning Should notification be shown when application is currently open
     * @param title Title text for notification
     * @param content Content text for notification
     */
    public void sendNotification(PendingIntent launchIntent, boolean showWhenRunning, String title, String content) {
        /* If should show when running, or if the app is in background
        * Show notification */
        if(showWhenRunning || !isAppInForeground()) {
            Uri soundUri = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notification = new NotificationCompat.Builder(getContext(), DAILY_REMINDERS_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(launchIntent)
                    .setSound(soundUri).setSmallIcon(R.drawable.notification_logo)
                    .build();

            /* Cancel notification on click */
            notification.flags += Notification.FLAG_AUTO_CANCEL;

            NotificationManagerCompat.from(getContext()).notify(0, notification);
        }
        else
            Log.i(NotificationSender.class.getSimpleName(), "Notification was not sent as the application is currently in the foreground.");
    }

    /**
     * Check if application is currently running and in foreground (i.e. actively being used)
     * @return True if application is in foreground
     */
    public boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);

        /* If the activity manager couldn't be found for any reason, default to assuming the app is in the background */
        if(activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            for(ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                /* If the current process is in the foreground, check if it is this app */
                if(processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for(String activeProcess : processInfo.pkgList) {
                        /* If the active process is this app, the app is not in the background */
                        if(activeProcess.equals(getContext().getPackageName())) {
                            return true;
                        }
                    }
                }
            }
        }
        /* App was not found in the foreground */
        return false;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

}
