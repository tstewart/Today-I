package io.github.tstewart.todayi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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


    // TODO: Move to seperate file, date will be tracked elsewhere
    public String getDateFormatted(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

}
