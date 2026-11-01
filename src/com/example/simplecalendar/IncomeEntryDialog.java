package com.example.simplecalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class IncomeEntryDialog {
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##",
        DecimalFormatSymbols.getInstance(Locale.getDefault()));
    private static final DecimalFormat MULT = new DecimalFormat("0",
        DecimalFormatSymbols.getInstance(Locale.getDefault()));

    private final Context context;
    private final EventStore store;
    private final Prefs prefs;
    private final Runnable onSaved;

    private AlertDialog dialog;
    private EditText etRegular, etOvertimeMult, etOvertime, etExtra, etHolidayAmount, etDoublePayAmount;
    private CheckBox cbHoliday, cbDoublePay;

    private boolean updating = false;
    private Event editing;
    private CalendarDate day;

    public IncomeEntryDialog(Context context, EventStore store, Prefs prefs, Runnable onSaved) {
        this.context = context;
        this.store = store;
        this.prefs = prefs;
        this.onSaved = onSaved;
    }

    public void show(Event event, CalendarDate day) {
        this.day = day;
        this.editing = event;
        boolean isEdit = event != null && event.id > 0;

        double savedMult = prefs.defaultOvertimeMult();
        if (isEdit && event.regular > 0 && event.overtime > 0) {
            savedMult = (event.overtime / (event.regular / 8.0)) / 1.25;
        }

        int accent = prefs.appColor();
        int foreground = UiUtils.getColor(context, R.color.foreground);
        int muted = UiUtils.getColor(context, R.color.muted);
        int white = UiUtils.getColor(context, R.color.white);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(UiUtils.dpToPx(context, 20), UiUtils.dpToPx(context, 16),
            UiUtils.dpToPx(context, 20), UiUtils.dpToPx(context, 16));

        // Header
        TextView head = new TextView(context);
        head.setText(isEdit ? context.getString(R.string.edit_event) : context.getString(R.string.add_event));
        head.setTextSize(22);
        head.setTextColor(foreground);
        head.setTypeface(null, android.graphics.Typeface.BOLD);
        head.setPadding(0, 0, 0, UiUtils.dpToPx(context, 16));
        root.addView(head);

        // Regular
        // Add another function button area
        if (prefs.includeRegular()) {
            root.addView(fieldLabel(context, "Regular", muted));
            etRegular = moneyField(context, isEdit ? event.regular : 0);
            etRegular.setTextColor(foreground);
            root.addView(etRegular);
        }

        // Overtime
        if (prefs.includeOvertime()) {
            root.addView(fieldLabel(context, "Overtime", muted));
            LinearLayout otRow = new LinearLayout(context);
            otRow.setOrientation(LinearLayout.HORIZONTAL);
            otRow.setGravity(Gravity.CENTER_VERTICAL);

            Button btnMinus = new Button(context);
            btnMinus.setText("\u2212");
            btnMinus.setAllCaps(false);
            btnMinus.setTextColor(0xffffffff);
            GradientDrawable minusBg = new GradientDrawable();
            minusBg.setShape(GradientDrawable.RECTANGLE);
            minusBg.setColor(accent);
            minusBg.setCornerRadius(UiUtils.dpToPx(context, 8));
            btnMinus.setBackground(minusBg);
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                UiUtils.dpToPx(context, 44), UiUtils.dpToPx(context, 44));
            btnMinus.setLayoutParams(btnLp);
            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    long cur = parseLong(etOvertimeMult);
                    etOvertimeMult.setText(String.valueOf(Math.max(0, cur - 1)));
                }
            });
            otRow.addView(btnMinus);

            etOvertimeMult = new EditText(context);
            etOvertimeMult.setText(formatMult(savedMult));
            etOvertimeMult.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            etOvertimeMult.setGravity(Gravity.CENTER);
            etOvertimeMult.setWidth(UiUtils.dpToPx(context, 60));
            etOvertimeMult.setTextSize(16);
            etOvertimeMult.setTypeface(null, android.graphics.Typeface.BOLD);
            etOvertimeMult.setTextColor(foreground);
            LinearLayout.LayoutParams multLp = new LinearLayout.LayoutParams(
                UiUtils.dpToPx(context, 60), UiUtils.dpToPx(context, 44));
            multLp.leftMargin = UiUtils.dpToPx(context, 8);
            multLp.rightMargin = UiUtils.dpToPx(context, 8);
            etOvertimeMult.setLayoutParams(multLp);
            otRow.addView(etOvertimeMult);

            Button btnPlus = new Button(context);
            btnPlus.setText("+");
            btnPlus.setAllCaps(false);
            btnPlus.setTextColor(0xffffffff);
            GradientDrawable plusBg = new GradientDrawable();
            plusBg.setShape(GradientDrawable.RECTANGLE);
            plusBg.setColor(accent);
            plusBg.setCornerRadius(UiUtils.dpToPx(context, 8));
            btnPlus.setBackground(plusBg);
            btnPlus.setLayoutParams(btnLp);
            btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    long cur = parseLong(etOvertimeMult);
                    etOvertimeMult.setText(String.valueOf(cur + 1));
                }
            });
            otRow.addView(btnPlus);



            root.addView(otRow);

            etOvertime = moneyField(context, isEdit ? event.overtime : 0);
            etOvertime.setEnabled(false);
            etOvertime.setFocusable(false);
            etOvertime.setTextColor(foreground);
            root.addView(etOvertime);
        }

        // Holiday checkbox
        if (prefs.includeHoliday()) {
            LinearLayout holidayRow = new LinearLayout(context);
            holidayRow.setOrientation(LinearLayout.HORIZONTAL);
            holidayRow.setGravity(Gravity.CENTER_VERTICAL);
            holidayRow.setPadding(0, UiUtils.dpToPx(context, 8), 0, 0);
            cbHoliday = new CheckBox(context);
            cbHoliday.setText("Holiday");
            cbHoliday.setTextColor(foreground);
            cbHoliday.setButtonTintList(ColorStateList.valueOf(foreground));
            cbHoliday.setChecked(isEdit && event.holiday > 0);
            holidayRow.addView(cbHoliday);
            
            root.addView(holidayRow);

            // Holiday amount field (read-only)
            etHolidayAmount = moneyField(context, isEdit && event.holiday > 0 ? event.holiday : 0);
            etHolidayAmount.setEnabled(false);
            etHolidayAmount.setFocusable(false);
            etHolidayAmount.setTextColor(foreground);
            root.addView(etHolidayAmount);
        }

        // Double Pay checkbox
        if (prefs.includeDoublePay()) {
            LinearLayout doubleRow = new LinearLayout(context);
            doubleRow.setOrientation(LinearLayout.HORIZONTAL);
            doubleRow.setGravity(Gravity.CENTER_VERTICAL);
            cbDoublePay = new CheckBox(context);
            cbDoublePay.setText("Double Pay");
            cbDoublePay.setTextColor(foreground);
            cbDoublePay.setButtonTintList(ColorStateList.valueOf(foreground));
            cbDoublePay.setChecked(isEdit && event.doublePay > 0);
            doubleRow.addView(cbDoublePay);
            
            root.addView(doubleRow);

            // Double Pay amount field (read-only)
            etDoublePayAmount = moneyField(context, isEdit && event.doublePay > 0 ? event.doublePay : 0);
            etDoublePayAmount.setEnabled(false);
            etDoublePayAmount.setFocusable(false);
            etDoublePayAmount.setTextColor(foreground);
            root.addView(etDoublePayAmount);
        }

        // Extra
        if (prefs.includeExtra()) {
            root.addView(fieldLabel(context, "Extra", muted));
            etExtra = moneyField(context, isEdit ? event.extra : 0);
            etExtra.setTextColor(foreground);
            root.addView(etExtra);
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { recompute(); }
        };
        if (prefs.includeRegular() && etRegular != null) {
            etRegular.addTextChangedListener(watcher);
        }
        if (prefs.includeOvertime() && etOvertimeMult != null) {
            etOvertimeMult.addTextChangedListener(watcher);
        }
        if (prefs.includeExtra() && etExtra != null) {
            etExtra.addTextChangedListener(watcher);
        }
        if (prefs.includeHoliday() && cbHoliday != null) {
            cbHoliday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton b, boolean checked) { recompute(); }
            });
        }
        if (prefs.includeDoublePay() && cbDoublePay != null) {
            cbDoublePay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton b, boolean checked) { recompute(); }
            });
        }

        recompute();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(root);
        builder.setPositiveButton(context.getString(R.string.save), null);
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        if (isEdit) {
            builder.setNeutralButton(context.getString(R.string.delete), null);
        }
        dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(UiUtils.roundedBg(
                UiUtils.getColor(context, R.color.surface), 16));
        }

        Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveBtn.setTextColor(accent);
        saveBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { save(isEdit); }
        });

        Button delBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (delBtn != null && isEdit) {
            delBtn.setTextColor(UiUtils.getColor(context, R.color.danger));
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.delete_confirm))
                        .setPositiveButton(context.getString(R.string.delete),
                            new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface d, int w) {
                                    store.deleteForDay(day);
                                    dialog.dismiss();
                                    if (onSaved != null) onSaved.run();
                                }
                            })
                        .setNegativeButton(context.getString(R.string.cancel), null)
                        .show();
                }
            });
        }

        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negBtn.setTextColor(UiUtils.getColor(context, R.color.muted));
        negBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); }
        });
    }

    private static TextView fieldLabel(Context ctx, String text, int muted) {
        TextView lbl = new TextView(ctx);
        lbl.setText(text);
        lbl.setTextSize(13);
        lbl.setTextColor(muted);
        lbl.setTypeface(null, android.graphics.Typeface.BOLD);
        lbl.setPadding(0, UiUtils.dpToPx(ctx, 12), 0, UiUtils.dpToPx(ctx, 2));
        return lbl;
    }

    private static EditText moneyField(Context ctx, double value) {
        EditText et = new EditText(ctx);
        et.setText(format(value));
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setTextSize(16);
        et.setPadding(UiUtils.dpToPx(ctx, 12), UiUtils.dpToPx(ctx, 12),
            UiUtils.dpToPx(ctx, 12), UiUtils.dpToPx(ctx, 12));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(UiUtils.getColor(ctx, R.color.surface_2));
        bg.setCornerRadius(UiUtils.dpToPx(ctx, 8));
        et.setBackground(bg);
        return et;
    }

    private static long parseLong(EditText et) {
        if (et == null) return 0;
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Long.parseLong(s); } catch (Exception ex) { return 0; }
    }

    private static double parse(EditText et) {
        if (et == null) return 0;
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Double.parseDouble(s); } catch (Exception ex) { return 0; }
    }

    private static String format(double v) {
        return MONEY.format(v);
    }

    private static String formatMult(double v) {
        return MULT.format(v);
    }

    private void recompute() {
        if (updating) return;
        updating = true;
        try {
            double regular = (etRegular != null) ? parse(etRegular) : 0;
            long mult = (etOvertimeMult != null) ? parseLong(etOvertimeMult) : 0;
            double overtime = prefs.includeOvertime() ? (regular / 8.0) * prefs.overtimeMultiplier() * mult : 0;
            double holiday = (prefs.includeHoliday() && cbHoliday != null && cbHoliday.isChecked()) ? regular * prefs.holidayMultiplier() : 0;
            double doublePay = (prefs.includeDoublePay() && cbDoublePay != null && cbDoublePay.isChecked()) ? regular * prefs.doublePayMultiplier() : 0;
            double extra = (etExtra != null) ? parse(etExtra) : 0;
            if (etOvertime != null) etOvertime.setText(format(overtime));
            if (etHolidayAmount != null) etHolidayAmount.setText(format(holiday));
            if (etDoublePayAmount != null) etDoublePayAmount.setText(format(doublePay));
            } finally {
                updating = false;
            }
        }

    private void save(boolean isEdit) {
        double regular = (etRegular != null) ? parse(etRegular) : 0;
        long mult = (etOvertimeMult != null) ? parseLong(etOvertimeMult) : 0;
        double overtime = prefs.includeOvertime() ? (regular / 8.0) * prefs.overtimeMultiplier() * mult : 0;
        double holiday = (prefs.includeHoliday() && cbHoliday != null && cbHoliday.isChecked()) ? regular * prefs.holidayMultiplier() : 0;
        double doublePay = (prefs.includeDoublePay() && cbDoublePay != null && cbDoublePay.isChecked()) ? regular * prefs.doublePayMultiplier() : 0;
        double extra = (etExtra != null) ? parse(etExtra) : 0;
        double total = 0;
        
        if (prefs.includeHoliday() && cbHoliday.isChecked()) {
            // Holiday: Holiday amount already includes Regular (Regular × 1.30)
            // Add overtime and extra on top, but do NOT add Regular again
            total = holiday + overtime + extra;
        } else if (prefs.includeDoublePay() && cbDoublePay.isChecked()) {
            // Double Pay: Double Pay amount already includes Regular (Regular × 2.00)
            // Add overtime and extra on top, but do NOT add Regular again
            total = doublePay + overtime + extra;
        } else {
            // Normal day: Regular + Overtime + Extra
            if (prefs.includeRegular()) total += regular;
            if (prefs.includeOvertime()) total += overtime;
            if (prefs.includeExtra()) total += extra;
        }

        String note = day.fullLabel();

        if (isEdit) {
            editing.title = note;
            editing.description = null;
            editing.startMs = day.startOfDayMillis();
            editing.endMs = day.endOfDayMillis();
            editing.allDay = true;
            editing.color = UiUtils.getColor(context, R.color.income);
            editing.reminderMin = 0;
            editing.regular = prefs.includeRegular() ? regular : 0;
            editing.overtime = prefs.includeOvertime() ? overtime : 0;
            editing.doublePay = prefs.includeDoublePay() ? doublePay : 0;
            editing.holiday = prefs.includeHoliday() ? holiday : 0;
            editing.extra = prefs.includeExtra() ? extra : 0;
            editing.total = total;
            store.update(editing);
        } else {
            Event e = new Event();
            e.title = note;
            e.description = null;
            e.startMs = day.startOfDayMillis();
            e.endMs = day.endOfDayMillis();
            e.allDay = true;
            e.color = UiUtils.getColor(context, R.color.income);
            e.reminderMin = 0;
            e.regular = prefs.includeRegular() ? regular : 0;
            e.overtime = prefs.includeOvertime() ? overtime : 0;
            e.doublePay = prefs.includeDoublePay() ? doublePay : 0;
            e.holiday = prefs.includeHoliday() ? holiday : 0;
            e.extra = prefs.includeExtra() ? extra : 0;
            e.total = total;
            store.insert(e);
        }
        dialog.dismiss();
        if (onSaved != null) onSaved.run();
    }
}
