package io.github.tstewart.todayi.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.tstewart.todayi.R;

/* Splash Screen, shown when application is run to show application logo */
public class SplashScreenActivity extends AppCompatActivity {

    /* Time in milliseconds to display this Activity for */
    static final int SPLASH_DISPLAY_LENGTH_MILLIS = 3000;

    /* Handles wait time */
    final Handler mSplashWaitHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        View mainLayout = findViewById(R.id.splashScreenLayout);
        mainLayout.setOnClickListener(v -> {
            /* Ends splash screen on click */
            mSplashWaitHandler.removeCallbacksAndMessages(null);
            endSplashToMainActivity();
        });

        /* Add delayed function call to close splash screen after SPLASH_DISPLAY_LENGTH_MILLIS elapses */
        mSplashWaitHandler.postDelayed(this::endSplashToMainActivity, SPLASH_DISPLAY_LENGTH_MILLIS);
    }

    /**
     * Ends this Activity and opens MainActivity
     */
    void endSplashToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        /* Transition with a fade out animation */
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }
}
