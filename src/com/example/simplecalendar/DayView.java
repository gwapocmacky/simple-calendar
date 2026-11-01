package com.example.simplecalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class DayView extends LinearLayout {
    private CalendarDate day;
    private OnEventClickListener listener;

    public DayView(Context context) { this(context, null); }
    public DayView(Context context, AttributeSet attrs) { super(context, attrs); setOrientation(VERTICAL); }

    public void setListener(OnEventClickListener l) { this.listener = l; }
    public void setDay(CalendarDate d) { this.day = d; }

    public void rebuild() {
        removeAllViews();
        Context ctx = getContext();
        if (day == null) return;

        int foreground = UiUtils.getColor(ctx, R.color.foreground);
int muted = UiUtils.getColor(ctx, R.color.muted);

        TextView header = new TextView(ctx);
        header.setText(day.fullLabel());
        header.setTextSize(20);
        header.setTextColor(foreground);
        header.setPadding(16, 16, 16, 8);
        addView(header);

        EventStore store = new EventStore(ctx);
        List<Event> events = store.getForDayList(day);
        if (events.isEmpty()) {
            TextView empty = new TextView(ctx);
            empty.setText(ctx.getString(R.string.no_events));
            empty.setTextSize(16);
            empty.setTextColor(muted);
            empty.setPadding(16, 8, 16, 16);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(empty);
            return;
        }

        double grandTotal = 0;
        for (Event e : events) {
            addView(eventRow(ctx, e));
            grandTotal += e.total;
        }

        // grand total row
        LinearLayout totalRow = new LinearLayout(ctx);
        totalRow.setOrientation(LinearLayout.HORIZONTAL);
        totalRow.setPadding(16, 8, 16, 16);
        TextView totalLabel = new TextView(ctx);
        totalLabel.setText("Total for " + day.dayLabel() + ":");
        totalLabel.setTextSize(15);
        totalLabel.setTextColor(foreground);
        totalLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView totalValue = new TextView(ctx);
        totalValue.setText(formatMoney(grandTotal));
        totalValue.setTextSize(16);
        totalValue.setTextColor(UiUtils.getColor(ctx, R.color.income));
        totalValue.setTypeface(null, android.graphics.Typeface.BOLD);
        totalRow.addView(totalLabel);
        totalRow.addView(totalValue);
        addView(totalRow);
    }

    private View eventRow(Context ctx, Event e) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 8, 16, 8);


        View chip = new View(ctx);
        chip.setBackgroundColor(e.color);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(6, LinearLayout.LayoutParams.MATCH_PARENT);
        clp.setMargins(0, 0, 8, 0);
        row.addView(chip, clp);

        LinearLayout textCol = new LinearLayout(ctx);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        title.setText(e.title != null ? e.title : day.fullLabel());
        title.setTextSize(16);
        title.setTextColor(UiUtils.getColor(ctx, R.color.foreground));
        textCol.addView(title);

        StringBuilder breakdown = new StringBuilder();
        if (e.regular > 0) breakdown.append("Reg ").append(formatMoney(e.regular));
        if (e.overtime > 0) {
            if (breakdown.length() > 0) breakdown.append("  ");
            breakdown.append("OT ").append(formatMoney(e.overtime));
        }
        if (e.doublePay > 0) {
            if (breakdown.length() > 0) breakdown.append("  ");
            breakdown.append("2x ").append(formatMoney(e.doublePay));
        }
        if (e.holiday > 0) {
            if (breakdown.length() > 0) breakdown.append("  ");
            breakdown.append("Hol ").append(formatMoney(e.holiday));
        }
        if (e.extra > 0) {
            if (breakdown.length() > 0) breakdown.append("  ");
            breakdown.append("Extra ").append(formatMoney(e.extra));
        }
        if (breakdown.length() == 0) breakdown.append(formatMoney(e.total));

        TextView info = new TextView(ctx);
        info.setText(breakdown.toString());
        info.setTextSize(13);
        info.setTextColor(UiUtils.getColor(ctx, R.color.muted));
        textCol.addView(info);

        row.addView(textCol);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (listener != null) listener.onEventClick(e);
            }
        });
        return row;
    }

    private static String formatMoney(double v) {
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.##");
        return f.format(v);
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}
