package io.github.tstewart.todayi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.elevation.SurfaceColors;

import org.threeten.bp.LocalDate;

import java.util.List;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.events.OnDateChanged;
import io.github.tstewart.todayi.events.OnSwipePerformedListener;
import io.github.tstewart.todayi.helpers.DateFormatter;
import io.github.tstewart.todayi.helpers.RelativeDateHelper;
import io.github.tstewart.todayi.interfaces.OnDateChangedListener;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentDialog;
import io.github.tstewart.todayi.ui.dialogs.AccomplishmentNewDialog;
import io.github.tstewart.todayi.ui.dialogs.CalendarDialog;
import io.github.tstewart.todayi.ui.fragments.AccomplishmentListFragment;

/*
Main Activity of the application, handles AccomplishmentListFragment and DayRatingFragment functionality
 */
public class MainActivity extends AppCompatActivity implements OnDateChangedListener {

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

    /* Calendar Dialog */
    AlertDialog mCalendarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* If MainActivity launched with CREATE_POST flag, launch new Accomplishment dialog */
        Intent thisIntent = getIntent();
        if (thisIntent.getAction().equals("io.github.tstewart.todayi.CREATE_POST")) {
            AccomplishmentDialog dialog = new AccomplishmentNewDialog(LocalDate.now());

            dialog.display(getSupportFragmentManager());
        }

        /* Get bottom bar buttons for controlling date */
        ImageButton prevButton = findViewById(R.id.buttonPrevDay);
        ImageButton nextButton = findViewById(R.id.buttonNextDay);
        mLayoutDayLabel = findViewById(R.id.linearLayoutDayLabel);
        mDayLabel = findViewById(R.id.textViewCurrentDate);
        mRelativeDayLabel = findViewById(R.id.textViewRelativeDay);

        /* Set functionality of bottom bar buttons */
        if (prevButton != null)
            prevButton.setOnClickListener(this::onDayChangeButtonClicked);
        if (nextButton != null)
            nextButton.setOnClickListener(this::onDayChangeButtonClicked);
        if (mLayoutDayLabel != null)
            mLayoutDayLabel.setOnTouchListener(changeDayOnSwipe());

        /* Register for date changed events */
        OnDateChanged.addListener(this);

        /* Set custom toolbar */
        Toolbar mToolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(mToolbar);

        /* Set status/navigation bar colors to match topbar */
        setSystemBarColors();
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

        /* Check to see if onboarding needs to be shown (if user is new) */
        if (!UserPreferences.isOnboardingShown()) {
            Intent onboardingIntent = new Intent(this, OnboardingActivity.class);
            startActivity(onboardingIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* Set status/navigation bar colors to match topbar */
        setSystemBarColors();
    }

    /* Inflate Main Activity's top bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_nav, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /* If an item on the top bar is selected */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Intent intent = null;
        /*
         Depending on the selected item, set the Intent Activity
        */
        if (itemId == R.id.toolbar_settings) {
            intent = new Intent(this, SettingsActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            /* Add animation on Activity change, swipe out this activity and swipe in new activity */
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onDateChanged(LocalDate date) {
        this.mSelectedDate = date;

        /*
         Update date label to currently selected date
         Include day indicators (i.e. 1st, 2nd, 3rd)
        */
        if (mDayLabel != null)
            mDayLabel.setText(new DateFormatter("MMMM d yyyy").formatWithDayIndicators(mSelectedDate));

        if (mRelativeDayLabel != null) {
            /* Get relative date string
             * E.g. if selected date is today, show "Today"
             * If it was yesterday, show "Yesterday" etc. */
            mRelativeDayLabel.setText(RelativeDateHelper.getRelativeDaysSinceString(date));
        }
    }

    void setSystemBarColors() {
        /* Apply status bar/navigation bar colors */
        int color = SurfaceColors.SURFACE_2.getColor(this);
        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    /* Return listener action to change current day when view is swiped */
    private OnSwipePerformedListener changeDayOnSwipe() {
        return new OnSwipePerformedListener(this) {
            @Override
            public boolean onTouch(MotionEvent e) {
                /* If touch event is recognised (i.e. a swipe that is too small), open calendar to change days */
                if (mCalendarDialog == null || !mCalendarDialog.isShowing()) {
                    mCalendarDialog = new CalendarDialog(MainActivity.this, mSelectedDate).create();
                    mCalendarDialog.show();
                }
                return true;
            }

            @Override
            public void onSwipe(SwipeDirection direction) {
                /* If we should do anything with swipe gestures (controlled by settings) */
                if (UserPreferences.isGesturesEnabled()) {

                    if (mSelectedDate == null) mSelectedDate = LocalDate.now();

                    if (direction == SwipeDirection.LEFT) {
                        mSelectedDate = mSelectedDate.plusDays(1);
                    } else {
                        mSelectedDate = mSelectedDate.minusDays(1);
                    }

                    OnDateChanged.notifyDateChanged(mSelectedDate);
                }
            }
        };
    }
}
