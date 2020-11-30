package io.github.tstewart.todayi.ui.activities;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.helpers.DatabaseHelper;
import io.github.tstewart.todayi.ui.decorators.DayPostedDecorator;
import io.github.tstewart.todayi.ui.decorators.DayRatedDecorator;
import io.github.tstewart.todayi.ui.decorators.DayRatingSplitter;
import io.github.tstewart.todayi.helpers.DateFormatter;

/*
    Provides a calendar that the user can use to select a date to instantly jump to a date
    Also provides an overview of days that Accomplishments were posted on and days rated
 */
public class CalendarActivity extends AppCompatActivity {
    // Log tag, used for Logging
    // Represents class name
    private final String CLASS_LOG_TAG = this.getClass().getSimpleName();

    // Calendar view
    MaterialCalendarView mCalendarView;

    // Currently selected date (Application-wide, controlled by OnDateChangedListener)
    Date mSelectedDate = new Date();

    // List of days posted on
    List<CalendarDay> mDaysPostedOn;
    // HashMap of days rated and their respective rating
    HashMap<CalendarDay, Integer> mRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Check if this Activity was instantiated with a selected date
        // If it was, set the current date to the provided date
        // This will be instantiated with the calendar view later, so that the calendar view automatically scrolls to the current month
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long time = extras.getLong("selectedDate");
            if (time > 0) {
                mSelectedDate.setTime(time);
            }
        }
        // If no date was provided with the Activity launch, set the current date to the System's time
        else {
            mSelectedDate.setTime(System.currentTimeMillis());
        }

        // Get calendar view from Layout
        mCalendarView = findViewById(R.id.calendarView);

        if (mCalendarView != null) {
            // Set action when a date is selected on the calendar view
            mCalendarView.setOnDateChangedListener(this::onCalendarClick);
            // Set current date to the selected date
            // This defaults to the System's time if no date was provided as an argument when the Activity was created
            mCalendarView.setCurrentDate(getCalendarDayFromDate(mSelectedDate));
            // Draws a circle around the selected date
            mCalendarView.setDateSelected(getCalendarDayFromDate(mSelectedDate), true);
        }

        // Get the top bar, and set it's title correctly
        ActionBar supportBar = getSupportActionBar();
        if (supportBar != null) supportBar.setTitle(R.string.activity_calendar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Get a list of days that Accomplishments were posted on from the Database
        mDaysPostedOn = getPostedDates();
        // Get a HashMap of days rated and their ratings from the Database.
        mRatings = getDaysRated();

        /*
        Decorators are used by MaterialCalendarView to decorate individual day views on the calendar

        In order to bypass an inbuilt restriction that forces every decorator to only accept one view decoration type,
        ratings are separated into a separate decorator instance for every possible rating.
        This is done to ensure that every day view in the calendar is colored to match it's respective rating (e.g. 1 = red, 5 = green etc.)
         */

        // Get decorator for days posted
        DayPostedDecorator daysPostedDecorator = new DayPostedDecorator(mDaysPostedOn);
        // Get list of decorators for every rating
        List<DayRatedDecorator> dayRatedDecorators = new DayRatingSplitter(this).getDayRatingDecorators(mRatings);

        // Set decorators
        mCalendarView.addDecorator(daysPostedDecorator);
        mCalendarView.addDecorators(dayRatedDecorators);
    }

    /* Called if a button is pressed in the top bar */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* If the item selected was the "back" button */
        if (item.getItemId() == android.R.id.home) {
            /* Alert the Activity that launched this Activity that it will not receive a response. */
            returnResponseCancelled();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get days posted on from Database
     * @return A list of events matching dates that Accomplishments have been posted on
     */
    private List<CalendarDay> getPostedDates() {

        Database db = new Database(this);
        SQLiteDatabase sqlDb = db.getReadableDatabase();

        List<CalendarDay> dates = new ArrayList<>();

        /*
         Get a cursor for the SQL query
         Query gets all days posted on
        */
        Cursor cursor = sqlDb.rawQuery(DBConstants.ACCOMPLISHMENT_DATE_GROUP_QUERY, null);

        /* If cursor contains responses */
        if (cursor.moveToFirst()) {
            do {
                /*
                 Get date response from Database
                 Date is represented as a string inside the Database, so it will need to be converted
                */
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                if (dateString != null) {
                    try {
                        /* Try and parse the date string from Database format to a Date object */
                        Date date = new DateFormatter(DBConstants.DATE_FORMAT).parse(dateString);

                        /* If successful, the Date object must be converted to an object that MaterialCalendarView understands */
                        if (date != null) {
                            /* Convert Date to CalendarDay */
                            CalendarDay calendarDay = getCalendarDayFromDate(date);

                            /* If successful, add to list of days posted on */
                            if (calendarDay != null) {
                                dates.add(calendarDay);
                            }
                        }

                    } catch (ParseException e) {
                        /* Alert the user that date information may be corrupt in the Database */
                        Toast.makeText(this, "Failed to gather dates posted on. Database may be corrupt.", Toast.LENGTH_LONG).show();
                        Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                    }
                }
            }
            /* Keep getting responses while it is possible to do so. */
            while (cursor.moveToNext());
        }

        /* Close Database connection */
        cursor.close();

        return dates;
    }

    /**
     * Get days rated from Database
     * @return A HashMap of days rated and their ratings
     */
    private HashMap<CalendarDay, Integer> getDaysRated() {
        SQLiteDatabase db = new DatabaseHelper(DBConstants.RATING_TABLE).getDatabase(getApplicationContext());

        HashMap<CalendarDay, Integer> ratings = new HashMap<>();

        /*
         Get a cursor for the SQL query
         Query gets all ratings
        */
        Cursor cursor = db.rawQuery(DBConstants.DAY_RATING_ALL_RESULTS_QUERY, null);

        /* If database contains ratings */
        if (cursor.moveToFirst()) {
            do {
                /* Get day rating response from Database */
                int rating = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
                /*
                 Get date response from Database
                 Date is represented as a string inside the Database, so it will need to be converted
                */
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                try {
                    /* Try and parse the date string from Database format to a Date object */
                    Date date = new DateFormatter(DBConstants.DATE_FORMAT).parse(dateString);

                    /* If successful, the Date object must be converted to an object that MaterialCalendarView understands */
                    if (date != null) {
                        /* Convert Date to CalendarDay */
                        CalendarDay calendarDay = getCalendarDayFromDate(date);

                        /* If successful, add to list of days posted on */
                        if (calendarDay != null) {
                            ratings.put(calendarDay, rating);
                        }
                    }

                } catch (ParseException e) {
                    /* Alert the user that date information may be corrupt in the Database */
                    Toast.makeText(this, "Failed to gather ratings. Database may be corrupt.", Toast.LENGTH_LONG).show();
                    Log.w(CLASS_LOG_TAG, e.getMessage(), e);
                }

            }
            /* Keep getting responses while it is possible to do so. */
            while (cursor.moveToNext());
        }

        /* Close Database connection */
        cursor.close();

        return ratings;
    }

    /**
     * Convert Date to CalendarDay
     * @param date Date object to convert
     * @return CalendarDay instance
     */
    private CalendarDay getCalendarDayFromDate(Date date) {
        if (date != null) {
            /* First, we need to convert the Date object to a LocalDate object */
            LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            /* CalendarDay provides the functionality to convert from LocalDate to CalendarDay */
            return CalendarDay.from(localDate);
        }
        return null;
    }

    private void onCalendarClick(MaterialCalendarView view, CalendarDay date, boolean b) {

        /* Get LocalDate from selected date CalendarDay object */
        LocalDate localDate = date.getDate();

        /* Get time since epoch from selected day */
        long epochDay = localDate.toEpochDay();

        /* Return to parent Activity with selected day result */
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", epochDay);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /*
    Alert parent Activity that the request for a response was cancelled
     */
    private void returnResponseCancelled() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
