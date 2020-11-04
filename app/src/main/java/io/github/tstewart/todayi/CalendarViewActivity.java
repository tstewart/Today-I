package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.CalendarView;
import android.widget.Toast;

import java.util.GregorianCalendar;

public class CalendarViewActivity extends AppCompatActivity {

    CalendarView datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        datePicker = findViewById(R.id.calendarView);
        datePicker.setOnDateChangeListener(this::onCalendarClick);
    }

    private void onCalendarClick(CalendarView calendarView, int year, int month, int day) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, day);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",gregorianCalendar.getTimeInMillis());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
