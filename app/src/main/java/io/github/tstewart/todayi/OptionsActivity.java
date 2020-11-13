package io.github.tstewart.todayi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
