package com.example.simplecalendar;

public class Event {
    public long id;
    public String title;
    public String description;
    public long startMs;
    public long endMs;
    public boolean allDay;
    public int color;
    public int reminderMin;
    // income fields (stored as REAL in SQLite)
    public double regular;
    public double overtime;
    public double doublePay;
    public double holiday;
    public double extra;
    public double total;

    public Event() {}

    public Event(long id, String title, String description, long startMs, long endMs,
                 boolean allDay, int color, int reminderMin,
                 double regular, double overtime, double doublePay, double holiday, double extra, double total) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startMs = startMs;
        this.endMs = endMs;
        this.allDay = allDay;
        this.color = color;
        this.reminderMin = reminderMin;
        this.regular = regular;
        this.overtime = overtime;
        this.doublePay = doublePay;
        this.holiday = holiday;
        this.extra = extra;
        this.total = total;
    }
}
