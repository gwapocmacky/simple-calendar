package com.elegantcal.app;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREF_NAME = "elegant_calendar_prefs";
    private static final String KEY_DARK_MODE = "key_dark_mode";
    private static final String KEY_ACCENT_COLOR = "key_accent_color";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        this.sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return sp.getBoolean(KEY_DARK_MODE, true); // default dark
    }

    public void setDarkMode(boolean dark) {
        sp.edit().putBoolean(KEY_DARK_MODE, dark).apply();
    }

    public int getAccentColor() {
        return sp.getInt(KEY_ACCENT_COLOR, 0xff7c6af7);
    }

    public void setAccentColor(int color) {
        sp.edit().putInt(KEY_ACCENT_COLOR, color).apply();
    }

    // Multipliers
    public double overtimeMultiplier() {
        return Double.parseDouble(sp.getString("overtime_mult", "1.25"));
    }

    public void setOvertimeMultiplier(double v) {
        sp.edit().putString("overtime_mult", String.valueOf(v)).apply();
    }

    public double holidayMultiplier() {
        return Double.parseDouble(sp.getString("holiday_mult", "1.30"));
    }

    public void setHolidayMultiplier(double v) {
        sp.edit().putString("holiday_mult", String.valueOf(v)).apply();
    }

    public double doublePayMultiplier() {
        return Double.parseDouble(sp.getString("double_pay_mult", "2.00"));
    }

    public void setDoublePayMultiplier(double v) {
        sp.edit().putString("double_pay_mult", String.valueOf(v)).apply();
    }

    // Component includes in total
    public boolean includeRegular() {
        return sp.getBoolean("include_regular", true);
    }

    public void setIncludeRegular(boolean v) {
        sp.edit().putBoolean("include_regular", v).apply();
    }

    public boolean includeOvertime() {
        return sp.getBoolean("include_overtime", true);
    }

    public void setIncludeOvertime(boolean v) {
        sp.edit().putBoolean("include_overtime", v).apply();
    }

    public boolean includeHoliday() {
        return sp.getBoolean("include_holiday", true);
    }

    public void setIncludeHoliday(boolean v) {
        sp.edit().putBoolean("include_holiday", v).apply();
    }

    public boolean includeDoublePay() {
        return sp.getBoolean("include_double_pay", true);
    }

    public void setIncludeDoublePay(boolean v) {
        sp.edit().putBoolean("include_double_pay", v).apply();
    }

    public boolean includeExtra() {
        return sp.getBoolean("include_extra", true);
    }

    public void setIncludeExtra(boolean v) {
        sp.edit().putBoolean("include_extra", v).apply();
    }
}
