package com.elegantcal.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthView extends LinearLayout {
    private static final long DOUBLE_TAP_DELAY = 350;

    public interface OnDayClickListener {
        void onDayClick(CalendarDate date);
    }
    public interface OnDayLongClickListener {
        void onDayLongClick(CalendarDate date);
    }
    public interface OnDayDoubleTapListener {
        void onDayDoubleTap(CalendarDate date);
    }

    private CalendarDate currentMonth;
    private CalendarDate selectedDay;
    private CalendarDate today;

    private Map<String, List<String>> eventColorsByDate = new HashMap<String, List<String>>();
    private Map<String, Double> incomeByDate = new HashMap<String, Double>();

    private OnDayClickListener clickListener;
    private OnDayLongClickListener longClickListener;
    private OnDayDoubleTapListener doubleTapListener;

    private long lastTapTime = 0;
    private CalendarDate lastTapDay = null;

    private boolean isDark = true;
    private int accentColor = 0xff7c6af7;

    private final DecimalFormat numFormat = new DecimalFormat("#,##0.##");

    public MonthView(Context context) {
        super(context);
        init();
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    public void setDarkMode(boolean dark, int accent) {
        this.isDark = dark;
        this.accentColor = accent;
        rebuild();
    }

    public void setMonth(CalendarDate c) {
        this.currentMonth = c;
        rebuild();
    }

    public void setSelected(CalendarDate d) {
        this.selectedDay = d;
        rebuild();
    }

    public void setToday(CalendarDate t) {
        this.today = t;
        rebuild();
    }

    public void setData(Map<String, List<String>> events, Map<String, Double> income) {
        this.eventColorsByDate = (events == null) ? new HashMap<String, List<String>>() : events;
        this.incomeByDate = (income == null) ? new HashMap<String, Double>() : income;
        rebuild();
    }

    public void setClickListener(OnDayClickListener l) { this.clickListener = l; }
    public void setLongClickListener(OnDayLongClickListener l) { this.longClickListener = l; }
    public void setDoubleTapListener(OnDayDoubleTapListener l) { this.doubleTapListener = l; }

    private void rebuild() {
        removeAllViews();
        if (currentMonth == null) return;

        Context ctx = getContext();
        float density = ctx.getResources().getDisplayMetrics().density;

        int textColor = isDark ? 0xfff1f5f9 : 0xff0f172a;
        int mutedColor = isDark ? 0xff64748b : 0xff94a3b8;
        int dividerColor = isDark ? 0xff27272a : 0xffe2e8f0;
        int cellBgColor = isDark ? 0xff161920 : 0xffffffff;

        // Weekday header row
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        header.setPadding(0, (int) (4 * density), 0, (int) (8 * density));

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(ctx);
            tv.setText(days[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor((i == 0 || i == 6) ? accentColor : mutedColor);
            tv.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            header.addView(tv);
        }
        addView(header);

        // Header divider
        View hLine = new View(ctx);
        hLine.setBackgroundColor(dividerColor);
        hLine.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        addView(hLine);

        int totalDays = currentMonth.daysInMonth();
        int firstDay = currentMonth.firstDayOfWeek(); // 0 to 6
        int totalCells = firstDay + totalDays;
        int rows = (totalCells + 6) / 7;

        int cellMinHeight = (int) (58 * density);
        int idx = 0;

        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(HORIZONTAL);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, cellMinHeight));

            for (int c = 0; c < 7; c++) {
                if (idx < firstDay || idx >= firstDay + totalDays) {
                    View empty = new View(ctx);
                    empty.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
                    row.addView(empty);
                } else {
                    int dayNum = idx - firstDay + 1;
                    View cell = createDayCell(ctx, dayNum, density);
                    row.addView(cell);
                }

                // Vertical column divider
                if (c < 6) {
                    View vDiv = new View(ctx);
                    vDiv.setBackgroundColor(dividerColor);
                    vDiv.setLayoutParams(new LayoutParams(1, LayoutParams.MATCH_PARENT));
                    row.addView(vDiv);
                }
                idx++;
            }
            addView(row);

            // Horizontal row divider
            if (r < rows - 1) {
                View rDiv = new View(ctx);
                rDiv.setBackgroundColor(dividerColor);
                rDiv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
                addView(rDiv);
            }
        }
    }

    private View createDayCell(final Context ctx, final int dayNum, float density) {
        final CalendarDate date = new CalendarDate(currentMonth.year, currentMonth.month, dayNum);
        final String dateKey = date.toDateStr();

        boolean isSel = (selectedDay != null && selectedDay.isSameDay(date));
        boolean isTod = (today != null && today.isSameDay(date));

        Double income = incomeByDate.get(dateKey);
        List<String> eventColors = eventColorsByDate.get(dateKey);

        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        cell.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        cell.setPadding((int) (2 * density), (int) (4 * density), (int) (2 * density), (int) (2 * density));

        // Styling background
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(6 * density);

        if (isSel) {
            bg.setColor(isDark ? 0xff2e1065 : 0xffede9fe);
            bg.setStroke((int) (1.5f * density), accentColor);
        } else if (isTod) {
            bg.setColor(isDark ? 0xff1e1b4b : 0xffdbeafe);
            bg.setStroke((int) (1.2f * density), 0xff3b82f6);
        } else {
            bg.setColor(isDark ? 0xff161920 : 0xffffffff);
        }
        cell.setBackground(bg);

        // Day Number TextView
        TextView tvDay = new TextView(ctx);
        tvDay.setText(String.valueOf(dayNum));
        tvDay.setTextSize(14);
        tvDay.setTypeface(null, (isTod || isSel) ? Typeface.BOLD : Typeface.NORMAL);

        if (isSel) {
            tvDay.setTextColor(accentColor);
        } else if (isTod) {
            tvDay.setTextColor(0xff3b82f6);
        } else {
            tvDay.setTextColor(isDark ? 0xfff1f5f9 : 0xff0f172a);
        }
        cell.addView(tvDay);

        // Event dots row (if any)
        if (eventColors != null && !eventColors.isEmpty()) {
            LinearLayout dotRow = new LinearLayout(ctx);
            dotRow.setOrientation(HORIZONTAL);
            dotRow.setGravity(Gravity.CENTER);
            dotRow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, (int) (5 * density)));
            dotRow.setPadding(0, (int) (1 * density), 0, 0);

            int maxDots = Math.min(eventColors.size(), 4);
            for (int i = 0; i < maxDots; i++) {
                View dot = new View(ctx);
                int dotSize = (int) (4 * density);
                LayoutParams dlp = new LayoutParams(dotSize, dotSize);
                dlp.setMargins((int) (1 * density), 0, (int) (1 * density), 0);
                dot.setLayoutParams(dlp);

                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                int c = 0xff7c6af7;
                try {
                    c = Color.parseColor(eventColors.get(i));
                } catch (Exception ignored) {}
                dotBg.setColor(c);
                dot.setBackground(dotBg);

                dotRow.addView(dot);
            }
            cell.addView(dotRow);
        }

        // Income badge (if any)
        if (income != null && income > 0) {
            TextView tvIncome = new TextView(ctx);
            tvIncome.setText("+" + numFormat.format(income));
            tvIncome.setTextSize(10);
            tvIncome.setTypeface(null, Typeface.BOLD);
            tvIncome.setTextColor(isDark ? 0xff34d399 : 0xff059669);
            tvIncome.setGravity(Gravity.CENTER);
            tvIncome.setSingleLine(true);
            tvIncome.setPadding((int) (2 * density), 0, (int) (2 * density), 0);

            LayoutParams ilp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ilp.setMargins(0, (int) (1 * density), 0, 0);
            cell.addView(tvIncome, ilp);
        }

        // Click / Double tap / Long click listener
        cell.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (lastTapDay != null && lastTapDay.isSameDay(date) && (now - lastTapTime < DOUBLE_TAP_DELAY)) {
                    if (doubleTapListener != null) {
                        doubleTapListener.onDayDoubleTap(date);
                    }
                    lastTapTime = 0;
                    lastTapDay = null;
                } else {
                    lastTapTime = now;
                    lastTapDay = date;
                    if (clickListener != null) {
                        clickListener.onDayClick(date);
                    }
                }
            }
        });

        cell.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onDayLongClick(date);
                    return true;
                }
                return false;
            }
        });

        return cell;
    }
}
