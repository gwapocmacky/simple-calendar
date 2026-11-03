package com.example.simplecalendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

public class MonthView extends LinearLayout {
    private static final long DOUBLE_TAP_DELAY = 300;

    private CalendarDate current;
    private CalendarDate selected;
    private CalendarDate today;
    private Map<CalendarDate, Double> totalsByDay = new java.util.HashMap<>();
    private OnDayClickListener clickListener;
    private OnDayLongClickListener longClickListener;
    private OnDateDoubleTapListener doubleTapListener;

    private long lastTapTime = 0;
    private CalendarDate lastTapDay = null;

    private int accent;
    private int accentDark;
    private int accentSoft;
    private int todayColor;
    private int selectedColor;
    private int foreground;
    private int foregroundSoft;
    private int muted;
    private int incomeColor;
    private int divider;
    private int cellMinH;
    private int radiusPx;

    public MonthView(Context context) { this(context, null); }
    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        applyTheme(context);
    }

    private void applyTheme(Context ctx) {
        accent = UiUtils.getColor(ctx, R.color.accent);
        accentDark = UiUtils.getColor(ctx, R.color.accent_dark);
        accentSoft = UiUtils.getColor(ctx, R.color.accent_soft);
        todayColor = UiUtils.getColor(ctx, R.color.today);
        selectedColor = UiUtils.getColor(ctx, R.color.selected_day);
        foreground = UiUtils.getColor(ctx, R.color.foreground);
        foregroundSoft = UiUtils.getColor(ctx, R.color.foreground_soft);
        muted = UiUtils.getColor(ctx, R.color.muted);
        incomeColor = UiUtils.getColor(ctx, R.color.income);
        divider = UiUtils.getColor(ctx, R.color.divider);
        cellMinH = ctx.getResources().getDimensionPixelSize(R.dimen.day_cell_min_height);
        radiusPx = UiUtils.dpToPx(ctx, 10);
    }

    public void setClickListener(OnDayClickListener l) { this.clickListener = l; }
    public void setLongClickListener(OnDayLongClickListener l) { this.longClickListener = l; }
    public void setDoubleTapListener(OnDateDoubleTapListener l) { this.doubleTapListener = l; }
    public void setMonth(CalendarDate c) { this.current = c; rebuild(); }
    public CalendarDate getMonth() { return current; }
    public void setSelected(CalendarDate d) { this.selected = d; rebuild(); }
    public void setToday(CalendarDate t) { this.today = t; rebuild(); }

    public void setTotalsByDay(Map<CalendarDate, Double> totals) {
        this.totalsByDay = totals == null ? new java.util.HashMap<>() : totals;
        rebuild();
    }

    private void rebuild() {
        removeAllViews();
        if (current == null) return;
        Context ctx = getContext();

        // weekday header
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        header.setPadding(0, 8, 0, 4);
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(ctx);
            tv.setText(weekdays[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(muted);
            tv.setTextSize(12);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            header.addView(tv);
        }
        addView(header);

        int days = current.daysInMonth();
        int first = current.firstDayOfWeek();
        int totalCells = first + days;
        int rows = (totalCells + 6) / 7;
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, cellMinH));
            for (int c = 0; c < 7; c++) {
                if (idx < first || idx >= first + days) {
                    row.addView(emptyCell(ctx));
                } else {
                    row.addView(dayCell(ctx, idx - first + 1));
                }
                // add vertical column divider between cells
                if (c < 6) {
                    View divider = new View(ctx);
                    divider.setBackgroundColor(UiUtils.getColor(ctx, R.color.divider));
                    LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
                    divider.setLayoutParams(dlp);
                    row.addView(divider);
                }
                idx++;
            }
            addView(row);
            // horizontal divider between rows
            if (r < rows - 1) {
                View hDiv = new View(ctx);
                hDiv.setBackgroundColor(UiUtils.getColor(ctx, R.color.divider));
                LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
                hDiv.setLayoutParams(hlp);
                addView(hDiv);
            }
        }
    }

    private View emptyCell(Context ctx) {
        View v = new View(ctx);
        v.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        return v;
    }

    private View dayCell(Context ctx, int day) {
        CalendarDate cd = new CalendarDate(current.year, current.month, day);
        boolean sel = selected != null && selected.isSameDay(cd);
        boolean isToday = today != null && today.isSameDay(cd);
        Double total = totalsByDay.get(cd);
        boolean hasIncome = total != null && total > 0;

        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        cell.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        cell.setPadding(6, 4, 6, 4);
        cell.setTag(cd);
        cell.setBackgroundColor(UiUtils.getColor(ctx, R.color.surface));

        // day number
        TextView num = new TextView(ctx);
        num.setText(String.valueOf(day));
        num.setGravity(Gravity.CENTER);
        num.setTextSize(15);
        num.setTextColor(isToday || sel ? accentDark : foreground);
        num.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        cell.addView(num, lp);

        // selection highlight (rounded background, no circle)
        if (sel) {
            GradientDrawable selBg = new GradientDrawable();
            selBg.setShape(GradientDrawable.RECTANGLE);
            selBg.setCornerRadius(UiUtils.dpToPx(ctx, 8));
            selBg.setColor(accentSoft);
            cell.setBackground(selBg);
            num.setTextColor(accentDark);
        } else if (isToday) {
            GradientDrawable todayBg = new GradientDrawable();
            todayBg.setShape(GradientDrawable.RECTANGLE);
            todayBg.setCornerRadius(UiUtils.dpToPx(ctx, 8));
            todayBg.setColor(todayColor);
            cell.setBackground(todayBg);
            num.setTextColor(accentDark);
        }

        // income amount
        if (hasIncome) {
            TextView amt = new TextView(ctx);
            amt.setText(formatMoney(total));
            amt.setGravity(Gravity.CENTER);
            amt.setTextSize(10);
            amt.setTextColor(incomeColor);
            amt.setTypeface(null, android.graphics.Typeface.BOLD);
            amt.setMaxLines(2);
            amt.setEllipsize(null);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            alp.topMargin = 3;
            alp.gravity = Gravity.CENTER_HORIZONTAL;
            cell.addView(amt, alp);
        }

        cell.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                long now = android.os.SystemClock.elapsedRealtime();
                if (doubleTapListener != null && lastTapDay != null
                    && lastTapDay.isSameDay(cd) && (now - lastTapTime) < DOUBLE_TAP_DELAY) {
                    doubleTapListener.onDateDoubleTap(cd);
                    lastTapTime = 0;
                    lastTapDay = null;
                } else {
                    if (clickListener != null) clickListener.onDayClick(cd);
                    setSelected(cd);
                    lastTapTime = now;
                    lastTapDay = cd;
                }
            }
        });

        cell.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onDayLongClick(cd);
                }
                return true;
            }
        });

        return cell;
    }

    private static String formatMoney(double v) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.##");
        return f.format(v);
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarDate day);
    }

    public interface OnDayLongClickListener {
        void onDayLongClick(CalendarDate day);
    }

    public interface OnDateDoubleTapListener {
        void onDateDoubleTap(CalendarDate day);
    }
}
