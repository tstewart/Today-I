package io.github.tstewart.todayi.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDate;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.events.OnDateChanged;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* If MainActivity launched with CREATE_POST flag, launch new Accomplishment dialog */
        Intent thisIntent = getIntent();
        if(thisIntent.getAction().equals("io.github.tstewart.todayi.CREATE_POST")) {
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
        if(prevButton != null)
            prevButton.setOnClickListener(this::onDayChangeButtonClicked);
        if(nextButton != null)
            nextButton.setOnClickListener(this::onDayChangeButtonClicked);
        if(mLayoutDayLabel != null)
            /* If day label (parent layout) is pressed, open calendar to change days */
            mLayoutDayLabel.setOnClickListener(v -> new CalendarDialog(this, mSelectedDate).create().show());


        /* Register for date changed events */
        OnDateChanged.addListener(this);

        /* Set custom toolbar */
        Toolbar mToolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(mToolbar);

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
        if(itemId == R.id.toolbar_settings) {
            intent = new Intent(this, SettingsActivity.class);
        }

        if(intent != null)  {
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
        if(mDayLabel != null)
            mDayLabel.setText(new DateFormatter("MMMM d yyyy").formatWithDayIndicators(mSelectedDate));

        if(mRelativeDayLabel != null) {
            /* Get relative date string
            * E.g. if selected date is today, show "Today"
            * If it was yesterday, show "Yesterday" etc. */
            mRelativeDayLabel.setText(RelativeDateHelper.getRelativeDaysSinceString(date));
        }
    }
}
