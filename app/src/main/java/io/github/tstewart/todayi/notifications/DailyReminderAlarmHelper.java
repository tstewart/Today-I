package io.github.tstewart.todayi.notifications;

import android.app.Activity;

import java.time.LocalTime;

/* Helper class for registering and unregistering daily reminder notifications */
public class DailyReminderAlarmHelper {

    public void registerAlarm(LocalTime time, Notification notification) {

    }

    public void unregisterAlarm() {

    }

    public class Notification {

        private final Activity mLaunchActivity;
        private final String mTitle;
        private final String mContent;

        public Notification(Activity launchActivity, String title, String content) {
            mLaunchActivity = launchActivity;
            mTitle = title;
            mContent = content;
        }
    }
}
