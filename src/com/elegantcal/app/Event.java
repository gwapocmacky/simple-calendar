package com.elegantcal.app;

public class Event {
    public static final String TYPE_EVENT = "EVENT";
    public static final String TYPE_INCOME = "INCOME";

    public long id;
    public String dateStr; // yyyy-MM-dd
    public String type;    // TYPE_EVENT or TYPE_INCOME

    // Event fields
    public String title;
    public String time;
    public String description;
    public String color;   // e.g. "#7c6af7"

    // Income fields
    public double regular;
    public double overtime;
    public double doublePay;
    public double holiday;
    public double extra;
    public double total;

    public Event() {
        this.type = TYPE_EVENT;
        this.color = "#7c6af7";
    }

    public static Event createEvent(String dateStr, String title, String time, String desc, String color) {
        Event e = new Event();
        e.dateStr = dateStr;
        e.type = TYPE_EVENT;
        e.title = title;
        e.time = time;
        e.description = desc;
        e.color = (color != null && !color.isEmpty()) ? color : "#7c6af7";
        return e;
    }

    public static Event createIncome(String dateStr, double regular, double overtime, double doublePay, double holiday, double extra) {
        Event e = new Event();
        e.dateStr = dateStr;
        e.type = TYPE_INCOME;
        e.regular = regular;
        e.overtime = overtime;
        e.doublePay = doublePay;
        e.holiday = holiday;
        e.extra = extra;
        e.total = regular + overtime + doublePay + holiday + extra;
        e.color = "#10b981";
        return e;
    }
}
