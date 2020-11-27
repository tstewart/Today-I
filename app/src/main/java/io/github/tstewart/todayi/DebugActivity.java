package io.github.tstewart.todayi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import io.github.tstewart.todayi.object.Accomplishment;
import io.github.tstewart.todayi.sql.DBConstants;
import io.github.tstewart.todayi.sql.DatabaseHelper;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        this.setTitle("Debug Menu <3");

        Button invalidateBackupButton = findViewById(R.id.debugInvalidateBackupTime);
        Button populateAccomplishmentsButton = findViewById(R.id.debug_populate_accomplishments);
        Button backButton = findViewById(R.id.debugBack);

        if(invalidateBackupButton != null) invalidateBackupButton.setOnClickListener(this::onInvalidateBackupButtonClicked);
        if(populateAccomplishmentsButton != null) populateAccomplishmentsButton.setOnClickListener(this::onPopulateAccomplishmentsButtonClicked);
        if(backButton != null) backButton.setOnClickListener(view -> {
            this.finish();
        });
    }


    private void onInvalidateBackupButtonClicked(View view) {
        try {
            getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE)
                    .edit()
                    .putLong(getString(R.string.user_prefs_last_backed_up_key), -1)
                    .apply();

            Toast.makeText(this, "Invalidated time since last backup.", Toast.LENGTH_SHORT).show();
        }
        catch(NullPointerException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onPopulateAccomplishmentsButtonClicked(View view) {
        Random random = new Random();
        Date targetDate = new Date();
        Date currentDate = getDateMonthAgo();

        DatabaseHelper helper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);

        while(currentDate.before(targetDate)) {
            int numPosts = random.nextInt(5);

            for (int i = 0; i < numPosts; i++) {
                Accomplishment accomplishment = new Accomplishment(currentDate, "DUMMY CONTENT!");
                helper.insert(this, accomplishment);
            }

            currentDate = addDay(currentDate);
        }
    }

    private Date addDay(Date currentDate) {
        Calendar c = new GregorianCalendar();
        c.setTime(currentDate);
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTime();
    }

    private Date getDateMonthAgo() {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_YEAR, -31);

        return c.getTime();
    }
}
