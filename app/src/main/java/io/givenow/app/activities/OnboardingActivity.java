package io.givenow.app.activities;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.parse.ParseUser;

import java.util.HashMap;

import io.givenow.app.R;
import io.givenow.app.fragments.PhoneNumberOnboardingFragment;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.AnimatorEndListener;
import io.givenow.app.models.ParseUserHelper;
import rx.parse.ParseObservable;

/**
 * Created by aphex on 11/16/15.
 */
public class OnboardingActivity extends AppIntro2
        implements PhoneNumberOnboardingFragment.AppIntroInteractionListener {

    private PhoneNumberOnboardingFragment phoneNumberOnboardingFragment;
    private ImageView ivDone;

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
                R.drawable.onboarding_2_image,
                getResources().getColor(R.color.onboarding_2_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_3_title),
                getString(R.string.onboarding_3_description),
                R.drawable.onboarding_3_image,
                getResources().getColor(R.color.onboarding_3_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_4_title),
                getString(R.string.onboarding_4_description),
                R.drawable.onboarding_4_image,
                getResources().getColor(R.color.onboarding_4_color)));

        phoneNumberOnboardingFragment = PhoneNumberOnboardingFragment.newInstance(getString(R.string.onboarding_5_title),
                getString(R.string.phone_number_disclaimer),
                getResources().getColor(R.color.onboarding_5_color));
        addSlide(phoneNumberOnboardingFragment);

        // OPTIONAL METHODS
        // Override bar/separator color
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        //TODO: possibly implement color fade effect between slides instead of this
        setFadeAnimation();
        // Hide Skip/Done button
//        showSkipButton(false);
        showDoneButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
//        setVibrate(true);
//        setVibrateIntensity(30);

        ivDone = (ImageView) findViewById(com.github.paolorotolo.appintro.R.id.done);

    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        // first time
//        Prefs.setRanBefore(true);

        String phoneNumber = phoneNumberOnboardingFragment.getPhoneNumber();
        if (phoneNumber.length() > 0) {
            //change done button to spinner
            CustomAnimations.circularHide(ivDone).start();

            //TODO could do additional phone number verification here
            ParseObservable.first(ParseUser.getQuery().whereEqualTo("phoneNumber", phoneNumber))
                    .doOnError(e -> {
                        //user doesn't exist, let's sign them up
                        Log.d("Onboarding", "User query error result: " + e.getMessage());
                        Log.d("Onboarding", "User with # " + phoneNumber + " doesn't exist, registering.");
                        ParseUserHelper.registerUserWithDevice(phoneNumber);
                        onUserLoginComplete();
                    })
                    .subscribe(user -> {
                        //user does already exist, let's "log them in"
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("phoneNumber", phoneNumber);
                        ParseObservable.callFunction("getUserSessionToken", params).subscribe(sessionToken -> {
                            ParseObservable.become(sessionToken.toString()).subscribe(becameUser -> {
                                Log.d("Onboarding", "Became user " + becameUser.get("phoneNumber"));
                                ParseUserHelper.associateWithDevice(becameUser);
                                onUserLoginComplete();
                            });
                        });
                    });
        } else {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            ivDone.startAnimation(shake);
        }
    }

    public ImageView getDoneButton() {
        return ivDone;
    }

    public void onUserLoginComplete() {
        runOnUiThread(() -> {
            //set first time var
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();

            // TODO fix the observable calling this to observe on the right thread (the thread that created ivDone)
            Intent mainIntent = new Intent(this, MainActivity.class);
            //change done button to givenow smiley
            ivDone.setImageResource(R.mipmap.ic_launcher);
            Animator reveal = CustomAnimations.circularReveal(ivDone);
            reveal.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d("Onboarding", "Starting MainActivity");
                    startActivity(mainIntent);
                    finish();
                }
            });
            reveal.start();
        });
    }
}