package io.github.tstewart.todayi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ListView accomplishmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO replace with seperate date tracking
        //TODO replace setText with resource edit
        TextView date = findViewById(R.id.textViewCurrentDate);
        date.setText(getDateFormatted("MMMM d Y"));
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

        if(targetIntent != null) startActivity(targetIntent);

        return super.onOptionsItemSelected(item);
    }

    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

}
