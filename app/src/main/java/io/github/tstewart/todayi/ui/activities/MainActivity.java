package io.github.tstewart.todayi.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.events.OnDatabaseInteracted;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.ui.fragments.AccomplishmentListFragment;
import io.github.tstewart.todayi.helpers.DateFormatter;

import static java.util.Calendar.getInstance;

/*
Main Activity of the application (obviously), handles AccomplishmentListFragment and DayRatingFragment functionality
 */
public class MainActivity extends AppCompatActivity implements OnDateChangedListener {

    /* Used when requesting a response from CalendarActivity */
    private static final int CALENDAR_ACTIVITY_REQUEST_CODE = 1;
    /* Used when requesting a response from OptionsActivity */
    private static final int OPTIONS_ACTIVITY_REQUEST_CODE = 2;

    /* Minimum pixel distance required to constitute a swipe gesture */
    private static final int SWIPE_GESTURE_DISTANCE = 250;

    /* Location the user started to touch the screen, and the location the user stopped touching the screen */
    private float mTouchLocationStart;
    private float mTouchLocationEnd;

    /* Currently selected date (Application-wide, controlled by OnDateChangedListener) */
    Date mSelectedDate;

    /* Fragment that contains functionality for viewing, creating, editing, and deleting Accomplishments */
    AccomplishmentListFragment mListFragment;

    /* Bottom bar containing date buttons and DayRating fragment */
    LinearLayout mBottomBar;
    /* Text label, shows current date formatted */
    TextView mDayLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get bottom bar buttons for controlling date */
        Button prevButton = findViewById(R.id.buttonPrevDay);
        Button todayButton = findViewById(R.id.buttonToday);
        Button nextButton = findViewById(R.id.buttonNextDay);
        mBottomBar = findViewById(R.id.linearLayoutBottomBar);
        mDayLabel = findViewById(R.id.textViewCurrentDate);

        /* Set functionality of bottom bar buttons */
        prevButton.setOnClickListener(this::onDayChangeButtonClicked);
        todayButton.setOnClickListener(this::onDayChangeButtonClicked);
        nextButton.setOnClickListener(this::onDayChangeButtonClicked);

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
        updateCurrentDate(new Date());
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
            targetIntent.putExtra("selectedDate", mSelectedDate.getTime());

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

                    /*
                     TODO simplify this
                     Set the current selected day to the provided result
                    */
                    Calendar c = new GregorianCalendar();
                    c.setTime(new Date(0));
                    c.add(Calendar.DAY_OF_YEAR, (int) dateResult);

                    updateCurrentDate(c.getTime());
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

    /* If the phone orientation is changed, hide or show rating fragment and day management buttons */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        /* If bottom bar was found */
        if(mBottomBar != null) {
            /* If new orientation is portrait, show additional elements */
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) mBottomBar.setVisibility(View.VISIBLE);
            /* If new orientation is landscape, hide additional elements */
            else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) mBottomBar.setVisibility(View.GONE);
        }
    }

    /* Handle on touch screen events, calculate if a swipe gesture was performed */
    public boolean onTouchEvent(View view, MotionEvent event) {

        /* If event was not null and gestures are enabled */
        if(event != null && UserPreferences.isEnableGestures()) {
            switch(event.getAction()) {
                /* When user presses down on the screen, get the X position of the position of their click */
                case MotionEvent.ACTION_DOWN:
                    mTouchLocationStart = event.getX();
                    break;
                /* When user stops pressing down on the screen, get the X position they stopped pressing the screen */
                case MotionEvent.ACTION_UP:
                    mTouchLocationEnd = event.getX();

                    /* Get distance user travelled while pressing screen */
                    float swipeDistance = mTouchLocationEnd - mTouchLocationStart;

                    /* If the distance, positive or negative, is greater than the minimum distance required to constitute a swipe gesture */
                    if(Math.abs(swipeDistance) >= SWIPE_GESTURE_DISTANCE) {
                        Date newDate = mSelectedDate;
                        /* If swipe distance is positive, user swiped right */
                        if(swipeDistance>0) {
                            /* Go to previous day */
                            newDate = addToCurrentDate(newDate,-1);
                        }
                        /* If swipe distance is negative, user swiped left */
                        else {
                            /* Go to next day */
                            newDate = addToCurrentDate(newDate,1);
                        }
                        /* Update current day across application */
                        updateCurrentDate(newDate);
                    }
                    break;
                default:
                    break;
            }
        }

        view.performClick();
        return false;
    }

    /*
        Notifies all subscribers to OnDatabaseInteracted that the selected date has been changed
         */
    void updateCurrentDate(@NonNull Date date) {
        OnDateChanged.notifyDateChanged(date);
    }

    /*
    When one of the bottom bar buttons (Next, Today, Previous) are pressed
     */
    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        Date newDate = mSelectedDate;

        /* If selected button was Next or Previous, add or subtract one day from current */
        if (viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            if (viewId == R.id.buttonPrevDay) {
                newDate = addToCurrentDate(newDate,-1);
            } else {
                newDate = addToCurrentDate(newDate,1);
            }
        }
        /* If the selected button was Today, reset the currently selected day to System's current day */
        else if (viewId == R.id.buttonToday) newDate = new Date();
        updateCurrentDate(newDate);

        /* Dismiss accomplishment fragment dialog if exists */
        if (mListFragment != null) mListFragment.dismissCurrentDialog();

    }

    /*
    Add a number of days to the provided date
     */
    public Date addToCurrentDate(Date date, int value) {
        Calendar calendar = getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, value);

        return calendar.getTime();
    }

    @Override
    public void onDateChanged(Date date) {
        this.mSelectedDate = date;

        /*
         Update date label to currently selected date
         Include day indicators (i.e. 1st, 2nd, 3rd)
        */
        if(mDayLabel != null)
            mDayLabel.setText(new DateFormatter("MMMM d yyyy").formatWithDayIndicators(mSelectedDate));
    }
}
