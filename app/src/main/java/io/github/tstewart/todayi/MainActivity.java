package io.github.tstewart.todayi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.ui.fragment.AccomplishmentListFragment;
import io.github.tstewart.todayi.sql.Database;

import android.app.Activity;
import android.content.Intent;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity {

    // Used to ensure the response from the calendar's selection matches the requested number
    private final int CALENDAR_ACTIVITY_REQUEST_CODE = 1;
    private final int OPTIONS_ACTIVITY_REQUEST_CODE = 2;

    Date selectedDate;

    AccomplishmentListFragment listFragment;

    Database database;
    SQLiteDatabase sqLiteDatabase;

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
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Set Accomplishment Fragment Date
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment fragment : fragments) {
            if(fragment instanceof AccomplishmentListFragment) {
                this.listFragment = (AccomplishmentListFragment) fragment;
                break;
            }
        }

        this.database = new Database(getApplicationContext());
        this.sqLiteDatabase = database.getReadableDatabase();

        updateCurrentDayAccomplishments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent targetIntent = null;
        int requestCode = 0;

        switch(item.getItemId()) {
            case R.id.toolbar_calendar:
                targetIntent = new Intent(this, CalendarViewActivity.class);
                requestCode = CALENDAR_ACTIVITY_REQUEST_CODE;
                break;
            case R.id.toolbar_settings:
                targetIntent = new Intent(this, OptionsActivity.class);
                requestCode = OPTIONS_ACTIVITY_REQUEST_CODE;
                break;
            default:
                break;
        }

        // Await response from calendar option selected
        if(targetIntent != null) startActivityForResult(targetIntent, requestCode);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        long dateResult = -1;

        if (requestCode == CALENDAR_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                dateResult = data.getLongExtra("result", -1);

                if(dateResult>=0) {

                    Date date = new Date();
                    date.setTime(dateResult);

                    //TODO change current date based on calendar click
                    Toast.makeText(this, getDateFormatted("MMMM d y", date), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == OPTIONS_ACTIVITY_REQUEST_CODE) {
            // On receive OK response, settings activity forces reset of accomplishments
            if (resultCode == Activity.RESULT_OK) {
                if(this.listFragment != null) {
                    listFragment.updateDateAndFetch(selectedDate);
                }
            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    void updateCurrentDayAccomplishments() {
        if(selectedDate == null) selectedDate = new Date();

        if(listFragment != null && sqLiteDatabase != null) {
            listFragment.updateDateAndFetch(selectedDate);
        }

        TextView dateLabel = findViewById(R.id.textViewCurrentDate);
        dateLabel.setText(getDateFormatted("MMMM d Y", selectedDate));
    }

    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        if (viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            Calendar calendar = getInstance();
            calendar.setTime(selectedDate);

            if (viewId == R.id.buttonPrevDay) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            selectedDate = calendar.getTime();

        }
        else if(viewId == R.id.buttonToday) selectedDate = new Date();
        updateCurrentDayAccomplishments();
    }

    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

}
