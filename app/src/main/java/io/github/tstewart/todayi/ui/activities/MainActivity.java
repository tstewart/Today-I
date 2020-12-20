package io.github.tstewart.todayi.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.RelativeDateHelper;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.notifications.DailyReminderAlarmHelper;
import io.github.tstewart.todayi.ui.fragments.AccomplishmentListFragment;
import io.github.tstewart.todayi.ui.tutorials.MainActivityTutorial;

/*
Main Activity of the application (obviously), handles AccomplishmentListFragment and DayRatingFragment functionality
 */
public class MainActivity extends AppCompatActivity implements OnDateChangedListener {

    /* Used when requesting a response from CalendarActivity */
    private static final int CALENDAR_ACTIVITY_REQUEST_CODE = 1;
    /* Used when requesting a response from OptionsActivity */
    private static final int OPTIONS_ACTIVITY_REQUEST_CODE = 2;

    /* Currently selected date (Application-wide, controlled by OnDateChangedListener) */
    LocalDate mSelectedDate;

    /* Fragment that contains functionality for viewing, creating, editing, and deleting Accomplishments */
    AccomplishmentListFragment mListFragment;

    /* Parent layout for day/relative day labels */
    LinearLayout mLayoutDayLabel;

    /* Text label, shows current date formatted */
    TextView mDayLabel;

    /* Text label, shows current date in a relative time span from system date */
    TextView mRelativeDayLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get bottom bar buttons for controlling date */
        ImageButton prevButton = findViewById(R.id.buttonPrevDay);
        ImageButton nextButton = findViewById(R.id.buttonNextDay);
        mLayoutDayLabel = findViewById(R.id.linearLayoutDayLabel);
        mDayLabel = findViewById(R.id.textViewCurrentDate);
        mRelativeDayLabel = findViewById(R.id.textViewRelativeDay);

        /* Set functionality of bottom bar buttons */
        if(prevButton != null)
            prevButton.setOnClickListener(this::onDayChangeButtonClicked);
        if(nextButton != null)
            nextButton.setOnClickListener(this::onDayChangeButtonClicked);
        if(mLayoutDayLabel != null)
            /* If day label (parent layout) is long pressed, reset current day */
            mLayoutDayLabel.setOnLongClickListener(this::onDayLabelLongPressed);

        /* Register for date changed events */
        OnDateChanged.addListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /* Search currently registered fragments on this Activity for instance of AccomplishmentListFragment */
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof AccomplishmentListFragment) {
                this.mListFragment = (AccomplishmentListFragment) fragment;
                break;
            }
        }

        /* Set current date to System's current date */
        updateCurrentDate(LocalDate.now());

        /*
        Show tutorial after a set period of time
        This is done to ensure all views have been initialised before the tutorial is shown
         */
        MainActivity thisActivity = this;
        new Handler().postDelayed(() -> new MainActivityTutorial().showTutorial(thisActivity), 200);
    }

    /* Inflate Main Activity's top bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* If an item on the top bar is selected */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        Intent targetIntent = null;
        int requestCode = 0;

        /*
         Depending on the selected item, set the Intent Activity, and the request code
         Request code will be used later on to determine the response from the Activity
        */
        if (itemId == R.id.toolbar_calendar) {
            targetIntent = new Intent(this, CalendarActivity.class);
            requestCode = CALENDAR_ACTIVITY_REQUEST_CODE;
            /* CalendarView is initialised with the current selected date as an argument */
            targetIntent.putExtra("selectedDate", mSelectedDate.toEpochDay());

        } else if (itemId == R.id.toolbar_settings) {
            targetIntent = new Intent(this, OptionsActivity.class);
            requestCode = OPTIONS_ACTIVITY_REQUEST_CODE;
        }

        /*
         Start new activity and await response
         Calendar will respond when a date is selected
         Options will respond when an option causes a database refresh
        */
        if (targetIntent != null) startActivityForResult(targetIntent, requestCode);

        return super.onOptionsItemSelected(item);
    }

    /* On receiving a response from an Activity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* If the calendar was the Activity that responded */
        if (requestCode == CALENDAR_ACTIVITY_REQUEST_CODE) {
            /* If the response was Ok, and data was provided with the response */
            if (resultCode == Activity.RESULT_OK && data != null) {
                long dateResult = data.getLongExtra("result", -1);

                /* If the date result was not the default value (-1) */
                if (dateResult >= 0) {
                    LocalDate selectedDate = LocalDate.ofEpochDay(dateResult);
                    updateCurrentDate(selectedDate);
                }
            }
        }
        /* If the response was Ok, settings activity forces reset of accomplishments */
        else if (requestCode == OPTIONS_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            /*
             Options Activities responses trigger Database interaction notification
             For example, when Erase Data is called from Options, the database must now refresh,
             as data has been cleared
            */
            OnDatabaseInteracted.notifyDatabaseInteracted();
        }
    }

    /*
     Notifies all subscribers to OnDatabaseInteracted that the selected date has been changed
     */
    void updateCurrentDate(@NonNull LocalDate date) {
        OnDateChanged.notifyDateChanged(date);
    }

    /*
    When one of the bottom bar buttons (Next, Today, Previous) are pressed
     */
    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        LocalDate newDate = mSelectedDate;

        /* If selected button was Next or Previous, add or subtract one day from current */
        if (viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            if (viewId == R.id.buttonPrevDay) {
                newDate = newDate.minusDays(1);
            } else {
                newDate = newDate.plusDays(1);
            }
        }
        updateCurrentDate(newDate);

        /* Dismiss accomplishment fragment dialog if exists */
        if (mListFragment != null) mListFragment.dismissCurrentDialog();

    }


    /* Reset day to today on long press day label */
    private boolean onDayLabelLongPressed(View view) {
        updateCurrentDate(LocalDate.now());
        return true;
    }

    @Override
    public void onDateChanged(LocalDate date) {
        this.mSelectedDate = date;

        /*
         Update date label to currently selected date
         Include day indicators (i.e. 1st, 2nd, 3rd)
        */
        if(mDayLabel != null)
            mDayLabel.setText(new DateFormatter("MMMM d yyyy").formatWithDayIndicators(mSelectedDate));

        if(mRelativeDayLabel != null) {
            mRelativeDayLabel.setText(RelativeDateHelper.getRelativeDaysSinceString(date));
        }
    }
}
