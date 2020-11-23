package io.github.tstewart.todayi;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.event.OnDateChanged;
import io.github.tstewart.todayi.event.OnDateChangedListener;
import io.github.tstewart.todayi.sql.Database;
import io.github.tstewart.todayi.ui.fragment.AccomplishmentListFragment;

import static java.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity implements OnDateChangedListener {

    // Used to ensure the response from the calendar's selection matches the requested number
    private final int PARENT_ACTIVITY_REQUEST_CODE = 1;

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

        OnDateChanged.addListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Set Accomplishment Fragment Date
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof AccomplishmentListFragment) {
                this.listFragment = (AccomplishmentListFragment) fragment;
                break;
            }
        }

        this.database = new Database(getApplicationContext());
        this.sqLiteDatabase = database.getReadableDatabase();

        Date date = new Date();
        updateCurrentDate(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent targetIntent = null;

        switch (item.getItemId()) {
            case R.id.toolbar_calendar:
                targetIntent = new Intent(this, CalendarViewActivity.class);
                targetIntent.putExtra("selectedDate", selectedDate.getTime());
                break;
            case R.id.toolbar_settings:
                targetIntent = new Intent(this, OptionsActivity.class);
                break;
            default:
                break;
        }

        // Await response from calendar option selected
        if (targetIntent != null) startActivityForResult(targetIntent, 1);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PARENT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                long dateResult;

                dateResult = data.getLongExtra("result", -1);

                if (dateResult >= 0) {

                    Calendar c = new GregorianCalendar();
                    c.setTime(new Date(0));
                    c.add(Calendar.DAY_OF_YEAR, (int) dateResult);

                    updateCurrentDate(c.getTime());
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    void updateCurrentDate(@NonNull Date date) {
        OnDateChanged.notifyDatabaseInteracted(date);
    }

    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        Date newDate = selectedDate;

        if (viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            Calendar calendar = getInstance();
            calendar.setTime(selectedDate);

            if (viewId == R.id.buttonPrevDay) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            newDate = calendar.getTime();

        } else if (viewId == R.id.buttonToday) newDate = new Date();
        updateCurrentDate(newDate);

        // Dismiss accomplishment fragment dialog if exists
        if(listFragment != null) listFragment.dismissCurrentDialog();

    }

    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    public void onDateChanged(Date date) {
        this.selectedDate = date;

        TextView dateLabel = findViewById(R.id.textViewCurrentDate);
        dateLabel.setText(getDateFormatted("MMMM d yyyy", selectedDate));
    }
}
