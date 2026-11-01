package com.example.simplecalendar;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "simple_calendar_prefs";
    private static final int DEFAULT_REMINDER = 10;
    private static final boolean DEFAULT_WEEK_START_SUNDAY = true;

    private final SharedPreferences prefs;

    public Prefs(Context context) {
        prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public int reminderDefault() {
        return prefs.getInt("reminder_default", DEFAULT_REMINDER);
    }

    public void setReminderDefault(int minutes) {
        prefs.edit().putInt("reminder_default", minutes).apply();
    }

    public boolean weekStartsSunday() {
        return prefs.getBoolean("week_starts_sunday", DEFAULT_WEEK_START_SUNDAY);
    }

    public void setWeekStartsSunday(boolean v) {
        prefs.edit().putBoolean("week_starts_sunday", v).apply();
    }

    public boolean darkMode() {
        return prefs.getBoolean("dark_mode", false);
    }

    public void setDarkMode(boolean v) {
        prefs.edit().putBoolean("dark_mode", v).apply();
    }

    public int appColor() {
        return prefs.getInt("app_color_hex", 0xff6366f1);
    }

    public void setAppColor(int v) {
        prefs.edit().putInt("app_color", v).apply();
    }

    public int defaultOvertimeMult() {
        return prefs.getInt("default_ot_mult", 0);
    }

    public void setDefaultOvertimeMult(int v) {
        prefs.edit().putInt("default_ot_mult", v).apply();
    }

    public boolean includeRegular() {
        return prefs.getBoolean("include_regular", true);
    }

    public void setIncludeRegular(boolean v) {
        prefs.edit().putBoolean("include_regular", v).apply();
    }

    public boolean includeOvertime() {
        return prefs.getBoolean("include_overtime", true);
    }

    public void setIncludeOvertime(boolean v) {
        prefs.edit().putBoolean("include_overtime", v).apply();
    }

    public boolean includeHoliday() {
        return prefs.getBoolean("include_holiday", true);
    }

    public void setIncludeHoliday(boolean v) {
        prefs.edit().putBoolean("include_holiday", v).apply();
    }

    public boolean includeDoublePay() {
        return prefs.getBoolean("include_double_pay", true);
    }

    public void setIncludeDoublePay(boolean v) {
        prefs.edit().putBoolean("include_double_pay", v).apply();
    }

    public boolean includeExtra() {
        return prefs.getBoolean("include_extra", true);
    }

    public void setIncludeExtra(boolean v) {
        prefs.edit().putBoolean("include_extra", v).apply();
    }

    public double overtimeMultiplier() {
        return Double.parseDouble(prefs.getString("overtime_mult", "1.25"));
    }

    public void setOvertimeMultiplier(double v) {
        prefs.edit().putString("overtime_mult", String.valueOf(v)).apply();
    }

    public double holidayMultiplier() {
        return Double.parseDouble(prefs.getString("holiday_mult", "1.30"));
    }

    public void setHolidayMultiplier(double v) {
        prefs.edit().putString("holiday_mult", String.valueOf(v)).apply();
    }

    public double doublePayMultiplier() {
        return Double.parseDouble(prefs.getString("double_pay_mult", "2.00"));
    }

    public void setDoublePayMultiplier(double v) {
        prefs.edit().putString("double_pay_mult", String.valueOf(v)).apply();
    }
}
