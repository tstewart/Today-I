package io.github.tstewart.todayi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    final int SPLASH_DISPLAY_LENGTH_MILLIS = 3000;

    final Handler mSplashWaitHandler = new Handler();

    View mMainLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mMainLayout = findViewById(R.id.splashScreenLayout);
        mMainLayout.setOnClickListener(v -> {
            // Ends splash screen on click
            mSplashWaitHandler.removeCallbacksAndMessages(null);
            endSplashToMainActivity();
        });

        mSplashWaitHandler.postDelayed(this::endSplashToMainActivity, SPLASH_DISPLAY_LENGTH_MILLIS);
    }

    void endSplashToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }
}
