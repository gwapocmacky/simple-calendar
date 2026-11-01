package com.example.simplecalendar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class WeekView extends LinearLayout {
    private CalendarDate[] days = new CalendarDate[7];
    private CalendarDate selected;
    private OnDayClickListener dayListener;
    private OnEventClickListener eventListener;

    public WeekView(Context context) { this(context, null); }
    public WeekView(Context context, AttributeSet attrs) { super(context, attrs); setOrientation(HORIZONTAL); }

    public void setListeners(OnDayClickListener dl, OnEventClickListener el) {
        this.dayListener = dl;
        this.eventListener = el;
    }

    public void setWeek(CalendarDate start) {
        CalendarDate c = start;
        for (int i = 0; i < 7; i++) {
            days[i] = c;
            c = plusOneDay(c);
        }
        rebuild();
    }

    public void setSelected(CalendarDate d) {
        this.selected = d;
        rebuild();
    }

    private CalendarDate plusOneDay(CalendarDate d) {
        Calendar cal = Calendar.getInstance();
        cal.set(d.year, d.month, d.day);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return new CalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    private void rebuild() {
        removeAllViews();
        Context ctx = getContext();
        for (int i = 0; i < 7; i++) {
            addView(dayColumn(ctx, days[i]));
        }
    }

    private View dayColumn(Context ctx, CalendarDate d) {
        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        col.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        col.setPadding(4, 4, 4, 4);
        boolean sel = selected != null && selected.isSameDay(d);
        boolean todayFlag = CalendarDate.today().isSameDay(d);
        int todayColor = UiUtils.getColor(ctx, R.color.today);
int selectedColor = UiUtils.getColor(ctx, R.color.selected_day);
        col.setBackgroundColor(sel ? selectedColor : (todayFlag ? todayColor : Color.TRANSPARENT));

        TextView wday = new TextView(ctx);
        wday.setText(d.weekdayShort());
        wday.setTextSize(13);
        wday.setTextColor(UiUtils.getColor(ctx, R.color.muted));
        col.addView(wday);

        TextView num = new TextView(ctx);
        num.setText(String.valueOf(d.day));
        num.setTextSize(20);
        int accent = UiUtils.getColor(ctx, R.color.accent);
int foreground = UiUtils.getColor(ctx, R.color.foreground);
        num.setTextColor(sel || todayFlag ? accent : foreground);
        col.addView(num);

        EventStore store = new EventStore(ctx);
        List<Event> events = store.getForDayList(d);
        for (Event e : events) {
            TextView ev = new TextView(ctx);
            ev.setText(e.title);
            ev.setTextSize(11);
            ev.setMaxLines(1);
            ev.setEllipsize(android.text.TextUtils.TruncateAt.END);
            ev.setPadding(4, 2, 4, 2);
            ev.setBackgroundColor(e.color);
            ev.setTextColor(UiUtils.getColor(ctx, R.color.white));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 2;
            col.addView(ev, lp);
        }

        col.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (dayListener != null) dayListener.onDayClick(d);
                setSelected(d);
            }
        });
        return col;
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarDate day);
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}
