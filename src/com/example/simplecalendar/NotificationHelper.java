package com.example.simplecalendar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {
    private static final String CHANNEL_ID = "simple_calendar_reminders";
    private final Context context;
    private final NotificationManager nm;

    public NotificationHelper(Context context) {
        this.context = context;
        this.nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannelIfNeeded();
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription(context.getString(R.string.channel_desc));
                ch.enableVibration(true);
                nm.createNotificationChannel(ch);
            }
        }
    }

    public void notify(int id, Notification notification) {
        nm.notify(id, notification);
    }

    public void cancel(int id) {
        nm.cancel(id);
    }

    public Notification.Builder newBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(context, CHANNEL_ID);
        }
        return new Notification.Builder(context);
    }
}
