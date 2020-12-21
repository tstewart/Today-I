package io.github.tstewart.todayi.ui.activities;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.errors.ValidationFailedException;
import io.github.tstewart.todayi.helpers.ColorBlendHelper;
import io.github.tstewart.todayi.notifications.NotificationSender;
import io.github.tstewart.todayi.helpers.db.AccomplishmentTableHelper;
import io.github.tstewart.todayi.helpers.db.DayRatingTableHelper;
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
        else if(id == R.id.debugShowTutorial) onShowTutorialButtonClicked();
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

                        AccomplishmentTableHelper helper = new AccomplishmentTableHelper(this);

                        while (currentDate.isBefore(targetDate)) {
                            int numPosts = random.nextInt(5);

                            for (int i = 0; i < numPosts; i++) {
                                Accomplishment accomplishment = new Accomplishment(currentDate.atStartOfDay(), "DUMMY CONTENT!");

                                try {
                                    helper.insert(accomplishment);
                                } catch (ValidationFailedException e) {
                                    Log.w("debug", e.getMessage(), e);
                                }
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

                    DayRatingTableHelper helper = new DayRatingTableHelper(this);

                    while (currentDate.isBefore(targetDate)) {
                        int rating = random.nextInt(UserPreferences.getMaxDayRating())+1;

                        DayRating dayRating = new DayRating(currentDate,rating);
                        helper.insert(dayRating);

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
        NotificationSender notificationSender = new NotificationSender(getApplicationContext());

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                new Intent(this,
                        CalendarActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationSender.sendNotification(pendingIntent, "Debug", "Hello! This is a test!");
    }


    private void onShowTutorialButtonClicked() {
        UserPreferences userPrefs = new UserPreferences(getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE));
        userPrefs.set(getString(R.string.user_prefs_tutorial_shown), false);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
