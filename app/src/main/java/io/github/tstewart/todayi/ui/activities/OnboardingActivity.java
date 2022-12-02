package io.github.tstewart.todayi.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import io.github.tstewart.todayi.R;
import io.github.tstewart.todayi.adapters.OnboardingItemAdapter;
import io.github.tstewart.todayi.data.UserPreferences;
import io.github.tstewart.todayi.models.OnboardingItem;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 mViewPager;
    private Button mSkipButton;
    private Button mNextButton;
    private Button mPreviousButton;

    ArrayList<OnboardingItem> mOnboardingPages;

    private UserPreferences mUserPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.user_prefs_file_location_key), MODE_PRIVATE);
        mUserPrefs = new UserPreferences(sharedPrefs);

        mSkipButton = findViewById(R.id.buttonSkipOnboarding);
        mNextButton = findViewById(R.id.buttonNextOnboarding);
        mPreviousButton = findViewById(R.id.buttonPreviousOnboarding);
        mViewPager = findViewById(R.id.viewPagerOnboardingContent);

        setViewPagerContent();

        mSkipButton.setOnClickListener(view -> finishOnboarding());
        mNextButton.setOnClickListener(this::onNextButtonClicked);
        mPreviousButton.setOnClickListener(this::onPreviousButtonClicked);
        mPreviousButton.setClickable(false);

        /* Prevent user from swiping to change pages on viewpager */
        mViewPager.setUserInputEnabled(false);
    }

    private void onNextButtonClicked(View view) {
        int newPos = mViewPager.getCurrentItem()+1;
        int maxPos = mOnboardingPages.size()-1;

        mViewPager.setCurrentItem(newPos);
        mNextButton.setText(newPos >= maxPos ? R.string.button_end : R.string.button_next);
        mPreviousButton.setClickable(true);

        if(newPos > maxPos) {
            finishOnboarding();
        }
    }

    private void onPreviousButtonClicked(View view) {
        int newPos = mViewPager.getCurrentItem()-1;

        mViewPager.setCurrentItem(newPos);
        mNextButton.setText(R.string.button_next);
        mPreviousButton.setClickable(newPos>0);
    }

    private void setViewPagerContent() {
        if(this.mViewPager != null) {
            mOnboardingPages = new ArrayList<>();
            mOnboardingPages.add(new OnboardingItem(R.string.ob_intro_title, R.string.ob_intro_description, R.drawable.splash_logo));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_accomplishments_title, R.string.ob_accomplishments_description, R.drawable.onboarding_add));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_editor_title, R.string.ob_editor_description, R.drawable.onboarding_editor));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_accomp_card_title, R.string.ob_accomp_card_description, R.drawable.onboarding_manage));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_reordering_title, R.string.ob_reordering_description, R.drawable.onboarding_reordering));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_rating_title, R.string.ob_rating_description, R.drawable.onboarding_rating));
            mOnboardingPages.add(new OnboardingItem(R.string.ob_calendar_title, R.string.ob_calendar_description, R.drawable.onboarding_calendar));
            mViewPager.setAdapter(new OnboardingItemAdapter(this, mOnboardingPages));
        }
    }

    private void finishOnboarding() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.ob_end_dialog)
                .setPositiveButton(R.string.button_yes, (dialogInterface, i) -> {
                    if(mUserPrefs != null) {
                        mUserPrefs.set(getString(R.string.user_prefs_onboarding_shown), true);
                        UserPreferences.setOnboardingShown(true);
                    }
                    finish();
                    overridePendingTransition(0, R.anim.fade_out);
                })
                .setNegativeButton(R.string.button_no, null)
                .create()
                .show();
    }
}