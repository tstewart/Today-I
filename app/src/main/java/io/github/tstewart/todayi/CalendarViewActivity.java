package io.github.tstewart.todayi;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.CalendarView;
import android.widget.Toast;

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
        Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
    }

}
