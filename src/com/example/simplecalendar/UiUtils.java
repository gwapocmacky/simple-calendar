package com.example.simplecalendar;

import android.content.Context;
import android.app.PendingIntent;
import android.os.Build;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

public class UiUtils {
    public static boolean isDarkMode(Context context) {
        return new Prefs(context).darkMode();
    }

    public static int getColor(Context context, int resId) {
        boolean dark = isDarkMode(context);
        if (dark) {
            switch (resId) {
                case R.color.background: return 0xff0f172a;
                case R.color.surface: return 0xff1e293b;
                case R.color.surface_2: return 0xff334155;
                case R.color.foreground: return 0xfff1f5f9;
                case R.color.foreground_soft: return 0xffcbd5e1;
                case R.color.muted: return 0xff94a3b8;
                case R.color.accent: return 0xff818cf8;
                case R.color.accent_dark: return 0xff6366f1;
                case R.color.accent_soft: return 0xff312e81;
                case R.color.today: return 0xff1e3a5f;
                case R.color.selected_day: return 0xff1e40af;
                case R.color.income: return 0xff34d399;
                case R.color.income_soft: return 0xff064e3b;
                case R.color.white: return 0xffffffff;
                case R.color.divider: return 0xff334155;
                case R.color.card_shadow: return 0xff000000;
                case R.color.danger: return 0xfff87171;
            }
        }
        // Light mode or unknown
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(resId);
        }
        return context.getResources().getColor(resId);
    }

    public static int getContrastColor(int bgColor) {
        double r = Color.red(bgColor) / 255.0;
        double g = Color.green(bgColor) / 255.0;
        double b = Color.blue(bgColor) / 255.0;
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance > 0.179 ? 0xff000000 : 0xffffffff;
    }

    public static int getPendingIntentFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static GradientDrawable roundedBg(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radiusDp);
        return d;
    }

    public static GradientDrawable roundedStrokeBg(int color, int radiusDp, int strokeWidth, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radiusDp);
        d.setStroke(strokeWidth, strokeColor);
        return d;
    }
}
