package io.givenow.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import io.givenow.app.R;

/**
 * Created by aphex on 11/16/15.
 *
 */
public class OnboardingActivity extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_1_title),
                getString(R.string.onboarding_1_description),
                R.drawable.ic_launcher_google_play,
                getResources().getColor(R.color.onboarding_1_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_2_title),
                getString(R.string.onboarding_2_description),
                R.drawable.donor_choose_categories,
                getResources().getColor(R.color.onboarding_2_color)));
//        addSlide(first_fragment);
//        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_3_title),
//                getString(R.string.onboarding_3_description),
//                getDrawable(R.drawable.onboarding_3_image),
//                getColor(R.color.onboarding_3_color)));

        // OPTIONAL METHODS
        // Override bar/separator color
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        setFadeAnimation();
        // Hide Skip/Done button
//        showSkipButton(false);
//        showDoneButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        // first time
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", true);
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));

        finish();

    }
}