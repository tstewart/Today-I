package io.github.tstewart.todayi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.github.tstewart.todayi.fragments.AccomplishmentListFragment;
import io.github.tstewart.todayi.object.Accomplishment;
import io.github.tstewart.todayi.sql.Database;

import android.app.Activity;
import android.content.Intent;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity {

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
        Button nextButton = findViewById(R.id.buttonNextDay);

        prevButton.setOnClickListener(this::onDayChangeButtonClicked);
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

        switch(item.getItemId()) {
            case R.id.toolbar_calendar:
                targetIntent = new Intent(this, CalendarViewActivity.class);
                break;
            case R.id.toolbar_settings:
                targetIntent = new Intent(this, OptionsActivity.class);
                break;
            default:
                break;
        }

        // Await response from calendar option selected
        if(targetIntent != null) startActivityForResult(targetIntent, 1);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        long dateResult = -1;

        if (requestCode == PARENT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                dateResult = data.getLongExtra("result", -1);
            }
        }

        if(dateResult>=0) {

            Date date = new Date();
            date.setTime(dateResult);

            //TODO change current date based on calendar click
            Toast.makeText(this, getDateFormatted("MMMM d y", date), Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void updateCurrentDayAccomplishments() {
        if(selectedDate == null) selectedDate = new Date();

        if(listFragment != null) {
            ArrayList<Accomplishment> accomplishments = new DatabaseAccomplishmentLoader().getAccomplishmentsFromDatabase(sqLiteDatabase, selectedDate);

            if(accomplishments != null) {
                listFragment.setAccomplishments(accomplishments);
            }
        }

        TextView dateLabel = findViewById(R.id.textViewCurrentDate);
        dateLabel.setText(getDateFormatted("MMMM d Y", selectedDate));
    }

    void onDayChangeButtonClicked(View view) {
        int viewId = view.getId();

        if(viewId == R.id.buttonNextDay || viewId == R.id.buttonPrevDay) {

            Log.i("here", "HERE");

            Calendar calendar = getInstance();
            calendar.setTime(selectedDate);

            if (viewId == R.id.buttonPrevDay) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            selectedDate = calendar.getTime();

            Log.i("here", selectedDate.toString());

            updateCurrentDayAccomplishments();
        }
    }

    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

}
