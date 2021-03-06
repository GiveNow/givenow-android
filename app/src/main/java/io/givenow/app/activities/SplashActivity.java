package io.givenow.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by aphex on 11/23/15.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent targetIntent;
        if (isFirstTime()) {
            targetIntent = new Intent(this, OnboardingActivity.class);
        } else {
            targetIntent = new Intent(this, MainActivity.class);
        }
        // So push notification data gets passed on:
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                targetIntent.putExtras(getIntent().getExtras());
            }
        }

        startActivity(targetIntent);
        finish();
    }

    /***
     * Checks that application runs first time and write flag at SharedPreferences
     *
     * @return true if 1st time
     */
    private boolean isFirstTime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        return !ranBefore;
    }
}
