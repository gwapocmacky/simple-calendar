package com.example.simplecalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;

public class SecretSettingsPanel {
    private final Context context;
    private final Prefs prefs;

    public SecretSettingsPanel(Context context, Prefs prefs) {
        this.context = context;
        this.prefs = prefs;
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(UiUtils.dpToPx(context, 20), UiUtils.dpToPx(context, 16),
            UiUtils.dpToPx(context, 20), UiUtils.dpToPx(context, 16));
        root.setBackgroundColor(UiUtils.getColor(context, R.color.surface));

        int accent = prefs.appColor();
        int foreground = UiUtils.getColor(context, R.color.foreground);
        int muted = UiUtils.getColor(context, R.color.muted);

        // Header with accent bar
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, UiUtils.dpToPx(context, 16));

        View accentBar = new View(context);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setShape(GradientDrawable.RECTANGLE);
        barBg.setColor(accent);
        barBg.setCornerRadius(UiUtils.dpToPx(context, 2));
        accentBar.setBackground(barBg);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
            UiUtils.dpToPx(context, 4), UiUtils.dpToPx(context, 24));
        barLp.rightMargin = UiUtils.dpToPx(context, 12);
        accentBar.setLayoutParams(barLp);
        headerRow.addView(accentBar);

        TextView head = new TextView(context);
        head.setText("Secret Settings");
        head.setTextSize(22);
        head.setTextColor(foreground);
        head.setTypeface(null, android.graphics.Typeface.BOLD);
        headerRow.addView(head);

        root.addView(headerRow);

        // Overtime multiplier
        root.addView(fieldLabel(context, "Overtime multiplier:", muted));
        LinearLayout otRow = new LinearLayout(context);
        otRow.setOrientation(LinearLayout.HORIZONTAL);
        otRow.setGravity(Gravity.CENTER_VERTICAL);
        otRow.setPadding(0, 0, 0, UiUtils.dpToPx(context, 16));

        TextView otLabel = new TextView(context);
        otLabel.setText("Regular ÷ 8 × 1.25 × mult");
        otLabel.setTextSize(13);
        otLabel.setTextColor(muted);
        otRow.addView(otLabel);

        EditText etOtMult = new EditText(context);
        etOtMult.setText(String.valueOf(prefs.overtimeMultiplier()));
        etOtMult.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etOtMult.setWidth(80);
        etOtMult.setTextColor(foreground);
        otRow.addView(etOtMult);
        root.addView(otRow);

        // Holiday multiplier
        root.addView(fieldLabel(context, "Holiday multiplier:", muted));
        LinearLayout holRow = new LinearLayout(context);
        holRow.setOrientation(LinearLayout.HORIZONTAL);
        holRow.setGravity(Gravity.CENTER_VERTICAL);
        holRow.setPadding(0, 0, 0, UiUtils.dpToPx(context, 16));

        TextView holLabel = new TextView(context);
        holLabel.setText("Regular × mult");
        holLabel.setTextSize(13);
        holLabel.setTextColor(muted);
        holRow.addView(holLabel);

        EditText etHolMult = new EditText(context);
        etHolMult.setText(String.valueOf(prefs.holidayMultiplier()));
        etHolMult.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etHolMult.setWidth(80);
        etHolMult.setTextColor(foreground);
        holRow.addView(etHolMult);
        root.addView(holRow);

        // Double Pay multiplier
        root.addView(fieldLabel(context, "Double Pay multiplier:", muted));
        LinearLayout dpRow = new LinearLayout(context);
        dpRow.setOrientation(LinearLayout.HORIZONTAL);
        dpRow.setGravity(Gravity.CENTER_VERTICAL);
        dpRow.setPadding(0, 0, 0, UiUtils.dpToPx(context, 16));

        TextView dpLabel = new TextView(context);
        dpLabel.setText("Regular × mult");
        dpLabel.setTextSize(13);
        dpLabel.setTextColor(muted);
        dpRow.addView(dpLabel);

        EditText etDpMult = new EditText(context);
        etDpMult.setText(String.valueOf(prefs.doublePayMultiplier()));
        etDpMult.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDpMult.setWidth(80);
        etDpMult.setTextColor(foreground);
        dpRow.addView(etDpMult);
        root.addView(dpRow);

        // Component toggles
        TextView componentsLabel = new TextView(context);
        componentsLabel.setText("Include in Total:");
        componentsLabel.setTextSize(16);
        componentsLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        componentsLabel.setTextColor(foreground);
        componentsLabel.setPadding(0, 16, 0, 4);
        root.addView(componentsLabel);

        CheckBox cbRegular = new CheckBox(context);
        cbRegular.setText("Regular");
        cbRegular.setTextColor(foreground);
        cbRegular.setChecked(prefs.includeRegular());
        root.addView(cbRegular);

        CheckBox cbOvertime = new CheckBox(context);
        cbOvertime.setText("Overtime");
        cbOvertime.setTextColor(foreground);
        cbOvertime.setChecked(prefs.includeOvertime());
        root.addView(cbOvertime);

        CheckBox cbHoliday = new CheckBox(context);
        cbHoliday.setText("Holiday");
        cbHoliday.setTextColor(foreground);
        cbHoliday.setChecked(prefs.includeHoliday());
        root.addView(cbHoliday);

        CheckBox cbDoublePay = new CheckBox(context);
        cbDoublePay.setText("Double Pay");
        cbDoublePay.setTextColor(foreground);
        cbDoublePay.setChecked(prefs.includeDoublePay());
        root.addView(cbDoublePay);

        CheckBox cbExtra = new CheckBox(context);
        cbExtra.setText("Extra");
        cbExtra.setTextColor(foreground);
        cbExtra.setChecked(prefs.includeExtra());
        root.addView(cbExtra);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(root);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okBtn.setTextColor(accent);
        okBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                SharedPreferences sp = context.getSharedPreferences("simple_calendar_prefs", Context.MODE_PRIVATE);
                sp.edit().putInt("app_color_hex", prefs.appColor()).apply();

                try {
                    double otMult = Double.parseDouble(etOtMult.getText().toString().trim());
                    prefs.setOvertimeMultiplier(otMult);
                } catch (Exception ex) {
                    prefs.setOvertimeMultiplier(1.25);
                }
                try {
                    double holMult = Double.parseDouble(etHolMult.getText().toString().trim());
                    prefs.setHolidayMultiplier(holMult);
                } catch (Exception ex) {
                    prefs.setHolidayMultiplier(1.30);
                }
                try {
                    double dpMult = Double.parseDouble(etDpMult.getText().toString().trim());
                    prefs.setDoublePayMultiplier(dpMult);
                } catch (Exception ex) {
                    prefs.setDoublePayMultiplier(2.00);
                }
                prefs.setIncludeRegular(cbRegular.isChecked());
                prefs.setIncludeOvertime(cbOvertime.isChecked());
                prefs.setIncludeHoliday(cbHoliday.isChecked());
                prefs.setIncludeDoublePay(cbDoublePay.isChecked());
                prefs.setIncludeExtra(cbExtra.isChecked());
                dialog.dismiss();
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).recreate();
                }
            }
        });

        Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        cancelBtn.setTextColor(muted);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
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
}
