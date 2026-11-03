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
    private TextView tvMonthTotal, tvMonthAmount, tvFirstHalf, tvSecondHalf,
                     tvHalf1Label, tvHalf2Label,
                     tvEmptyState, tvEmptySub, tvEmptyHint, tvFooter;
    private FrameLayout content;
    private Button fab;
    private TextView headerTitle;
    private Button btnPrev, btnNext;
    private CalendarDate currentMonth;
    private CalendarDate selectedDay;
    private int maxDays;

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

        // month total row: label left, amount right
summaryPanel = new LinearLayout(this);
summaryPanel.setOrientation(LinearLayout.VERTICAL);
summaryPanel.setPadding(UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 16),
    UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 16));
summaryPanel.setLayoutParams(new LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

LinearLayout totalRow = new LinearLayout(this);
totalRow.setOrientation(LinearLayout.HORIZONTAL);
totalRow.setPadding(UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 14),
    UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 14));

tvMonthTotal = new TextView(this);
tvMonthTotal.setText("Month total");
tvMonthTotal.setTextSize(18);
tvMonthTotal.setTextColor(UiUtils.getColor(this, R.color.foreground));
tvMonthTotal.setTypeface(null, android.graphics.Typeface.BOLD);
totalRow.addView(tvMonthTotal, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

tvMonthAmount = new TextView(this);
tvMonthAmount.setTextSize(18);
tvMonthAmount.setTextColor(UiUtils.getColor(this, R.color.foreground));
tvMonthAmount.setTypeface(null, android.graphics.Typeface.BOLD);
tvMonthAmount.setGravity(Gravity.END);
totalRow.addView(tvMonthAmount, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

summaryPanel.addView(totalRow);

// combined halves row: "First half (1–15) +693.75 · Second half (16–31) +693.75"
LinearLayout halfRow = new LinearLayout(this);
halfRow.setOrientation(LinearLayout.HORIZONTAL);
halfRow.setPadding(UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 6),
    UiUtils.dpToPx(this, 20), UiUtils.dpToPx(this, 6));
TextView tvHalf1Label = new TextView(this);
tvHalf1Label.setText("1–15");
tvHalf1Label.setTextSize(14);
tvHalf1Label.setTextColor(UiUtils.getColor(this, R.color.muted));
halfRow.addView(tvHalf1Label, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
tvFirstHalf = new TextView(this);
tvFirstHalf.setTextSize(14);
tvFirstHalf.setTextColor(UiUtils.getColor(this, R.color.muted));
tvFirstHalf.setTypeface(null, android.graphics.Typeface.BOLD);
tvFirstHalf.setGravity(Gravity.END);
halfRow.addView(tvFirstHalf, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
TextView tvDot = new TextView(this);
tvDot.setText(" · ");
tvDot.setTextSize(14);
tvDot.setTextColor(UiUtils.getColor(this, R.color.muted));
halfRow.addView(tvDot, new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
tvHalf2Label = new TextView(this);
tvHalf2Label.setText("16–" + maxDays);
tvHalf2Label.setTextSize(14);
tvHalf2Label.setTextColor(UiUtils.getColor(this, R.color.muted));
halfRow.addView(tvHalf2Label, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
tvSecondHalf = new TextView(this);
tvSecondHalf.setTextSize(14);
tvSecondHalf.setTextColor(UiUtils.getColor(this, R.color.muted));
tvSecondHalf.setTypeface(null, android.graphics.Typeface.BOLD);
tvSecondHalf.setGravity(Gravity.END);
halfRow.addView(tvSecondHalf, new LinearLayout.LayoutParams(0,
    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
summaryPanel.addView(halfRow);

// divider
View divider = new View(this);
divider.setBackgroundColor(UiUtils.getColor(this, R.color.divider));
LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT, 1);
summaryPanel.addView(divider, dlp);

// empty state
tvEmptyState = new TextView(this);
tvEmptyState.setTextSize(20);
tvEmptyState.setTextColor(UiUtils.getColor(this, R.color.foreground));
tvEmptyState.setTypeface(null, android.graphics.Typeface.BOLD);
tvEmptyState.setGravity(Gravity.CENTER_HORIZONTAL);
LinearLayout.LayoutParams esp = new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
esp.topMargin = UiUtils.dpToPx(this, 16);
esp.bottomMargin = UiUtils.dpToPx(this, 4);
tvEmptyState.setLayoutParams(esp);
summaryPanel.addView(tvEmptyState);

tvEmptySub = new TextView(this);
tvEmptySub.setText("Add regular, overtime, double pay or holiday earnings to a day.");
tvEmptySub.setTextSize(14);
tvEmptySub.setTextColor(UiUtils.getColor(this, R.color.muted));
tvEmptySub.setGravity(Gravity.CENTER_HORIZONTAL);
LinearLayout.LayoutParams esp2 = new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
esp2.bottomMargin = UiUtils.dpToPx(this, 8);
tvEmptySub.setLayoutParams(esp2);
summaryPanel.addView(tvEmptySub);

tvEmptyHint = new TextView(this);
tvEmptyHint.setText("Double-tap a date to copy the previous income entry, or tap + to add income");
tvEmptyHint.setTextSize(12);
tvEmptyHint.setTextColor(UiUtils.getColor(this, R.color.muted));
tvEmptyHint.setGravity(Gravity.CENTER_HORIZONTAL);
tvEmptyHint.setLineSpacing(UiUtils.dpToPx(this, 2), 1f);
LinearLayout.LayoutParams esp3 = new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
esp3.bottomMargin = UiUtils.dpToPx(this, 12);
tvEmptyHint.setLayoutParams(esp3);
summaryPanel.addView(tvEmptyHint);

// footer: recorded days + month + long press hint
tvFooter = new TextView(this);
tvFooter.setTextSize(14);
tvFooter.setTextColor(UiUtils.getColor(this, R.color.muted));
tvFooter.setGravity(Gravity.CENTER_HORIZONTAL);
LinearLayout.LayoutParams footerLp = new LinearLayout.LayoutParams(
    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
footerLp.bottomMargin = UiUtils.dpToPx(this, 140); // clear FAB
tvFooter.setLayoutParams(footerLp);
summaryPanel.addView(tvFooter);

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
        maxDays = max;
        tvHalf2Label.setText("16–" + maxDays);

        double monthTotal = 0;
        double firstHalf = 0;
        double secondHalf = 0;
        for (Map.Entry<CalendarDate, Double> e : totals.entrySet()) {
            monthTotal += e.getValue();
            if (e.getKey().day <= 15) firstHalf += e.getValue();
            else secondHalf += e.getValue();
        }

        tvMonthTotal.setText("Month total");
        tvMonthAmount.setText("₱" + formatMoney(monthTotal));
        tvFirstHalf.setText("+" + formatMoney(firstHalf));
        tvSecondHalf.setText("+" + formatMoney(secondHalf));

        boolean empty = monthTotal <= 0;
        tvEmptyState.setText(empty ? "Start with your first income entry" : "");
        tvEmptySub.setText(empty ? "Add regular, overtime, double pay or holiday earnings to a day." : "");
        tvEmptyHint.setText(empty ? "Double-tap a date to copy the previous income entry, or tap + to add income" : "");

        int count = 0;
        for (Event ev : store.getAll()) {
            CalendarDate d = CalendarDate.fromMillis(ev.startMs);
            if (d.year == currentMonth.year && d.month == currentMonth.month) count++;
        }

        // month name for footer
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault());
        java.util.Calendar mc = java.util.Calendar.getInstance();
        mc.set(currentMonth.year, currentMonth.month, 1);
        String monthName = sdf.format(mc.getTime());

        tvFooter.setText(count + " recorded days · " + monthName + " · Long press a date to remove its amount");
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
