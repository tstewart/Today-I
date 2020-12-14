package io.github.tstewart.todayi.ui.activities;

import android.app.ActionBar;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.threeten.bp.LocalDate;

import java.util.Random;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.data.DBConstants;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;
import io.github.tstewart.todayi.helpers.NotificationHelper;
import io.github.tstewart.todayi.helpers.db.DatabaseHelper;
import io.github.tstewart.todayi.models.Accomplishment;
import io.github.tstewart.todayi.models.DayRating;

/*
Debug functions for messing with the internals of the app
 */
public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        this.setTitle("Debug Menu <3");

        LinearLayout debugLayout = findViewById(R.id.debugLayout);

        if(debugLayout != null) {
            for (View v : debugLayout.getTouchables()) {
                if(v instanceof Button) {
                    v.setOnClickListener(this::onButtonClicked);
                }
            }
        }
    }


    public void onButtonClicked(View view) {
        int id = view.getId();

        if(id == R.id.debugInvalidateBackupTime) onInvalidateBackupButtonClicked();
        else if(id == R.id.debug_populate_accomplishments) onPopulateAccomplishmentsButtonClicked();
        else if(id == R.id.debug_populate_ratings) onPopulateRatingsButtonClicked();
        else if(id == R.id.debug_color_test) onColorTestButtonClicked();
        else if(id == R.id.debug_send_notification) onSendNotificationButtonClicked();
        else if(id == R.id.debugBack) this.finish();
    }

    private void onInvalidateBackupButtonClicked() {
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

    private void onPopulateAccomplishmentsButtonClicked() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.debug_populate_accomplishments)
                .setMessage(R.string.debug_populate_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                        Random random = new Random();
                        LocalDate targetDate = LocalDate.now();
                        LocalDate currentDate = targetDate.minusDays(31);

                        DatabaseHelper helper = new DatabaseHelper(DBConstants.ACCOMPLISHMENT_TABLE);

                        while (currentDate.isBefore(targetDate)) {
                            int numPosts = random.nextInt(5);

                            for (int i = 0; i < numPosts; i++) {
                                Accomplishment accomplishment = new Accomplishment(currentDate.atStartOfDay(), "DUMMY CONTENT!");
                                helper.insert(this, accomplishment);
                            }

                            currentDate = currentDate.plusDays(1);
                        }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
    }

    private void onPopulateRatingsButtonClicked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.debug_populate_ratings)
                .setMessage(R.string.debug_populate_confirmation)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    Random random = new Random();
                    LocalDate targetDate = LocalDate.now();
                    LocalDate currentDate = targetDate.minusDays(31);

                    DatabaseHelper helper = new DatabaseHelper(DBConstants.RATING_TABLE);

                    while (currentDate.isBefore(targetDate)) {
                        int rating = random.nextInt(UserPreferences.getMaxDayRating())+1;

                        DayRating dayRating = new DayRating(currentDate,rating);
                        helper.insert(getApplicationContext(),dayRating);

                        currentDate = currentDate.plusDays(1);
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
    }

    public void onColorTestButtonClicked() {

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


    private void onSendNotificationButtonClicked() {
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        notificationHelper.sendNotification("Debug", "Hello! This is a test!");
    }
}
