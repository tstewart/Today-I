package io.github.tstewart.todayi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        this.setTitle("Debug Menu <3");

        Button invalidateBackupButton = findViewById(R.id.debugInvalidateBackupTime);
        Button backButton = findViewById(R.id.debugBack);

        if(invalidateBackupButton != null) invalidateBackupButton.setOnClickListener(this::onInvalidateBackupButtonClicked);
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
}