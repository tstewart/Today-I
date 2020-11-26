package io.github.tstewart.todayi;

import io.github.tstewart.todayi.event.OnDatabaseInteracted;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.event.OnDateChanged;
import io.github.tstewart.todayi.event.OnDateChangedListener;
import io.github.tstewart.todayi.ui.fragment.AccomplishmentListFragment;
import io.github.tstewart.todayi.utils.DateFormatter;

import static java.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity implements OnDateChangedListener {

    // Used to ensure the response from the calendar's selection matches the requested number
    private final int CALENDAR_ACTIVITY_REQUEST_CODE = 1;
    private final int OPTIONS_ACTIVITY_REQUEST_CODE = 2;

    Date mSelectedDate;

    AccomplishmentListFragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button prevButton = findViewById(R.id.buttonPrevDay);
        Button todayButton = findViewById(R.id.buttonToday);
        Button nextButton = findViewById(R.id.buttonNextDay);

        prevButton.setOnClickListener(this::onDayChangeButtonClicked);
        todayButton.setOnClickListener(this::onDayChangeButtonClicked);
        nextButton.setOnClickListener(this::onDayChangeButtonClicked);

        OnDateChanged.addListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Set Accomplishment Fragment Date
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof AccomplishmentListFragment) {
                this.mListFragment = (AccomplishmentListFragment) fragment;
                break;
            }
        }

        updateCurrentDate(new Date());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        Intent targetIntent = null;
        int requestCode = 0;

        if(itemId == R.id.toolbar_calendar) {
            targetIntent = new Intent(this, CalendarActivity.class);
            requestCode = CALENDAR_ACTIVITY_REQUEST_CODE;
            targetIntent.putExtra("selectedDate", mSelectedDate.getTime());
        }
        else if(itemId == R.id.toolbar_settings) {
            targetIntent = new Intent(this, OptionsActivity.class);
            requestCode = OPTIONS_ACTIVITY_REQUEST_CODE;
        }

        /*
         Start new activity and await response
         Calendar will respond when a date is selected
         Options will respond when an option causes a database refresh
        */
        if(targetIntent != null) startActivityForResult(targetIntent, requestCode);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        long dateResult;

        if (requestCode == CALENDAR_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                dateResult = data.getLongExtra("result", -1);

                if (dateResult >= 0) {
                    Calendar c = new GregorianCalendar();
                    c.setTime(new Date(0));
                    c.add(Calendar.DAY_OF_YEAR, (int) dateResult);

                    updateCurrentDate(c.getTime());
                }
            }
        }
        else if (requestCode == OPTIONS_ACTIVITY_REQUEST_CODE) {
            // On receive OK response, settings activity forces reset of accomplishments
            if (resultCode == Activity.RESULT_OK) {
                if(this.mListFragment != null) {
                    OnDatabaseInteracted.notifyDatabaseInteracted();
                }
            }
        }
    }

    void updateCurrentDate(@NonNull Date date) {
        OnDateChanged.notifyDatabaseInteracted(date);
    }

    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        Date newDate = mSelectedDate;

        if (viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            Calendar calendar = getInstance();
            calendar.setTime(mSelectedDate);

            if (viewId == R.id.buttonPrevDay) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            newDate = calendar.getTime();

        } else if (viewId == R.id.buttonToday) newDate = new Date();
        updateCurrentDate(newDate);

        // Dismiss accomplishment fragment dialog if exists
        if(mListFragment != null) mListFragment.dismissCurrentDialog();

    }

    @Override
    public void onDateChanged(Date date) {
        this.mSelectedDate = date;

        TextView dateLabel = findViewById(R.id.textViewCurrentDate);
        dateLabel.setText(new DateFormatter("MMMM d yyyy").formatWithDayIndicators(mSelectedDate));
    }
}
