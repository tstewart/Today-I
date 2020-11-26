package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.sql.DatabaseHelper;
import io.github.tstewart.todayi.ui.decorator.DayPostedDecorator;
import io.github.tstewart.todayi.ui.decorator.DayRatedDecorator;
import io.github.tstewart.todayi.ui.decorator.DayRatingSplitter;
import io.github.tstewart.todayi.utils.DateFormatter;


public class CalendarActivity extends AppCompatActivity {
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    MaterialCalendarView calendarView;

    Date selectedDate = new Date();

    List<CalendarDay> daysPostedOn;
    HashMap<CalendarDay, Integer> ratings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long time = extras.getLong("selectedDate");
            if (time > 0) {
                selectedDate.setTime(time);
            }
        }

        calendarView = findViewById(R.id.calendarView);

        if(calendarView != null) {
            calendarView.setOnDateChangedListener(this::onCalendarClick);
            calendarView.setCurrentDate(getCalendarDayFromDate(selectedDate));
            calendarView.setDateSelected(getCalendarDayFromDate(selectedDate), true);
        }

        if(getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.activity_calendar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        daysPostedOn = getPostedDates();
        ratings = getDaysRated();

        DayPostedDecorator daysPostedDecorator = new DayPostedDecorator(daysPostedOn);
        List<DayRatedDecorator> dayRatedDecorators = new DayRatingSplitter(this).getDayRatingDecorators(ratings);

        calendarView.addDecorator(daysPostedDecorator);
        calendarView.addDecorators(dayRatedDecorators);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            returnResponseCancelled();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Return a list of events matching dates that accomplishments have been posted on

    private List<CalendarDay> getPostedDates() {

        Database db = new Database(this);
        SQLiteDatabase sqlDb = db.getReadableDatabase();

        List<CalendarDay> dates = new ArrayList<>();

        Cursor cursor = sqlDb.rawQuery(DBConstants.ACCOMPLISHMENT_DATE_GROUP_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                if (dateString != null) {
                    try {
                        Date date = new DateFormatter(DBConstants.DATE_FORMAT).parse(dateString);

                        if(date != null) {
                            CalendarDay calendarDay = getCalendarDayFromDate(date);

                            if(calendarDay != null) {
                                dates.add(calendarDay);
                            }
                        }

                    } catch (ParseException e) {
                        Toast.makeText(this,"Failed to gather dates posted on. Database may be corrupt.", Toast.LENGTH_LONG).show();
                        Log.w(CLASS_LOG_TAG,e.getMessage(), e);
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return dates;
    }

    private HashMap<CalendarDay, Integer> getDaysRated() {
        SQLiteDatabase db = new DatabaseHelper(DBConstants.RATING_TABLE).getDatabase(getApplicationContext());

        HashMap<CalendarDay, Integer> ratings = new HashMap<>();

        Cursor cursor = db.rawQuery(DBConstants.DAY_RATING_ALL_RESULTS_QUERY, null);

        if(cursor.moveToFirst()) {
            do {
                int rating = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                try {
                    Date date = new DateFormatter(DBConstants.DATE_FORMAT).parse(dateString);
                    if(date != null) {
                        CalendarDay calendarDay = getCalendarDayFromDate(date);

                        if(calendarDay != null) {
                            ratings.put(calendarDay, rating);
                        }
                    }

                } catch (ParseException e) {
                    Toast.makeText(this,"Failed to gather ratings. Database may be corrupt.", Toast.LENGTH_LONG).show();
                    Log.w(CLASS_LOG_TAG,e.getMessage(), e);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        return ratings;
    }

    private CalendarDay getCalendarDayFromDate(Date date) {
        if(date != null) {
            LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

            return CalendarDay.from(localDate);
        }
        return null;
    }

    private void onCalendarClick(MaterialCalendarView view, CalendarDay date, boolean b) {

        LocalDate localDate = date.getDate();

        long epochDay = localDate.toEpochDay();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", epochDay);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void returnResponseCancelled() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }
}
