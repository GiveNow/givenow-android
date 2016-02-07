package io.givenow.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copied from com.parse.ParseNotificationManager; it is not public
 */
public class GiveNowNotificationManager {
    public final String TAG = getClass().getSimpleName();
    private final AtomicInteger notificationCount = new AtomicInteger(0);
    private volatile boolean shouldShowNotifications = true;

    public static GiveNowNotificationManager getInstance() {
        return Singleton.INSTANCE;
    }

    public void setShouldShowNotifications(boolean show) {
        shouldShowNotifications = show;
    }

    public int getNotificationCount() {
        return notificationCount.get();
    }

    public void showNotification(Context context, Notification notification) {
        if (context != null && notification != null) {
            notificationCount.incrementAndGet();

            if (shouldShowNotifications) {
                // Fire off the notification
                NotificationManager nm =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // Pick an id that probably won't overlap anything
                int notificationId = (int) System.currentTimeMillis();

                try {
                    nm.notify(notificationId, notification);
                } catch (SecurityException e) {
                    // Some phones throw an exception for unapproved vibration
                    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                    nm.notify(notificationId, notification);
                }
            }
        }
    }

    public static class Singleton {
        private static final GiveNowNotificationManager INSTANCE = new GiveNowNotificationManager();
    }
}
