package com.elegantcal.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SecretSettingsPanel {
    private final Context context;
    private final Prefs prefs;
    private final Runnable onSaved;

    public SecretSettingsPanel(Context context, Prefs prefs, Runnable onSaved) {
        this.context = context;
        this.prefs = prefs;
        this.onSaved = onSaved;
    }

    public void show() {
        final float density = context.getResources().getDisplayMetrics().density;
        final boolean isDark = prefs.isDarkMode();
        final int accent = prefs.getAccentColor();

        int dialogBg = isDark ? 0xff12151c : 0xffffffff;
        int cardBg = isDark ? 0xff1a1e28 : 0xfff8fafc;
        int cardStroke = isDark ? 0xff282c38 : 0xffe2e8f0;
        int inputBg = isDark ? 0xff242836 : 0xffffffff;
        int inputStroke = isDark ? 0xff3b4052 : 0xffcbd5e1;

        int textPrimary = isDark ? 0xfff1f5f9 : 0xff0f172a;
        int textSecondary = isDark ? 0xff94a3b8 : 0xff64748b;
        int textMuted = isDark ? 0xff64748b : 0xff94a3b8;

        ScrollView scroll = new ScrollView(context);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding((int) (22 * density), (int) (22 * density), (int) (22 * density), (int) (22 * density));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setShape(GradientDrawable.RECTANGLE);
        rootBg.setColor(dialogBg);
        rootBg.setCornerRadius(20 * density);
        root.setBackground(rootBg);

        scroll.addView(root);

        // Header
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, (int) (4 * density));

        View accentPill = new View(context);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setShape(GradientDrawable.RECTANGLE);
        pillBg.setColor(accent);
        pillBg.setCornerRadius(3 * density);
        accentPill.setBackground(pillBg);
        LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams((int) (4 * density), (int) (26 * density));
        pillLp.rightMargin = (int) (12 * density);
        accentPill.setLayoutParams(pillLp);
        headerRow.addView(accentPill);

        LinearLayout titleCol = new LinearLayout(context);
        titleCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Secret Settings");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(textPrimary);
        titleCol.addView(tvTitle);

        TextView tvSubtitle = new TextView(context);
        tvSubtitle.setText("Pay Date rate multipliers & formula controls");
        tvSubtitle.setTextSize(11);
        tvSubtitle.setTextColor(textSecondary);
        titleCol.addView(tvSubtitle);

        headerRow.addView(titleCol);
        root.addView(headerRow);

        // Divider
        View div1 = new View(context);
        div1.setBackgroundColor(cardStroke);
        LinearLayout.LayoutParams dlp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dlp1.setMargins(0, (int) (12 * density), 0, (int) (16 * density));
        div1.setLayoutParams(dlp1);
        root.addView(div1);

        // Section 1: Multipliers Card
        LinearLayout multCard = new LinearLayout(context);
        multCard.setOrientation(LinearLayout.VERTICAL);
        multCard.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (14 * density));

        GradientDrawable mcBg = new GradientDrawable();
        mcBg.setShape(GradientDrawable.RECTANGLE);
        mcBg.setColor(cardBg);
        mcBg.setCornerRadius(14 * density);
        mcBg.setStroke((int) (1 * density), cardStroke);
        multCard.setBackground(mcBg);

        TextView tvMultHeader = new TextView(context);
        tvMultHeader.setText("Rate Multipliers");
        tvMultHeader.setTextSize(14);
        tvMultHeader.setTypeface(null, Typeface.BOLD);
        tvMultHeader.setTextColor(textPrimary);
        tvMultHeader.setPadding(0, 0, 0, (int) (10 * density));
        multCard.addView(tvMultHeader);

        // Overtime Row
        final EditText etOtMult = createMultiplierRow(context, multCard,
                "Overtime", "Regular ÷ 8 × 1.25 × mult",
                String.valueOf(prefs.overtimeMultiplier()),
                textPrimary, textMuted, inputBg, inputStroke, density);

        // Holiday Row
        final EditText etHolMult = createMultiplierRow(context, multCard,
                "Holiday", "Regular × mult",
                String.valueOf(prefs.holidayMultiplier()),
                textPrimary, textMuted, inputBg, inputStroke, density);

        // Double Pay Row
        final EditText etDpMult = createMultiplierRow(context, multCard,
                "Double Pay", "Regular × mult",
                String.valueOf(prefs.doublePayMultiplier()),
                textPrimary, textMuted, inputBg, inputStroke, density);

        root.addView(multCard);

        // Section 2: Include in Total Card
        LinearLayout incCard = new LinearLayout(context);
        incCard.setOrientation(LinearLayout.VERTICAL);
        incCard.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (14 * density));
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        icLp.setMargins(0, (int) (14 * density), 0, 0);
        incCard.setLayoutParams(icLp);

        GradientDrawable icBg = new GradientDrawable();
        icBg.setShape(GradientDrawable.RECTANGLE);
        icBg.setColor(cardBg);
        icBg.setCornerRadius(14 * density);
        icBg.setStroke((int) (1 * density), cardStroke);
        incCard.setBackground(icBg);

        TextView tvIncHeader = new TextView(context);
        tvIncHeader.setText("Include in Monthly & Day Totals");
        tvIncHeader.setTextSize(14);
        tvIncHeader.setTypeface(null, Typeface.BOLD);
        tvIncHeader.setTextColor(textPrimary);
        tvIncHeader.setPadding(0, 0, 0, (int) (8 * density));
        incCard.addView(tvIncHeader);

        final CheckBox cbRegular = createCheckBox(context, "Regular Pay", prefs.includeRegular(), textPrimary, accent);
        final CheckBox cbOvertime = createCheckBox(context, "Overtime Earnings", prefs.includeOvertime(), textPrimary, accent);
        final CheckBox cbHoliday = createCheckBox(context, "Holiday Earnings", prefs.includeHoliday(), textPrimary, accent);
        final CheckBox cbDoublePay = createCheckBox(context, "Double Pay Earnings", prefs.includeDoublePay(), textPrimary, accent);
        final CheckBox cbExtra = createCheckBox(context, "Extra / Bonus Earnings", prefs.includeExtra(), textPrimary, accent);

        incCard.addView(cbRegular);
        incCard.addView(cbOvertime);
        incCard.addView(cbHoliday);
        incCard.addView(cbDoublePay);
        incCard.addView(cbExtra);

        root.addView(incCard);

        // Action Buttons Row
        LinearLayout actionsRow = new LinearLayout(context);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        actLp.setMargins(0, (int) (18 * density), 0, 0);
        actionsRow.setLayoutParams(actLp);

        Button btnCancel = new Button(context);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(13);
        btnCancel.setTextColor(textSecondary);
        btnCancel.setBackgroundColor(Color.TRANSPARENT);

        Button btnApply = new Button(context);
        btnApply.setText("Save Settings");
        btnApply.setTextSize(13);
        btnApply.setTypeface(null, Typeface.BOLD);
        btnApply.setTextColor(Color.WHITE);

        GradientDrawable applyBg = new GradientDrawable();
        applyBg.setShape(GradientDrawable.RECTANGLE);
        applyBg.setColor(accent);
        applyBg.setCornerRadius(10 * density);
        btnApply.setBackground(applyBg);
        btnApply.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), (int) (8 * density));

        actionsRow.addView(btnCancel);

        View gap = new View(context);
        gap.setLayoutParams(new LinearLayout.LayoutParams((int) (8 * density), 1));
        actionsRow.addView(gap);

        actionsRow.addView(btnApply);
        root.addView(actionsRow);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(scroll);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double ot = Double.parseDouble(etOtMult.getText().toString().trim());
                    prefs.setOvertimeMultiplier(ot);
                } catch (Exception ex) {
                    prefs.setOvertimeMultiplier(1.25);
                }
                try {
                    double hol = Double.parseDouble(etHolMult.getText().toString().trim());
                    prefs.setHolidayMultiplier(hol);
                } catch (Exception ex) {
                    prefs.setHolidayMultiplier(1.30);
                }
                try {
                    double dp = Double.parseDouble(etDpMult.getText().toString().trim());
                    prefs.setDoublePayMultiplier(dp);
                } catch (Exception ex) {
                    prefs.setDoublePayMultiplier(2.00);
                }

                prefs.setIncludeRegular(cbRegular.isChecked());
                prefs.setIncludeOvertime(cbOvertime.isChecked());
                prefs.setIncludeHoliday(cbHoliday.isChecked());
                prefs.setIncludeDoublePay(cbDoublePay.isChecked());
                prefs.setIncludeExtra(cbExtra.isChecked());

                dialog.dismiss();

                if (onSaved != null) {
                    onSaved.run();
                }
            }
        });
    }

    private static EditText createMultiplierRow(Context ctx, LinearLayout parent,
                                                 String label, String formula, String initialVal,
                                                 int textColor, int textMuted, int inputBg, int inputStroke, float density) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, (int) (4 * density), 0, (int) (6 * density));

        LinearLayout labelCol = new LinearLayout(ctx);
        labelCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lcp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelCol.setLayoutParams(lcp);

        TextView tvLabel = new TextView(ctx);
        tvLabel.setText(label);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setTextColor(textColor);
        labelCol.addView(tvLabel);

        TextView tvFormula = new TextView(ctx);
        tvFormula.setText(formula);
        tvFormula.setTextSize(11);
        tvFormula.setTextColor(textMuted);
        labelCol.addView(tvFormula);

        row.addView(labelCol);

        EditText et = new EditText(ctx);
        et.setText(initialVal);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setTextColor(textColor);
        et.setTextSize(14);
        et.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(inputBg);
        bg.setCornerRadius(8 * density);
        bg.setStroke((int) (1 * density), inputStroke);
        et.setBackground(bg);

        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams((int) (70 * density), (int) (40 * density));
        et.setLayoutParams(etLp);

        row.addView(et);
        parent.addView(row);
        return et;
    }

    private static CheckBox createCheckBox(Context ctx, String label, boolean checked, int textColor, int accent) {
        CheckBox cb = new CheckBox(ctx);
        cb.setText(label);
        cb.setTextSize(13);
        cb.setTextColor(textColor);
        cb.setChecked(checked);
        return cb;
    }
}
