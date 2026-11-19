package com.elegantcal.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "calendar_elegant.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ENTRIES = "entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date_str";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DESC = "description";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_REGULAR = "regular";
    public static final String COLUMN_OVERTIME = "overtime";
    public static final String COLUMN_DOUBLE_PAY = "double_pay";
    public static final String COLUMN_HOLIDAY = "holiday";
    public static final String COLUMN_EXTRA = "extra";
    public static final String COLUMN_TOTAL = "total";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ENTRIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_TIME + " TEXT, " +
                    COLUMN_DESC + " TEXT, " +
                    COLUMN_COLOR + " TEXT, " +
                    COLUMN_REGULAR + " REAL DEFAULT 0, " +
                    COLUMN_OVERTIME + " REAL DEFAULT 0, " +
                    COLUMN_DOUBLE_PAY + " REAL DEFAULT 0, " +
                    COLUMN_HOLIDAY + " REAL DEFAULT 0, " +
                    COLUMN_EXTRA + " REAL DEFAULT 0, " +
                    COLUMN_TOTAL + " REAL DEFAULT 0" +
                    ");";

    // Remember last income for double-tap quick repeat
    public static boolean hasLastIncome = false;
    public static double lastRegular = 0;
    public static double lastOvertime = 0;
    public static double lastDoublePay = 0;
    public static double lastHoliday = 0;
    public static double lastExtra = 0;

    public CalendarDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        onCreate(db);
    }

    public long insertEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_DATE, event.dateStr);
        v.put(COLUMN_TYPE, Event.TYPE_EVENT);
        v.put(COLUMN_TITLE, event.title);
        v.put(COLUMN_TIME, event.time);
        v.put(COLUMN_DESC, event.description);
        v.put(COLUMN_COLOR, event.color);
        return db.insert(TABLE_ENTRIES, null, v);
    }

    public long upsertIncome(Event income) {
        SQLiteDatabase db = getWritableDatabase();
        // Remove existing income for this day if any
        db.delete(TABLE_ENTRIES, COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ?",
                new String[]{income.dateStr, Event.TYPE_INCOME});

        ContentValues v = new ContentValues();
        v.put(COLUMN_DATE, income.dateStr);
        v.put(COLUMN_TYPE, Event.TYPE_INCOME);
        v.put(COLUMN_REGULAR, income.regular);
        v.put(COLUMN_OVERTIME, income.overtime);
        v.put(COLUMN_DOUBLE_PAY, income.doublePay);
        v.put(COLUMN_HOLIDAY, income.holiday);
        v.put(COLUMN_EXTRA, income.extra);
        v.put(COLUMN_TOTAL, income.total);
        v.put(COLUMN_COLOR, income.color);

        lastRegular = income.regular;
        lastOvertime = income.overtime;
        lastDoublePay = income.doublePay;
        lastHoliday = income.holiday;
        lastExtra = income.extra;
        hasLastIncome = true;

        return db.insert(TABLE_ENTRIES, null, v);
    }

    public List<Event> getEventsForDate(String dateStr) {
        List<Event> list = new ArrayList<Event>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ENTRIES, null,
                COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ?",
                new String[]{dateStr, Event.TYPE_EVENT},
                null, null, COLUMN_TIME + " ASC, " + COLUMN_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                Event e = new Event();
                e.id = c.getLong(c.getColumnIndexOrThrow(COLUMN_ID));
                e.dateStr = c.getString(c.getColumnIndexOrThrow(COLUMN_DATE));
                e.type = Event.TYPE_EVENT;
                e.title = c.getString(c.getColumnIndexOrThrow(COLUMN_TITLE));
                e.time = c.getString(c.getColumnIndexOrThrow(COLUMN_TIME));
                e.description = c.getString(c.getColumnIndexOrThrow(COLUMN_DESC));
                e.color = c.getString(c.getColumnIndexOrThrow(COLUMN_COLOR));
                list.add(e);
            }
            c.close();
        }
        return list;
    }

    public Event getIncomeForDate(String dateStr) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ENTRIES, null,
                COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ?",
                new String[]{dateStr, Event.TYPE_INCOME},
                null, null, null, "1");
        Event e = null;
        if (c != null) {
            if (c.moveToFirst()) {
                e = new Event();
                e.id = c.getLong(c.getColumnIndexOrThrow(COLUMN_ID));
                e.dateStr = c.getString(c.getColumnIndexOrThrow(COLUMN_DATE));
                e.type = Event.TYPE_INCOME;
                e.regular = c.getDouble(c.getColumnIndexOrThrow(COLUMN_REGULAR));
                e.overtime = c.getDouble(c.getColumnIndexOrThrow(COLUMN_OVERTIME));
                e.doublePay = c.getDouble(c.getColumnIndexOrThrow(COLUMN_DOUBLE_PAY));
                e.holiday = c.getDouble(c.getColumnIndexOrThrow(COLUMN_HOLIDAY));
                e.extra = c.getDouble(c.getColumnIndexOrThrow(COLUMN_EXTRA));
                e.total = c.getDouble(c.getColumnIndexOrThrow(COLUMN_TOTAL));
                e.color = c.getString(c.getColumnIndexOrThrow(COLUMN_COLOR));
            }
            c.close();
        }
        return e;
    }

    public Map<String, List<String>> getEventColorsForMonth(String yearMonthPrefix) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ENTRIES,
                new String[]{COLUMN_DATE, COLUMN_COLOR},
                COLUMN_DATE + " LIKE ? AND " + COLUMN_TYPE + " = ?",
                new String[]{yearMonthPrefix + "%", Event.TYPE_EVENT},
                null, null, COLUMN_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                String d = c.getString(0);
                String col = c.getString(1);
                List<String> list = map.get(d);
                if (list == null) {
                    list = new ArrayList<String>();
                    map.put(d, list);
                }
                list.add(col);
            }
            c.close();
        }
        return map;
    }

    public Map<String, Double> getIncomeTotalsForMonth(String yearMonthPrefix) {
        Map<String, Double> map = new HashMap<String, Double>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ENTRIES,
                new String[]{COLUMN_DATE, COLUMN_TOTAL},
                COLUMN_DATE + " LIKE ? AND " + COLUMN_TYPE + " = ?",
                new String[]{yearMonthPrefix + "%", Event.TYPE_INCOME},
                null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getDouble(1));
            }
            c.close();
        }
        return map;
    }

    public boolean deleteEvent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteIncomeForDate(String dateStr) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ENTRIES, COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ?",
                new String[]{dateStr, Event.TYPE_INCOME}) > 0;
    }

    public boolean clearAllForDate(String dateStr) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ENTRIES, COLUMN_DATE + " = ?", new String[]{dateStr}) > 0;
    }
}
