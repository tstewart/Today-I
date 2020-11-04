package io.github.tstewart.todayi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.sql.Database;

import android.database.sqlite.SQLiteDatabase;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    SQLiteDatabase accomplishmentDb;

    private ListView accomplishmentList;

    // Used to ensure the response from the calendar's selection matches the requested number
    private final int PARENT_ACTIVITY_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database db = new Database(getApplicationContext());
        accomplishmentDb = db.getWritableDatabase();

        //TODO replace with seperate date tracking
        //TODO replace setText with resource edit
        TextView date = findViewById(R.id.textViewCurrentDate);
        date.setText(getDateFormatted("MMMM d Y", new Date()));
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

    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

}
