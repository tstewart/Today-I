package io.github.tstewart.todayi;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Date;

public class CalenderViewActivity extends AppCompatActivity {

    CalendarView datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender_view);

        datePicker = findViewById(R.id.calendarView);
        datePicker.setOnDateChangeListener(this::onCalenderClick);
    }

    private void onCalenderClick(CalendarView calendarView, int year, int month, int day) {
        Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
    }

}
