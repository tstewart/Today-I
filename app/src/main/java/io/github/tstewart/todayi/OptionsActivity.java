package io.github.tstewart.todayi;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

public class OptionsActivity extends AppCompatActivity {

    Button exportDataButton;
    Button googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        exportDataButton = findViewById(R.id.buttonExportData);
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn);

        exportDataButton.setOnClickListener(this::onExportDataButtonClicked);
        googleSignInButton.setOnClickListener(this::onGoogleSignInButtonClicked);
    }

    private void onExportDataButtonClicked(View view) {
        //TODO
    }

    private void onGoogleSignInButtonClicked(View view) {
        //TODO
    }


}
