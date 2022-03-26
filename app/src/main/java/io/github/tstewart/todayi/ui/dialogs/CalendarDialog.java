package io.github.tstewart.todayi.ui.dialogs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.Database;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;
import io.github.tstewart.todayi.models.DayRating;
import io.github.tstewart.todayi.ui.decorators.DayPostedDecorator;
import io.github.tstewart.todayi.ui.decorators.DayRatedDecorator;
import io.github.tstewart.todayi.ui.decorators.DayRatingSplitter;

public class CalendarDialog extends MaterialAlertDialogBuilder {

    /*
     Log tag, used for Logging
     Represents class name
    */
    private static final String CLASS_LOG_TAG = CalendarDialog.class.getSimpleName();

    /* Activity context */
    private final Context mContext;
    /* This dialog's instance. Set when create is called */
    private AlertDialog mInstance;

    /* Calendar view */
    MaterialCalendarView mCalendarView;

    /* Currently selected date (Application-wide) */
    LocalDate mSelectedDate;

    /* List of days posted on */
    List<CalendarDay> mDaysPostedOn;
    /* HashMap of days rated and their respective rating */
    HashMap<CalendarDay, Integer> mRatings;

    public CalendarDialog(Context context, LocalDate currentDate) {
        super(context);

        this.mContext = context;
        this.mSelectedDate = currentDate;

        /* Inflate dialog layout */
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_calendar, null);
        this.setView(view);

        /* Get calendar view from Layout */
        mCalendarView = view.findViewById(R.id.calendarView);

        /* Get today button view from layout */
        Button todayButton = view.findViewById(R.id.buttonToday);
        if(todayButton != null) {
            todayButton.setOnClickListener(this::onTodayButtonClicked);
        }

        if (mCalendarView != null) {
            /* Set action when a date is selected on the calendar view */
            mCalendarView.setOnDateChangedListener(this::onCalendarClick);
            /*
             Set current date to the selected date
             This defaults to the System's time if no date was provided as an argument when the Activity was created
            */
            mCalendarView.setCurrentDate(mSelectedDate);
            /* Draws a circle around the selected date */
            mCalendarView.setDateSelected(CalendarDay.from(mSelectedDate), true);
        }
    }

    @Override
    public AlertDialog create() {

        AlertDialog dialog = super.create();

        /* Used for onclick control management */
        this.mInstance = dialog;

        /* Get a list of days that Accomplishments were posted on from the Database */
        mDaysPostedOn = getPostedDates();
        /* Get a HashMap of days rated and their ratings from the Database. */
        mRatings = getDaysRated();

        /*
        Decorators are used by MaterialCalendarView to decorate individual day views on the calendar

        In order to bypass an inbuilt restriction that forces every decorator to only accept one view decoration type,
        ratings are separated into a separate decorator instance for every possible rating.
        This is done to ensure that every day view in the calendar is colored to match it's respective rating (e.g. 1 = red, 5 = green etc.)
         */

        /* Get decorator for days posted */
        DayPostedDecorator daysPostedDecorator = new DayPostedDecorator(mDaysPostedOn);
        /* Get list of decorators for every rating */
        List<DayRatedDecorator> dayRatedDecorators = new DayRatingSplitter(mContext).getDayRatingDecorators(mRatings);

        /* Set decorators */
        mCalendarView.addDecorator(daysPostedDecorator);
        mCalendarView.addDecorators(dayRatedDecorators);

        return dialog;
    }

    /**
     * Get days posted on from Database
     * @return A list of events matching dates that Accomplishments have been posted on
     */
    private List<CalendarDay> getPostedDates() {

        Database db = Database.getInstance(mContext);
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

                try {
                    /* Try and parse the date string from Database format to a LocalDate object */
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DBConstants.DATE_FORMAT));

                    /* If successful, the LocalDate object must be converted to an object that MaterialCalendarView understands */
                    if (date != null) {
                        /* Convert LocalDate to CalendarDay */
                        CalendarDay calendarDay = CalendarDay.from(date);

                        /* Add to list of days posted on */
                        dates.add(calendarDay);
                    }

                } catch (DateTimeParseException e) {
                    /* Alert the user that date information may be corrupt in the Database */
                    Toast.makeText(mContext, R.string.gather_dates_failed, Toast.LENGTH_LONG).show();
                    Log.w(CLASS_LOG_TAG, e.getMessage(), e);
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
        HashMap<CalendarDay, Integer> ratings = new HashMap<>();

        /*
         Get a cursor for the SQL query
         Query gets all ratings
        */
        Cursor cursor = new DayRatingTableHelper(mContext).getAll();

        /* If database contains ratings */
        if (cursor.moveToFirst()) {
            do {
                /* Get day rating percent response from Database */
                int ratingPercent = cursor.getInt(cursor.getColumnIndex(DBConstants.COLUMN_RATING));

                int rating = DayRating.percentToRating(ratingPercent);
                /*
                 Get date response from Database
                 Date is represented as a string inside the Database, so it will need to be converted
                */
                String dateString = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_DATE));

                try {
                    /* Try and parse the date string from Database format to a LocalDate object */
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DBConstants.DATE_FORMAT_NO_TIME));

                    /* If successful, the LocalDate object must be converted to an object that MaterialCalendarView understands */
                    if (date != null) {
                        /* Convert Date to CalendarDay */
                        CalendarDay calendarDay = CalendarDay.from(date);

                        /* Add to list of days posted on */
                        ratings.put(calendarDay, rating);
                    }

                } catch (DateTimeParseException e) {
                    /* Alert the user that date information may be corrupt in the Database */
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


    private void onTodayButtonClicked(View view) {
        OnDateChanged.notifyDateChanged(LocalDate.now());
        if(mInstance != null) {
            mInstance.dismiss();
        }
    }

    private void onCalendarClick(MaterialCalendarView view, CalendarDay date, boolean b) {

        /* Get LocalDate from selected date CalendarDay object */
        LocalDate localDate = date.getDate();

        /* Alert subscribers that the date has changed */
        OnDateChanged.notifyDateChanged(localDate);

        /* Close dialog */
        if(mInstance != null) {
            mInstance.dismiss();
        }
    }

}
