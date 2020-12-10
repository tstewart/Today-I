package io.github.tstewart.todayi.ui.activities;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.helpers.DatabaseHelper;

/*
Debug functions for messing with the internals of the app
 */
public class DebugActivity extends AppCompatActivity {

    Button mGetColorPercentageTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        this.setTitle("Debug Menu <3");

        Button invalidateBackupButton = findViewById(R.id.debugInvalidateBackupTime);
        Button populateAccomplishmentsButton = findViewById(R.id.debug_populate_accomplishments);
        Button getAccomplishmentsButton = findViewById(R.id.debug_get_accomplishments);
        Button getRatingsButton = findViewById(R.id.debug_get_ratings);
        mGetColorPercentageTest = findViewById(R.id.debug_color_test);
        Button backButton = findViewById(R.id.debugBack);

        if (invalidateBackupButton != null)
            invalidateBackupButton.setOnClickListener(this::onInvalidateBackupButtonClicked);
        if (populateAccomplishmentsButton != null)
           // populateAccomplishmentsButton.setOnClickListener(this::onPopulateAccomplishmentsButtonClicked);
        if (getAccomplishmentsButton != null)
            getAccomplishmentsButton.setOnClickListener(this::onGetAccomplishmentsButtonClicked);
        if (getRatingsButton != null)
            getRatingsButton.setOnClickListener(this::onGetRatingsButtonClicked);
        if (mGetColorPercentageTest != null)
            mGetColorPercentageTest.setOnClickListener(this::onColorTestButtonClicked);
        if (backButton != null) backButton.setOnClickListener(view -> {
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
        } catch (NullPointerException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onPopulateAccomplishmentsButtonClicked(View view) {
        Random random = new Random();
        Date targetDate = new Date();
        Date currentDate = getDateMonthAgo();

        DatabaseHelper helper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);

        while (currentDate.before(targetDate)) {
            int numPosts = random.nextInt(5);

            for (int i = 0; i < numPosts; i++) {
                Accomplishment accomplishment = new Accomplishment(currentDate, "DUMMY CONTENT!");
                helper.insert(this, accomplishment);
            }

            currentDate = addDay(currentDate);
        }
    }

    private void onGetAccomplishmentsButtonClicked(View view) {
    }

    private void onGetRatingsButtonClicked(View view) {
    }

    public void onColorTestButtonClicked(View view) {

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(0);
        numberPicker.setMaxValue(100);

        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme))
                .setView(numberPicker)
                .setPositiveButton("Ok", (dialog, which) -> {
                    int numSelection = numberPicker.getValue();

                    int[] colors = new ColorBlendHelper(numSelection).blendColors();

                    LinearLayout resultView = new LinearLayout(this);
                    resultView.setOrientation(LinearLayout.VERTICAL);

                    ScrollView scrollView = new ScrollView(this);
                    scrollView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    scrollView.addView(resultView);

                    for (int i = 0; i < colors.length; i++) {
                        TextView colorView = new TextView(this);
                        colorView.setText("\n");
                        colorView.setBackgroundColor(colors[i]);

                        resultView.addView(colorView);
                    }

                    new AlertDialog.Builder(this)
                            .setView(scrollView)
                            .create()
                            .show();

                })
                .create()
                .show();
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
