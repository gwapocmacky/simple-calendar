package com.example.simplecalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String ACTION_REMINDER = "com.example.simplecalendar.ACTION_REMINDER";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private static final String EXTRA_EVENT_ID = "event_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_BOOT.equals(action)) {
            rescheduleAll(context);
            return;
        }
        if (ACTION_REMINDER.equals(action)) {
            long eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
            showReminder(context, eventId);
            return;
        }
    }

    public static void schedule(Context context, Event event) {
        if (event.id <= 0 || event.reminderMin <= 0) return;
        long fireAt = event.startMs - (long) event.reminderMin * 60 * 1000;
        if (fireAt <= System.currentTimeMillis()) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);
        intent.putExtra(EXTRA_EVENT_ID, event.id);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) event.id, intent,
            UiUtils.getPendingIntentFlags());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, fireAt, pi);
        }
    }

    public static void cancel(Context context, long eventId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) eventId, intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            am.cancel(pi);
        }
    }

    public static void rescheduleAll(Context context) {
        EventStore store = new EventStore(context);
        for (Event e : store.getAll()) {
            schedule(context, e);
        }
    }

    private void showReminder(Context context, long eventId) {
        EventStore store = new EventStore(context);
        Event e = store.get(eventId);
        if (e == null) return;
        NotificationHelper helper = new NotificationHelper(context);
        android.app.Notification.Builder b = helper.newBuilder()
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Calendar reminder")
            .setContentText(e.title)
            .setAutoCancel(true);
        if (e.description != null && !e.description.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                b.setSubText(e.description);
            }
        }
        helper.notify((int) e.id, b.build());
    }
}
