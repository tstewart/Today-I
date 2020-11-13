package io.github.tstewart.todayi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    //TODO add smooth transition between this activity and main

    final int SPLASH_DISPLAY_LENGTH_MILLIS = 3000;

    Handler splashWaitHandler = new Handler();

    View mainLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mainLayout = findViewById(R.id.splashScreenLayout);
        mainLayout.setOnClickListener(v -> {
            // Ends splash screen on click
            splashWaitHandler.removeCallbacksAndMessages(null);
            changeActivityToMain();
        });

        splashWaitHandler.postDelayed(this::changeActivityToMain, SPLASH_DISPLAY_LENGTH_MILLIS);
    }

    // TODO rename this terrible function name
    void changeActivityToMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
