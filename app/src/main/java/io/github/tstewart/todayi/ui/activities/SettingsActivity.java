package io.github.tstewart.todayi.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.elevation.SurfaceColors;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.ui.fragments.SettingsFragment;

/**
 * Settings Activity. Inflates Settings fragment and handles Activity animation
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        /* Inflate SettingsFragment */
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        Toolbar mToolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /* Apply status bar/navigation bar colors */
        int color = SurfaceColors.SURFACE_2.getColor(this);
        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /* If back button pressed */
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        /* Add animation on Activity change, swipe out this activity and swipe in new activity */
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}