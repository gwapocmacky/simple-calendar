package com.example.simplecalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;

public class MainActivity extends Activity {
    private EventStore store;
    private Prefs prefs;
    private NotificationHelper notif;
    private MonthView monthView;
    private LinearLayout summaryPanel;
    private TextView tvMonthTotal, tvHalfTotals;
    private FrameLayout content;
    private Button fab;
    private TextView headerTitle;
    private Button btnPrev, btnNext;
    private CalendarDate currentMonth;
    private CalendarDate selectedDay;

    private int appColorHex = 0xff6366f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        store = new EventStore(this);
        prefs = new Prefs(this);
        notif = new NotificationHelper(this);

        currentMonth = CalendarDate.today();
        selectedDay = CalendarDate.today();

        SharedPreferences sp = getSharedPreferences("simple_calendar_prefs", MODE_PRIVATE);
        appColorHex = sp.getInt("app_color_hex", 0xff6366f1);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackgroundColor(UiUtils.getColor(this, R.color.background));

        // top bar
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setPadding(12, 12, 12, 12);
        top.setBackgroundColor(appColorHex);
        top.setElevation(UiUtils.dpToPx(this, 4));

        btnPrev = new Button(this);
        btnPrev.setText("<");
        btnPrev.setAllCaps(false);
        btnPrev.setTextColor(0xffffffff);
        btnPrev.setBackgroundColor(appColorHex);
        btnPrev.setTextSize(18);
        top.addView(btnPrev);

