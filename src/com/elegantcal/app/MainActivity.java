package com.elegantcal.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private CalendarDbHelper dbHelper;
    private Prefs prefs;

    private CalendarDate currentMonth;
    private CalendarDate selectedDay;
    private CalendarDate today;

    // Views
    private FrameLayout mainRoot;
    private LinearLayout topBar;
    private TextView tvMonthHeader;
    private Button btnPrevMonth;
    private Button btnNextMonth;
    private Button btnThemeToggle;
    private Button btnToday;

    private LinearLayout cardMonthSummary;
    private TextView tvMonthTotalLabel;
    private TextView tvMonthTotalValue;
    private View dividerSummary;
    private TextView tvHalf1Label;
    private TextView tvHalf2Label;

    private LinearLayout cardCalendar;
    private FrameLayout monthViewContainer;
    private MonthView monthView;

    private TextView tvSelectedDateTitle;
    private Button btnAddEventMain;
    private Button btnAddIncomeMain;
    private LinearLayout containerDailyIncome;
    private LinearLayout containerEvents;
    private TextView tvEmptyDay;

    private Button fabAdd;

    private long lastHeaderTap = 0;

    private final DecimalFormat numFormat = new DecimalFormat("#,##0.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toast on app launch (copied from Pay Date)
        Toast.makeText(this, "{MRP}", Toast.LENGTH_SHORT).show();

        dbHelper = new CalendarDbHelper(this);
        prefs = new Prefs(this);

        today = CalendarDate.today();
        currentMonth = CalendarDate.today();
        selectedDay = CalendarDate.today();

        initViews();
        setupListeners();
        applyTheme(prefs.isDarkMode());

        renderCalendar();
        renderDayDetails();
    }

    private void initViews() {
        mainRoot = (FrameLayout) findViewById(R.id.main_root);
        topBar = (LinearLayout) findViewById(R.id.top_bar);
        tvMonthHeader = (TextView) findViewById(R.id.tv_month_header);
        btnPrevMonth = (Button) findViewById(R.id.btn_prev_month);
        btnNextMonth = (Button) findViewById(R.id.btn_next_month);
        btnThemeToggle = (Button) findViewById(R.id.btn_theme_toggle);
        btnToday = (Button) findViewById(R.id.btn_today);

        cardMonthSummary = (LinearLayout) findViewById(R.id.card_month_summary);
        tvMonthTotalLabel = (TextView) findViewById(R.id.tv_month_total_label);
        tvMonthTotalValue = (TextView) findViewById(R.id.tv_month_total_value);
        dividerSummary = findViewById(R.id.divider_summary);
        tvHalf1Label = (TextView) findViewById(R.id.tv_half1_label);
        tvHalf2Label = (TextView) findViewById(R.id.tv_half2_label);

        cardCalendar = (LinearLayout) findViewById(R.id.card_calendar);
        monthViewContainer = (FrameLayout) findViewById(R.id.month_view_container);

        monthView = new MonthView(this);
        monthView.setMonth(currentMonth);
        monthView.setSelected(selectedDay);
        monthView.setToday(today);
        monthViewContainer.addView(monthView);

        tvSelectedDateTitle = (TextView) findViewById(R.id.tv_selected_date_title);
        btnAddEventMain = (Button) findViewById(R.id.btn_add_event_main);
        btnAddIncomeMain = (Button) findViewById(R.id.btn_add_income_main);
        containerDailyIncome = (LinearLayout) findViewById(R.id.container_daily_income);
        containerEvents = (LinearLayout) findViewById(R.id.container_events);
        tvEmptyDay = (TextView) findViewById(R.id.tv_empty_day);

        fabAdd = (Button) findViewById(R.id.fab_add);
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = currentMonth.plusMonths(-1);
                monthView.setMonth(currentMonth);
                renderCalendar();
            }
        });

        btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth = currentMonth.plusMonths(1);
                monthView.setMonth(currentMonth);
                renderCalendar();
            }
        });

        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                today = CalendarDate.today();
                currentMonth = CalendarDate.today();
                selectedDay = CalendarDate.today();
                monthView.setMonth(currentMonth);
                monthView.setSelected(selectedDay);
                monthView.setToday(today);
                renderCalendar();
                renderDayDetails();
            }
        });

        btnThemeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newDark = !prefs.isDarkMode();
                prefs.setDarkMode(newDark);
                applyTheme(newDark);
                renderDayDetails();
            }
        });

        // Double tap on Month Header opens Color Wheel picker
        tvMonthHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastHeaderTap < 400) {
                    showColorWheelDialog();
                }
                lastHeaderTap = now;
            }
        });

        monthView.setClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(CalendarDate date) {
                selectedDay = date;
                monthView.setSelected(selectedDay);
                renderDayDetails();
            }
        });

        monthView.setDoubleTapListener(new MonthView.OnDayDoubleTapListener() {
            @Override
            public void onDayDoubleTap(CalendarDate date) {
                selectedDay = date;
                monthView.setSelected(selectedDay);
                renderDayDetails();
                copyLastIncomeToSelectedDate();
            }
        });

        monthView.setLongClickListener(new MonthView.OnDayLongClickListener() {
            @Override
            public void onDayLongClick(final CalendarDate date) {
                selectedDay = date;
                monthView.setSelected(selectedDay);
                dbHelper.clearAllForDate(date.toDateStr());
                renderCalendar();
                renderDayDetails();
            }
        });

        btnAddEventMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEventDialog();
            }
        });

        btnAddIncomeMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddIncomeDialog();
            }
        });

        // FAB Click -> Opens Income Entry for Selected Day
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddIncomeDialog();
            }
        });

        // FAB Long Click -> Opens Secret Settings Panel (Pay Date feature)
        fabAdd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openSecretSettings();
                return true;
            }
        });
    }

    private void showColorWheelDialog() {
        final float density = getResources().getDisplayMetrics().density;
        boolean isDark = prefs.isDarkMode();
        int dialogBg = isDark ? 0xff12151c : 0xffffffff;
        int textPrimary = isDark ? 0xfff1f5f9 : 0xff0f172a;
        int textSecondary = isDark ? 0xff94a3b8 : 0xff64748b;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int) (24 * density), (int) (20 * density), (int) (24 * density), (int) (20 * density));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setShape(GradientDrawable.RECTANGLE);
        rootBg.setColor(dialogBg);
        rootBg.setCornerRadius(20 * density);
        root.setBackground(rootBg);

        TextView title = new TextView(this);
        title.setText("Choose App Accent");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(textPrimary);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Rotate wheel to pick any color");
        subtitle.setTextSize(12);
        subtitle.setTextColor(textSecondary);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, (int) (3 * density), 0, (int) (14 * density));
        root.addView(subtitle);

        final ColorWheelView colorWheel = new ColorWheelView(this);
        int size = (int) (220 * density);
        LinearLayout.LayoutParams cwp = new LinearLayout.LayoutParams(size, size);
        cwp.gravity = Gravity.CENTER_HORIZONTAL;
        colorWheel.setLayoutParams(cwp);
        colorWheel.setColor(prefs.getAccentColor());

        final int[] picked = new int[]{prefs.getAccentColor()};
        colorWheel.setOnColorSelectedListener(new ColorWheelView.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                picked[0] = color;
            }
        });
        root.addView(colorWheel);

        Button btnApply = new Button(this);
        btnApply.setText("Apply Color");
        btnApply.setTextColor(Color.WHITE);
        btnApply.setTextSize(14);
        btnApply.setTypeface(null, Typeface.BOLD);

        GradientDrawable applyBg = new GradientDrawable();
        applyBg.setShape(GradientDrawable.RECTANGLE);
        applyBg.setColor(prefs.getAccentColor());
        applyBg.setCornerRadius(12 * density);
        btnApply.setBackground(applyBg);

        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (44 * density));
        blp.setMargins(0, (int) (18 * density), 0, 0);
        btnApply.setLayoutParams(blp);
        root.addView(btnApply);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(root);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.setAccentColor(picked[0]);
                applyTheme(prefs.isDarkMode());
                renderCalendar();
                renderDayDetails();
                dialog.dismiss();
            }
        });
    }

    private void openSecretSettings() {
        new SecretSettingsPanel(MainActivity.this, prefs, new Runnable() {
            @Override
            public void run() {
                applyTheme(prefs.isDarkMode());
                renderCalendar();
                renderDayDetails();
            }
        }).show();
    }

    private void applyTheme(boolean isDark) {
        int bgColor = isDark ? 0xff0d0f14 : 0xfff8fafc;
        int cardBgColor = isDark ? 0xff161920 : 0xffffffff;
        int cardStroke = isDark ? 0xff27272a : 0xffe2e8f0;
        int textColor = isDark ? 0xfff1f5f9 : 0xff0f172a;
        int textSoft = isDark ? 0xff94a3b8 : 0xff475569;
        int dividerColor = isDark ? 0xff27272a : 0xffe2e8f0;
        int accent = prefs.getAccentColor();

        mainRoot.setBackgroundColor(bgColor);

        // Top bar
        topBar.setBackgroundColor(cardBgColor);
        tvMonthHeader.setTextColor(textColor);
        btnThemeToggle.setText(isDark ? "☀️" : "🌙");

        float density = getResources().getDisplayMetrics().density;

        // Card backgrounds
        GradientDrawable cardBg1 = new GradientDrawable();
        cardBg1.setShape(GradientDrawable.RECTANGLE);
        cardBg1.setCornerRadius(14 * density);
        cardBg1.setColor(cardBgColor);
        cardBg1.setStroke((int) (1 * density), cardStroke);
        cardMonthSummary.setBackground(cardBg1);

        GradientDrawable cardBg2 = new GradientDrawable();
        cardBg2.setShape(GradientDrawable.RECTANGLE);
        cardBg2.setCornerRadius(14 * density);
        cardBg2.setColor(cardBgColor);
        cardBg2.setStroke((int) (1 * density), cardStroke);
        cardCalendar.setBackground(cardBg2);

        // Elegant FAB design: glowing subtle border and accent gradient
        GradientDrawable fabBg = new GradientDrawable();
        fabBg.setShape(GradientDrawable.OVAL);
        fabBg.setColor(accent);
        fabBg.setStroke((int) (2.5f * density), isDark ? 0x44ffffff : 0x22000000);
        fabAdd.setBackground(fabBg);

        // Action buttons
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setColor(accent);
        btnBg.setCornerRadius(10 * density);
        btnAddEventMain.setBackground(btnBg);
        btnAddIncomeMain.setBackground(btnBg);
        btnToday.setBackground(btnBg);

        tvMonthTotalLabel.setTextColor(textSoft);
        dividerSummary.setBackgroundColor(dividerColor);
        tvHalf1Label.setTextColor(textSoft);
        tvHalf2Label.setTextColor(textSoft);
        tvSelectedDateTitle.setTextColor(textColor);
        tvEmptyDay.setTextColor(textSoft);

        monthView.setDarkMode(isDark, accent);
    }

    private void renderCalendar() {
        tvMonthHeader.setText(currentMonth.monthLabel());

        String prefix = currentMonth.toYearMonthPrefix();
        Map<String, List<String>> eventsMap = dbHelper.getEventColorsForMonth(prefix);
        Map<String, Double> incomeMap = dbHelper.getIncomeTotalsForMonth(prefix);

        monthView.setData(eventsMap, incomeMap);

        // Compute Month Summary
        double totalMonth = 0;
        double firstHalf = 0;
        double secondHalf = 0;

        for (Map.Entry<String, Double> entry : incomeMap.entrySet()) {
            double val = entry.getValue();
            totalMonth += val;
            CalendarDate d = CalendarDate.fromDateStr(entry.getKey());
            if (d.day <= 15) {
                firstHalf += val;
            } else {
                secondHalf += val;
            }
        }

        tvMonthTotalValue.setText(numFormat.format(totalMonth));
        tvHalf1Label.setText("1–15: +" + numFormat.format(firstHalf));
        int maxDays = currentMonth.daysInMonth();
        tvHalf2Label.setText("16–" + maxDays + ": +" + numFormat.format(secondHalf));
    }

    private void renderDayDetails() {
        if (selectedDay == null) return;

        boolean isDark = prefs.isDarkMode();
        int textColor = isDark ? 0xfff1f5f9 : 0xff0f172a;
        int textSoft = isDark ? 0xff94a3b8 : 0xff475569;
        int cardBgColor = isDark ? 0xff161920 : 0xffffffff;
        int cardStroke = isDark ? 0xff27272a : 0xffe2e8f0;
        float density = getResources().getDisplayMetrics().density;

        tvSelectedDateTitle.setText(selectedDay.fullLabel());

        containerDailyIncome.removeAllViews();
        containerEvents.removeAllViews();

        String dateKey = selectedDay.toDateStr();
        final Event income = dbHelper.getIncomeForDate(dateKey);
        List<Event> events = dbHelper.getEventsForDate(dateKey);

        boolean hasIncome = (income != null && income.total > 0);
        boolean hasEvents = (!events.isEmpty());

        tvEmptyDay.setVisibility((!hasIncome && !hasEvents) ? View.VISIBLE : View.GONE);

        // Render Daily Income Card
        if (hasIncome) {
            LinearLayout incomeCard = new LinearLayout(this);
            incomeCard.setOrientation(LinearLayout.VERTICAL);
            incomeCard.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (12 * density));

            GradientDrawable icBg = new GradientDrawable();
            icBg.setShape(GradientDrawable.RECTANGLE);
            icBg.setCornerRadius(12 * density);
            icBg.setColor(cardBgColor);
            icBg.setStroke((int) (1 * density), isDark ? 0xff065f46 : 0xffa7f3d0);
            incomeCard.setBackground(icBg);

            // Income Title & Total row
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvIncTitle = new TextView(this);
            tvIncTitle.setText("💰 Earnings: +" + numFormat.format(income.total));
            tvIncTitle.setTextSize(15);
            tvIncTitle.setTypeface(null, Typeface.BOLD);
            tvIncTitle.setTextColor(isDark ? 0xff34d399 : 0xff059669);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            topRow.addView(tvIncTitle, tlp);

            // Edit button
            TextView btnEdit = new TextView(this);
            btnEdit.setText("✏️ Edit");
            btnEdit.setTextSize(13);
            btnEdit.setTextColor(prefs.getAccentColor());
            btnEdit.setPadding((int) (8 * density), (int) (4 * density), (int) (8 * density), (int) (4 * density));
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddIncomeDialog(income);
                }
            });
            topRow.addView(btnEdit);

            // Delete button with confirmation
            TextView btnDel = new TextView(this);
            btnDel.setText("✕");
            btnDel.setTextSize(16);
            btnDel.setTextColor(0xffef4444);
            btnDel.setPadding((int) (8 * density), (int) (4 * density), (int) (8 * density), (int) (4 * density));
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Earnings")
                            .setMessage("Remove earnings for " + selectedDay.fullLabel() + "?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.deleteIncomeForDate(income.dateStr);
                                    renderCalendar();
                                    renderDayDetails();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
            topRow.addView(btnDel);

            incomeCard.addView(topRow);

            // Breakdown row
            StringBuilder sb = new StringBuilder();
            if (income.regular > 0) sb.append("Regular: ").append(numFormat.format(income.regular)).append("  ");
            if (income.overtime > 0) sb.append("OT: ").append(numFormat.format(income.overtime)).append("  ");
            if (income.doublePay > 0) sb.append("Double: ").append(numFormat.format(income.doublePay)).append("  ");
            if (income.holiday > 0) sb.append("Holiday: ").append(numFormat.format(income.holiday)).append("  ");
            if (income.extra > 0) sb.append("Extra: ").append(numFormat.format(income.extra));

            if (sb.length() > 0) {
                TextView tvBreakdown = new TextView(this);
                tvBreakdown.setText(sb.toString().trim());
                tvBreakdown.setTextSize(12);
                tvBreakdown.setTextColor(textSoft);
                tvBreakdown.setPadding(0, (int) (4 * density), 0, 0);
                incomeCard.addView(tvBreakdown);
            }

            containerDailyIncome.addView(incomeCard);
        }

        // Render Events
        for (final Event evt : events) {
            LinearLayout evtCard = new LinearLayout(this);
            evtCard.setOrientation(LinearLayout.HORIZONTAL);
            evtCard.setGravity(Gravity.CENTER_VERTICAL);
            evtCard.setPadding((int) (12 * density), (int) (10 * density), (int) (12 * density), (int) (10 * density));
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            elp.setMargins(0, 0, 0, (int) (6 * density));
            evtCard.setLayoutParams(elp);

            GradientDrawable eBg = new GradientDrawable();
            eBg.setShape(GradientDrawable.RECTANGLE);
            eBg.setCornerRadius(10 * density);
            eBg.setColor(cardBgColor);
            eBg.setStroke((int) (1 * density), cardStroke);
            evtCard.setBackground(eBg);

            // Colored vertical accent bar
            View bar = new View(this);
            int barColor = 0xff7c6af7;
            try {
                barColor = Color.parseColor(evt.color);
            } catch (Exception ignored) {}
            GradientDrawable barBg = new GradientDrawable();
            barBg.setShape(GradientDrawable.RECTANGLE);
            barBg.setCornerRadius(4 * density);
            barBg.setColor(barColor);
            bar.setBackground(barBg);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams((int) (4 * density), (int) (32 * density));
            blp.setMargins(0, 0, (int) (10 * density), 0);
            evtCard.addView(bar, blp);

            // Event title & time info
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(evt.title);
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextColor(textColor);
            info.addView(tvTitle);

            if (evt.time != null && !evt.time.trim().isEmpty()) {
                TextView tvTime = new TextView(this);
                tvTime.setText("⏰ " + evt.time);
                tvTime.setTextSize(12);
                tvTime.setTextColor(prefs.getAccentColor());
                tvTime.setPadding(0, (int) (2 * density), 0, 0);
                info.addView(tvTime);
            }

            if (evt.description != null && !evt.description.trim().isEmpty()) {
                TextView tvDesc = new TextView(this);
                tvDesc.setText(evt.description);
                tvDesc.setTextSize(12);
                tvDesc.setTextColor(textSoft);
                tvDesc.setPadding(0, (int) (2 * density), 0, 0);
                info.addView(tvDesc);
            }
            evtCard.addView(info, ilp);

            // Delete event button with confirmation
            TextView btnDelete = new TextView(this);
            btnDelete.setText("✕");
            btnDelete.setTextSize(16);
            btnDelete.setTextColor(isDark ? 0xff71717a : 0xff9ca3af);
            btnDelete.setPadding((int) (10 * density), (int) (4 * density), (int) (4 * density), (int) (4 * density));
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete \"" + evt.title + "\"?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.deleteEvent(evt.id);
                                    renderCalendar();
                                    renderDayDetails();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
            evtCard.addView(btnDelete);

            containerEvents.addView(evtCard);
        }
    }

    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);
        builder.setTitle("Add Event: " + selectedDay.fullLabel());

        final EditText etTitle = (EditText) dialogView.findViewById(R.id.et_event_title);
        final EditText etTime = (EditText) dialogView.findViewById(R.id.et_event_time);
        final EditText etDesc = (EditText) dialogView.findViewById(R.id.et_event_desc);
        final RadioButton rbWork = (RadioButton) dialogView.findViewById(R.id.rb_work);
        final RadioButton rbPersonal = (RadioButton) dialogView.findViewById(R.id.rb_personal);
        final RadioButton rbUrgent = (RadioButton) dialogView.findViewById(R.id.rb_urgent);
        final RadioButton rbHealth = (RadioButton) dialogView.findViewById(R.id.rb_health);

        builder.setPositiveButton("Save Event", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = etTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    return;
                }

                String time = etTime.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();

                String color = "#7c6af7";
                if (rbPersonal.isChecked()) color = "#10b981";
                else if (rbUrgent.isChecked()) color = "#ef4444";
                else if (rbHealth.isChecked()) color = "#06b6d4";

                Event e = Event.createEvent(selectedDay.toDateStr(), title, time, desc, color);
                dbHelper.insertEvent(e);

                renderCalendar();
                renderDayDetails();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddIncomeDialog() {
        showAddIncomeDialog(dbHelper.getIncomeForDate(selectedDay.toDateStr()));
    }

    private void showAddIncomeDialog(final Event existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_income, null);
        builder.setView(dialogView);
        builder.setTitle("Earnings: " + selectedDay.fullLabel());

        final EditText etRegular = (EditText) dialogView.findViewById(R.id.et_regular);
        final Button btnOtMinus = (Button) dialogView.findViewById(R.id.btn_ot_minus);
        final EditText etOtHours = (EditText) dialogView.findViewById(R.id.et_ot_hours);
        final Button btnOtPlus = (Button) dialogView.findViewById(R.id.btn_ot_plus);
        final EditText etOvertime = (EditText) dialogView.findViewById(R.id.et_overtime);

        final CheckBox cbHoliday = (CheckBox) dialogView.findViewById(R.id.cb_holiday);
        final EditText etHoliday = (EditText) dialogView.findViewById(R.id.et_holiday);

        final CheckBox cbDoublePay = (CheckBox) dialogView.findViewById(R.id.cb_double_pay);
        final EditText etDoublePay = (EditText) dialogView.findViewById(R.id.et_double_pay);

        final EditText etExtra = (EditText) dialogView.findViewById(R.id.et_extra);
        final TextView tvCalcTotal = (TextView) dialogView.findViewById(R.id.tv_calc_total);

        // Pre-fill existing data if editing
        if (existing != null) {
            if (existing.regular > 0) etRegular.setText(String.valueOf(existing.regular));
            if (existing.overtime > 0) {
                etOvertime.setText(String.valueOf(existing.overtime));
                if (existing.regular > 0) {
                    double hourly = existing.regular / 8.0 * prefs.overtimeMultiplier();
                    if (hourly > 0) {
                        int h = (int) Math.round(existing.overtime / hourly);
                        etOtHours.setText(String.valueOf(h));
                    }
                }
            }
            if (existing.holiday > 0) {
                cbHoliday.setChecked(true);
                etHoliday.setText(String.valueOf(existing.holiday));
            }
            if (existing.doublePay > 0) {
                cbDoublePay.setChecked(true);
                etDoublePay.setText(String.valueOf(existing.doublePay));
            }
            if (existing.extra > 0) etExtra.setText(String.valueOf(existing.extra));
            tvCalcTotal.setText(numFormat.format(existing.total));
        }

        // Live calculation callback
        final Runnable recompute = new Runnable() {
            private boolean updating = false;
            @Override
            public void run() {
                if (updating) return;
                updating = true;
                try {
                    double reg = parseVal(etRegular.getText().toString());
                    long hours = parseLong(etOtHours.getText().toString());

                    double ot = 0;
                    if (prefs.includeOvertime() && reg > 0 && hours > 0) {
                        ot = (reg / 8.0) * prefs.overtimeMultiplier() * hours;
                        etOvertime.setText(numFormat.format(ot));
                    }

                    double hol = 0;
                    if (prefs.includeHoliday() && cbHoliday.isChecked() && reg > 0) {
                        hol = reg * prefs.holidayMultiplier();
                        etHoliday.setText(numFormat.format(hol));
                    }

                    double dp = 0;
                    if (prefs.includeDoublePay() && cbDoublePay.isChecked() && reg > 0) {
                        dp = reg * prefs.doublePayMultiplier();
                        etDoublePay.setText(numFormat.format(dp));
                    }

                    double extra = parseVal(etExtra.getText().toString());

                    double total = 0;
                    if (prefs.includeHoliday() && cbHoliday.isChecked()) {
                        total = hol + ot + (prefs.includeExtra() ? extra : 0);
                    } else if (prefs.includeDoublePay() && cbDoublePay.isChecked()) {
                        total = dp + ot + (prefs.includeExtra() ? extra : 0);
                    } else {
                        if (prefs.includeRegular()) total += reg;
                        if (prefs.includeOvertime()) total += ot;
                        if (prefs.includeExtra()) total += extra;
                    }

                    tvCalcTotal.setText(numFormat.format(total));
                } finally {
                    updating = false;
                }
            }
        };

        // Stepper buttons for Overtime hours
        btnOtMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long h = parseLong(etOtHours.getText().toString());
                if (h > 0) h--;
                etOtHours.setText(String.valueOf(h));
                recompute.run();
            }
        });

        btnOtPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long h = parseLong(etOtHours.getText().toString());
                h++;
                etOtHours.setText(String.valueOf(h));
                recompute.run();
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { recompute.run(); }
        };

        etRegular.addTextChangedListener(watcher);
        etOtHours.addTextChangedListener(watcher);
        etExtra.addTextChangedListener(watcher);

        cbHoliday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) cbDoublePay.setChecked(false); // Mutually exclusive
                recompute.run();
            }
        });

        cbDoublePay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) cbHoliday.setChecked(false); // Mutually exclusive
                recompute.run();
            }
        });

        builder.setPositiveButton("Save Earnings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double reg = parseVal(etRegular.getText().toString());
                long hours = parseLong(etOtHours.getText().toString());

                double ot = parseVal(etOvertime.getText().toString());
                if (ot <= 0 && reg > 0 && hours > 0) {
                    ot = (reg / 8.0) * prefs.overtimeMultiplier() * hours;
                }

                double hol = cbHoliday.isChecked() ? (reg * prefs.holidayMultiplier()) : 0;
                double dp = cbDoublePay.isChecked() ? (reg * prefs.doublePayMultiplier()) : 0;
                double extra = parseVal(etExtra.getText().toString());

                double total = 0;
                if (cbHoliday.isChecked()) {
                    total = hol + ot + extra;
                } else if (cbDoublePay.isChecked()) {
                    total = dp + ot + extra;
                } else {
                    total = reg + ot + extra;
                }

                if (total <= 0) {
                    return;
                }

                Event inc = Event.createIncome(selectedDay.toDateStr(), reg, ot, dp, hol, extra);
                inc.total = total;
                dbHelper.upsertIncome(inc);

                renderCalendar();
                renderDayDetails();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void copyLastIncomeToSelectedDate() {
        if (!CalendarDbHelper.hasLastIncome) {
            return;
        }

        Event inc = Event.createIncome(
                selectedDay.toDateStr(),
                CalendarDbHelper.lastRegular,
                CalendarDbHelper.lastOvertime,
                CalendarDbHelper.lastDoublePay,
                CalendarDbHelper.lastHoliday,
                CalendarDbHelper.lastExtra
        );

        dbHelper.upsertIncome(inc);
        renderCalendar();
        renderDayDetails();
    }

    private static double parseVal(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(s.trim().replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
