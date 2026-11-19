package com.elegantcal.app;

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

    public static CalendarDate fromDateStr(String str) {
        try {
            String[] parts = str.split("-");
            return new CalendarDate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return today();
        }
    }

    public String toDateStr() {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
    }

    public String toYearMonthPrefix() {
        return String.format(Locale.US, "%04d-%02d", year, month + 1);
    }

    public long startOfDayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public CalendarDate plusMonths(int delta) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        c.add(Calendar.MONTH, delta);
        return new CalendarDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
    }

    public int daysInMonth() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int firstDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return c.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday, 6 = Saturday
    }

    public String monthLabel() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(c.getTime());
    }

    public String fullLabel() {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(c.getTime());
    }

    public boolean isSameDay(CalendarDate o) {
        return o != null && year == o.year && month == o.month && day == o.day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarDate)) return false;
        CalendarDate that = (CalendarDate) o;
        return year == that.year && month == that.month && day == that.day;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * year + month) + day;
    }
}
