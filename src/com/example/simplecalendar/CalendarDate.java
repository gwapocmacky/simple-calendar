package com.example.simplecalendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarDate {
    public final int year;
    public final int month; // 0-11
    public final int day;

    public CalendarDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static CalendarDate today() {
        Calendar c = Calendar.getInstance();
        return new CalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    public static CalendarDate fromMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return new CalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    public long startOfDayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public long endOfDayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 23, 59, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    public int daysInMonth() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int firstDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return c.get(Calendar.DAY_OF_WEEK) - 1; // 0 = SUNDAY
    }

    public CalendarDate plusMonths(int n) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        c.add(Calendar.MONTH, n);
        return new CalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    public String monthLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return sdf.format(c.getTime());
    }

    public String fullLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return sdf.format(c.getTime());
    }

    public String label() {
        return String.format(Locale.getDefault(), "%02d/%04d", month + 1, year);
    }

    public String dayLabel() {
        return String.valueOf(day);
    }

    public String weekdayShort() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return sdf.format(c.getTime());
    }

    public boolean isSameDay(CalendarDate other) {
        return other != null && year == other.year && month == other.month && day == other.day;
    }

    public boolean isBefore(CalendarDate other) {
        if (year != other.year) return year < other.year;
        if (month != other.month) return month < other.month;
        return day < other.day;
    }

    public boolean isAfter(CalendarDate other) {
        if (year != other.year) return year > other.year;
        if (month != other.month) return month > other.month;
        return day > other.day;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + year;
        result = 31 * result + month;
        result = 31 * result + day;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CalendarDate)) return false;
        CalendarDate o = (CalendarDate) obj;
        return year == o.year && month == o.month && day == o.day;
    }
}
