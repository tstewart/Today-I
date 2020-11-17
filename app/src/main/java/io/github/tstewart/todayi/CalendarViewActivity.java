package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;

import static java.util.Calendar.getInstance;

public class CalendarViewActivity extends AppCompatActivity {

    CompactCalendarView datePicker;
    CompactCalendarView.CompactCalendarViewListener calendarEventListener;

    Date selectedDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long time = extras.getLong("selectedDate");
            if (time > 0) {
                selectedDate.setTime(time);
            }
        }

        TextView currentMonthTv = findViewById(R.id.textViewSelectedMonth);
        currentMonthTv.setText(getMonthYearFormatted(selectedDate));

        datePicker = findViewById(R.id.calendarView);

        if (datePicker != null) {
            datePicker.shouldDrawIndicatorsBelowSelectedDays(true);
            datePicker.setCurrentDate(selectedDate);

            calendarEventListener = new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {
                    onCalendarClick(dateClicked);
                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth) {
                    currentMonthTv.setText(getMonthYearFormatted(firstDayOfNewMonth));
                }
            };

            datePicker.setListener(calendarEventListener);
        }

        Button prevMonthButton = findViewById(R.id.buttonPrevMonth);
        Button nextMonthButton = findViewById(R.id.buttonNextMonth);

        if (prevMonthButton != null) {
            prevMonthButton.setOnClickListener(this::onChangeMonthButtonClick);
        }
        if (nextMonthButton != null) {
            nextMonthButton.setOnClickListener(this::onChangeMonthButtonClick);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(datePicker != null) datePicker.addEvents(getPostedDateEvents());
    }

    // Return a list of events matching dates that accomplishments have been posted on
    private List<Event> getPostedDateEvents() {

        Database db = new Database(this);
        SQLiteDatabase sqlDb = db.getReadableDatabase();

        List<Event> events = new ArrayList<>();

        Cursor cursor = sqlDb.rawQuery(DBConstants.ACCOMPLISHMENT_DATE_GROUP_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                if (dateString != null) {
                    try {
                        Date date = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).parse(dateString);

                        if(date != null) {
                            events.add(new Event(Color.BLACK, date.getTime()));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return events;
    }

    private void onChangeMonthButtonClick(View view) {

        Calendar calendar = getInstance();
        calendar.setTime(selectedDate);

        if (view.getId() == R.id.buttonPrevMonth) {
            calendar.add(Calendar.MONTH, -1);
        } else if (view.getId() == R.id.buttonNextMonth) {
            calendar.add(Calendar.MONTH, 1);
        }

        selectedDate = calendar.getTime();

        if (datePicker != null) {
            datePicker.setCurrentDate(selectedDate);
            if (calendarEventListener != null) {
                calendarEventListener.onMonthScroll(datePicker.getFirstDayOfCurrentMonth());
            }
        }
    }

    private void onCalendarClick(Date date) {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", date.getTime());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private String getMonthYearFormatted(@NonNull Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM YYYY", Locale.getDefault());
        return sdf.format(date);
    }

}
