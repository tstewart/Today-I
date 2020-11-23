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

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.sql.DatabaseHelper;

import static java.util.Calendar.getInstance;

public class CalendarViewActivity extends AppCompatActivity {

    MaterialCalendarView calendarView;

    Date selectedDate = new Date();

    List<Date> daysPostedOn;
    HashMap<Date, Integer> ratings;

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

        calendarView = findViewById(R.id.calendarView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        daysPostedOn = getPostedDates();
        ratings = getDaysRated();
    }

    // Return a list of events matching dates that accomplishments have been posted on

    private List<Date> getPostedDates() {

        Database db = new Database(this);
        SQLiteDatabase sqlDb = db.getReadableDatabase();

        List<Date> dates = new ArrayList<>();

        Cursor cursor = sqlDb.rawQuery(DBConstants.ACCOMPLISHMENT_DATE_GROUP_QUERY, null);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                if (dateString != null) {
                    try {
                        Date date = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).parse(dateString);

                        if(date != null) {
                            dates.add(date);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return dates;
    }

    private HashMap<Date, Integer> getDaysRated() {
        SQLiteDatabase db = new DatabaseHelper(DBConstants.RATING_TABLE).getDatabase(getApplicationContext());

        HashMap<Date, Integer> ratings = new HashMap<>();

        Cursor cursor = db.rawQuery(DBConstants.DAY_RATING_ALL_RESULTS_QUERY, null);

        if(cursor.moveToFirst()) {
            do {
                int rating = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                try {
                    Date date = new SimpleDateFormat(DBConstants.DATE_FORMAT, Locale.getDefault()).parse(dateString);
                    if(date != null) {
                        ratings.put(date,rating);
                    }

                } catch (ParseException e) {
                    //TODO MANAGE
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        return ratings;
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
