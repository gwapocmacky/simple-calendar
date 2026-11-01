package com.example.simplecalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventStore {
    private final CalendarDatabase dbHelper;

    // last saved income, used by double-tap to copy the same amounts to any date
    public static double lastRegular, lastOvertime, lastDoublePay, lastHoliday, lastExtra;
    public static boolean hasLastIncome = false;

    public EventStore(Context context) {
        this.dbHelper = new CalendarDatabase(context);
    }

    public long insert(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", event.title);
        cv.put("description", event.description);
        cv.put("start_ms", event.startMs);
        cv.put("end_ms", event.endMs);
        cv.put("all_day", event.allDay ? 1 : 0);
        cv.put("color", event.color);
        cv.put("reminder_min", event.reminderMin);
        cv.put("regular", event.regular);
        cv.put("overtime", event.overtime);
        cv.put("double_pay", event.doublePay);
        cv.put("holiday", event.holiday);
        cv.put("extra", event.extra);
        cv.put("total", event.total);
        long id = db.insert("events", null, cv);
        db.close();
        rememberLast(event);
        return id;
    }

    public int update(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", event.title);
        cv.put("description", event.description);
        cv.put("start_ms", event.startMs);
        cv.put("end_ms", event.endMs);
        cv.put("all_day", event.allDay ? 1 : 0);
        cv.put("color", event.color);
        cv.put("reminder_min", event.reminderMin);
        cv.put("regular", event.regular);
        cv.put("overtime", event.overtime);
        cv.put("double_pay", event.doublePay);
        cv.put("holiday", event.holiday);
        cv.put("extra", event.extra);
        cv.put("total", event.total);
        int rows = db.update("events", cv, "id=?", new String[]{String.valueOf(event.id)});
        db.close();
        rememberLast(event);
        return rows;
    }

    private void rememberLast(Event event) {
        lastRegular = event.regular;
        lastOvertime = event.overtime;
        lastDoublePay = event.doublePay;
        lastHoliday = event.holiday;
        lastExtra = event.extra;
        hasLastIncome = true;
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("events", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteForDay(CalendarDate day) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long start = day.startOfDayMillis();
        long end = day.endOfDayMillis();
        int rows = db.delete("events", "start_ms >= ? AND end_ms <= ?",
            new String[]{String.valueOf(start), String.valueOf(end)});
        db.close();
        return rows;
    }

    public Event get(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("events", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) return read(c);
            return null;
        } finally {
            c.close();
            db.close();
        }
    }

    public Event getForDay(CalendarDate day) {
        long start = day.startOfDayMillis();
        long end = day.endOfDayMillis();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("events", null, "start_ms >= ? AND end_ms <= ?",
            new String[]{String.valueOf(start), String.valueOf(end)}, null, null, "start_ms ASC");
        try {
            if (c.moveToFirst()) return read(c);
            return null;
        } finally {
            c.close();
            db.close();
        }
    }

    public List<Event> getForDayList(CalendarDate day) {
        long start = day.startOfDayMillis();
        long end = day.endOfDayMillis();
        return query("start_ms >= ? AND end_ms <= ?",
            new String[]{String.valueOf(start), String.valueOf(end)}, "start_ms ASC");
    }

    public List<Event> getInRange(long startMs, long endMs) {
        return query("start_ms < ? AND end_ms > ?",
            new String[]{String.valueOf(endMs), String.valueOf(startMs)}, "start_ms ASC");
    }

    public Map<CalendarDate, Double> getTotalsByDay(long startMs, long endMs) {
        Map<CalendarDate, Double> totals = new HashMap<>();
        List<Event> events = getInRange(startMs, endMs);
        for (Event e : events) {
            CalendarDate d = CalendarDate.fromMillis(e.startMs);
            Double cur = totals.get(d);
            totals.put(d, (cur == null ? 0.0 : cur) + e.total);
        }
        return totals;
    }

    public List<Event> getAll() {
        return query(null, null, "start_ms ASC");
    }

    private List<Event> query(String where, String[] args, String orderBy) {
        List<Event> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("events", null, where, args, null, null, orderBy);
        try {
            while (c.moveToNext()) list.add(read(c));
        } finally {
            c.close();
            db.close();
        }
        return list;
    }

    private Event read(Cursor c) {
        Event e = new Event();
        e.id = c.getLong(c.getColumnIndex("id"));
        int ti = c.getColumnIndex("title");
        e.title = c.isNull(ti) ? null : c.getString(ti);
        int di = c.getColumnIndex("description");
        e.description = c.isNull(di) ? null : c.getString(di);
        e.startMs = c.getLong(c.getColumnIndex("start_ms"));
        e.endMs = c.getLong(c.getColumnIndex("end_ms"));
        e.allDay = c.getInt(c.getColumnIndex("all_day")) == 1;
        e.color = c.getInt(c.getColumnIndex("color"));
        e.reminderMin = c.getInt(c.getColumnIndex("reminder_min"));
        e.regular = c.getDouble(c.getColumnIndex("regular"));
        e.overtime = c.getDouble(c.getColumnIndex("overtime"));
        e.doublePay = c.getDouble(c.getColumnIndex("double_pay"));
        e.holiday = c.getDouble(c.getColumnIndex("holiday"));
        e.extra = c.getDouble(c.getColumnIndex("extra"));
        e.total = c.getDouble(c.getColumnIndex("total"));
        return e;
    }
}
