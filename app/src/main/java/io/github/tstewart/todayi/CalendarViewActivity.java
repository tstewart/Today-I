package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import io.github.tstewart.todayi.sql.DatabaseHelper;

import static com.github.sundeepk.compactcalendarview.CompactCalendarView.FILL_LARGE_INDICATOR;
import static com.github.sundeepk.compactcalendarview.CompactCalendarView.SMALL_INDICATOR;
import static java.util.Calendar.getInstance;
import static com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener;

public class CalendarViewActivity extends AppCompatActivity {

    CompactCalendarView datePicker;
    CompactCalendarViewListener calendarEventListener;

    List<Event> daysPostedOn = new ArrayList<>();
    List<Event> daysRated = new ArrayList<>();

    Date selectedDate = new Date();

    CalendarType calendarType = CalendarType.ACCOMPLISHMENTS;

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
        Button changeCalendarTypeButton = findViewById(R.id.buttonChangeCalendarType);

        if (prevMonthButton != null) {
            prevMonthButton.setOnClickListener(this::onChangeMonthButtonClick);
        }
        if (nextMonthButton != null) {
            nextMonthButton.setOnClickListener(this::onChangeMonthButtonClick);
        }
        if(changeCalendarTypeButton != null) {
            changeCalendarTypeButton.setOnClickListener(this::onChangeCalendarTypeButtonClick);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        daysPostedOn = getPostedDateEvents();
        daysRated = getDaysRatedEvents();
        if(datePicker != null) {
            datePicker.setCurrentSelectedDayBackgroundColor(getColor(R.color.colorTransparent));
            datePicker.setCurrentDayBackgroundColor(getColor(R.color.colorAccent));
            datePicker.addEvents(getPostedDateEvents());
        }
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

    private List<Event> getDaysRatedEvents() {
        SQLiteDatabase db = new DatabaseHelper(DBConstants.RATING_TABLE).getDatabase(getApplicationContext());

        List<Event> events = new ArrayList<>();

        Cursor cursor = db.rawQuery(DBConstants.DAY_RATING_ALL_RESULTS_QUERY, null);

        if(cursor.moveToFirst()) {
            do {
                int rating = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                try {
                    Date date = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).parse(dateString);

                    // Default value is transparent
                    int color = getColor(R.color.colorTransparent);

                    switch(rating) {
                        case 1:
                            color = getColor(R.color.colorRatingRed);
                            break;
                        case 2:
                            color = getColor(R.color.colorRatingOrange);
                            break;
                        case 3:
                            color = getColor(R.color.colorRatingYellow);
                            break;
                        case 4:
                            color = getColor(R.color.colorRatingLightGreen);
                            break;
                        case 5:
                            color = getColor(R.color.colorRatingGreen);
                            break;
                        default:
                            break;
                    }

                    if(date != null) {
                        Event event = new Event(color,date.getTime());
                        events.add(event);
                    }

                } catch (ParseException e) {
                    //TODO MANAGE
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        events.forEach(element -> {
            Log.i("olo", element.toString());
        });

        return events;
    }

    private void onChangeCalendarTypeButtonClick(View view) {
        if(datePicker != null) {
            if (calendarType == CalendarType.ACCOMPLISHMENTS) {
                calendarType = CalendarType.RATINGS;

                datePicker.removeAllEvents();
                datePicker.setEventIndicatorStyle(FILL_LARGE_INDICATOR);
                datePicker.setCurrentDayIndicatorStyle(SMALL_INDICATOR);
                datePicker.addEvents(daysRated);
            } else {
                calendarType = CalendarType.ACCOMPLISHMENTS;

                datePicker.removeAllEvents();
                datePicker.setEventIndicatorStyle(SMALL_INDICATOR);
                datePicker.setCurrentDayIndicatorStyle(FILL_LARGE_INDICATOR);
                datePicker.addEvents(daysPostedOn);
            }
        }
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


    private enum CalendarType {
        ACCOMPLISHMENTS,
        RATINGS
    }
}