        headerTitle = new TextView(this);
        headerTitle.setTextSize(20);
        headerTitle.setTextColor(0xffffffff);
        headerTitle.setGravity(Gravity.CENTER);
        headerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tl.gravity = Gravity.CENTER;
        headerTitle.setLayoutParams(tl);
        // Double tap on month name
        headerTitle.setOnClickListener(new View.OnClickListener() {
            private long lastTap = 0;
            @Override public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastTap < 400) {
                    showThemeSettings();
                }
                lastTap = now;
            }
        });
        top.addView(headerTitle);

        btnNext = new Button(this);
        btnNext.setText(">");
        btnNext.setAllCaps(false);
        btnNext.setTextColor(0xffffffff);
        btnNext.setBackgroundColor(appColorHex);
        btnNext.setTextSize(18);
        top.addView(btnNext);

        root.addView(top);

        // content area
        content = new FrameLayout(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(content);

        // month view + summary panel
        LinearLayout monthStack = new LinearLayout(this);
        monthStack.setOrientation(LinearLayout.VERTICAL);
        monthStack.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        monthStack.setPadding(UiUtils.dpToPx(this, 8), UiUtils.dpToPx(this, 4),
            UiUtils.dpToPx(this, 8), 0);

        monthView = new MonthView(this);
        monthView.setMonth(currentMonth);
        monthView.setSelected(selectedDay);
        monthView.setToday(CalendarDate.today());
        monthView.setClickListener(new MonthView.OnDayClickListener() {
            @Override public void onDayClick(CalendarDate day) {
                selectedDay = day;
                monthView.setSelected(day);
            }
        });
        monthView.setLongClickListener(new MonthView.OnDayLongClickListener() {
            @Override public void onDayLongClick(CalendarDate day) { deleteDay(day); }
        });
        monthView.setDoubleTapListener(new MonthView.OnDateDoubleTapListener() {
            @Override public void onDateDoubleTap(CalendarDate day) { copyLastIncomeToDay(day); }
        });
        monthStack.addView(monthView);

        // summary panel
        summaryPanel = new LinearLayout(this);
        summaryPanel.setOrientation(LinearLayout.VERTICAL);
        summaryPanel.setPadding(UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 16),
            UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 16));
        int radius = UiUtils.dpToPx(this, 16);
        summaryPanel.setBackground(UiUtils.roundedBg(UiUtils.getColor(this, R.color.surface), radius));
        summaryPanel.setElevation(UiUtils.dpToPx(this, 2));
        LinearLayout.LayoutParams splp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        splp.topMargin = UiUtils.dpToPx(this, 8);
        summaryPanel.setLayoutParams(splp);

        tvMonthTotal = new TextView(this);
        tvMonthTotal.setTextSize(20);
        tvMonthTotal.setTextColor(UiUtils.getColor(this, R.color.foreground));
        tvMonthTotal.setTypeface(null, android.graphics.Typeface.BOLD);
        summaryPanel.addView(tvMonthTotal);

        tvHalfTotals = new TextView(this);
        tvHalfTotals.setTextSize(14);
        tvHalfTotals.setTextColor(UiUtils.getColor(this, R.color.muted));
        tvHalfTotals.setLineSpacing(UiUtils.dpToPx(this, 4), 1f);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = UiUtils.dpToPx(this, 6);
        tvHalfTotals.setLayoutParams(hlp);
        summaryPanel.addView(tvHalfTotals);

        monthStack.addView(summaryPanel);
        content.addView(monthStack);

        // FAB
        fab = new Button(this);
        fab.setText("+");
        fab.setTextSize(28);
        fab.setAllCaps(false);
        fab.setTextColor(0xffffffff);
        fab.setTypeface(null, android.graphics.Typeface.BOLD);
        GradientDrawable fabBg = new GradientDrawable();
        fabBg.setShape(GradientDrawable.OVAL);
        fabBg.setColor(appColorHex);
        fab.setBackground(fabBg);
        fab.setElevation(UiUtils.dpToPx(this, 6));
        int fabSize = UiUtils.dpToPx(this, 56);
        int fabMargin = UiUtils.dpToPx(this, 20);
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(fabSize, fabSize);
        flp.gravity = Gravity.BOTTOM | Gravity.END;
        flp.setMargins(0, 0, fabMargin, fabMargin);
        fab.setLayoutParams(flp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openEdit(null, selectedDay); }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                new SecretSettingsPanel(MainActivity.this, prefs).show();
                return true;
            }
        });
        content.addView(fab);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { navigateMonth(-1); }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { navigateMonth(1); }
        });

        setContentView(root);
        updateHeader();
        refreshMonth();

        // Toast on app open (every time)
        Toast.makeText(this, "{MRP}", Toast.LENGTH_SHORT).show();
    }

    private void showThemeSettings() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 16, 24, 16);

        TextView title = new TextView(this);
        title.setText("Theme Settings");
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        CheckBox cbDark = new CheckBox(this);
        cbDark.setText("Dark Mode");
        cbDark.setChecked(prefs.darkMode());
        root.addView(cbDark);

        TextView colorLabel = new TextView(this);
        colorLabel.setText("App Color (Color Wheel)");
        colorLabel.setPadding(0, 8, 0, 4);
        root.addView(colorLabel);

        ColorWheelView colorWheel = new ColorWheelView(this);
        LinearLayout.LayoutParams cwp = new LinearLayout.LayoutParams(200, 200);
        cwp.gravity = Gravity.CENTER_HORIZONTAL;
        colorWheel.setLayoutParams(cwp);
        final int[] picked = new int[]{appColorHex};
        colorWheel.setOnColorSelectedListener(new ColorWheelView.OnColorSelectedListener() {
            @Override public void onColorSelected(int color) {
                picked[0] = color;
            }
        });
        root.addView(colorWheel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(root);
        builder.setPositiveButton("Apply", new android.content.DialogInterface.OnClickListener() {
            @Override public void onClick(android.content.DialogInterface d, int w) {
                prefs.setDarkMode(cbDark.isChecked());
                getSharedPreferences("simple_calendar_prefs", MODE_PRIVATE)
                    .edit().putInt("app_color_hex", picked[0]).apply();
                recreate();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void navigateMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        monthView.setMonth(currentMonth);
        refreshMonth();
        updateHeader();
    }

    private void refreshMonth() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(currentMonth.year, currentMonth.month, 1);
        int max = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        long start = currentMonth.startOfDayMillis();
        cal.set(currentMonth.year, currentMonth.month, max, 23, 59, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();
        Map<CalendarDate, Double> totals = store.getTotalsByDay(start, end);
        monthView.setTotalsByDay(totals);

        double monthTotal = 0;
        double firstHalf = 0;
        double secondHalf = 0;
        for (Map.Entry<CalendarDate, Double> e : totals.entrySet()) {
            monthTotal += e.getValue();
            if (e.getKey().day <= 15) firstHalf += e.getValue();
            else secondHalf += e.getValue();
        }

        tvMonthTotal.setText("Month Total:  +" + formatMoney(monthTotal));
        tvHalfTotals.setText("First Half (1\u201315):  +" + formatMoney(firstHalf)
            + "\nSecond Half (16\u2013" + max + "):  +" + formatMoney(secondHalf));
    }

    private void deleteDay(CalendarDate d) {
        store.deleteForDay(d);
        refreshMonth();
    }

    private void copyLastIncomeToDay(CalendarDate day) {
        if (!EventStore.hasLastIncome) return;
        Event existing = store.getForDay(day);
        if (existing != null) return;
        double reg = EventStore.lastRegular, ot = EventStore.lastOvertime,
            dp = EventStore.lastDoublePay, hol = EventStore.lastHoliday, ex = EventStore.lastExtra;
        Event e = new Event();
        e.title = day.fullLabel();
        e.description = null;
        e.startMs = day.startOfDayMillis();
        e.endMs = day.endOfDayMillis();
        e.allDay = true;
        e.color = UiUtils.getColor(this, R.color.income);
        e.reminderMin = 0;
        e.regular = reg; e.overtime = ot; e.doublePay = dp; e.holiday = hol; e.extra = ex;
        e.total = reg + ot + dp + hol + ex;
        store.insert(e);
        refreshMonth();
    }

    private void openEdit(Event event, CalendarDate day) {
        new IncomeEntryDialog(this, store, prefs, new Runnable() {
            @Override public void run() { refreshMonth(); }
        }).show(event, day);
    }

    private static String formatMoney(double v) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.##");
        return f.format(v);
    }

    private void updateHeader() {
        headerTitle.setText(currentMonth.monthLabel());
    }
}
