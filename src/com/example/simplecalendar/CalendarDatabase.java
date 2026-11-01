package com.example.simplecalendar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CalendarDatabase extends SQLiteOpenHelper {
    private static final String NAME = "simple_calendar.db";
    private static final int VERSION = 2;

    public CalendarDatabase(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT," +
            "description TEXT," +
            "start_ms INTEGER NOT NULL," +
            "end_ms INTEGER NOT NULL," +
            "all_day INTEGER NOT NULL DEFAULT 0," +
            "color INTEGER NOT NULL," +
            "reminder_min INTEGER NOT NULL DEFAULT 10," +
            "regular REAL NOT NULL DEFAULT 0," +
            "overtime REAL NOT NULL DEFAULT 0," +
            "double_pay REAL NOT NULL DEFAULT 0," +
            "holiday REAL NOT NULL DEFAULT 0," +
            "extra REAL NOT NULL DEFAULT 0," +
            "total REAL NOT NULL DEFAULT 0" +
            ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE events ADD COLUMN regular REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE events ADD COLUMN overtime REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE events ADD COLUMN double_pay REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE events ADD COLUMN holiday REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE events ADD COLUMN extra REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE events ADD COLUMN total REAL NOT NULL DEFAULT 0");
        }
    }
}
