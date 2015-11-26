package io.givenow.app.fragments;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.R;
import io.givenow.app.activities.MainActivity;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.AnimatorEndListener;
import io.givenow.app.models.ParseUserHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

/**
 * Created by aphex on 11/23/15.
 */
public class PhoneNumberOnboardingFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_DRAWABLE = "drawable";
    private static final String ARG_COLOUR = "colour";
    @Bind(R.id.title)
    TextView tvTitle;
    @Bind(R.id.description)
    TextView tvDescription;
    @Bind(R.id.description2)
    TextView tvDescription2;
    @Bind(R.id.main)
    LinearLayout llMain;
    @Bind(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @Bind(R.id.etSMSCode)
    EditText etSMSCode;
    @Bind(R.id.back)
    ImageButton ibBack;
    @Bind(R.id.done)
    ImageButton ibDone;
    @Bind(R.id.vsPhoneSMS)
    ViewSwitcher vsPhoneSMS;
    private int drawable, colour;
    private String title, description;
    private boolean mPhoneNumberFieldShowing = true;

    public PhoneNumberOnboardingFragment() {
    }

    public static PhoneNumberOnboardingFragment newInstance(String title, String description, int colour) {
        PhoneNumberOnboardingFragment sampleSlide = new PhoneNumberOnboardingFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, description);
//        args.putInt(ARG_DRAWABLE, imageDrawable);
        args.putInt(ARG_COLOUR, colour);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().size() != 0) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESC);
            colour = getArguments().getInt(ARG_COLOUR);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_phone_number, container, false);
        ButterKnife.bind(this, v);

        tvTitle.setText(title);
        tvDescription.setText(description);
        tvDescription2.setText(R.string.phone_number_later);
        llMain.setBackgroundColor(colour);

        etPhoneNumber.addTextChangedListener(new android.telephony.PhoneNumberFormattingTextWatcher()); //new PhoneNumberFormattingTextWatcher(Locale.getDefault().getCountry()));
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) { //TODO or if valid phone number
                    if (ibDone.getVisibility() != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start();
                    }
                } else {
                    if (ibDone.getVisibility() == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start();
                    }
                }
            }
        });

        etSMSCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    if (ibDone.getVisibility() != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start();
                    }
                } else {
                    if (ibDone.getVisibility() == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start();
                    }
                }
            }
        });
        vsPhoneSMS.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        vsPhoneSMS.setOutAnimation(getActivity(), android.R.anim.slide_out_right);
        return v;
    }

    public String getPhoneNumber() {
        return etPhoneNumber.getText().toString();
    }

    @OnClick(R.id.done)
    public void onDonePressed(ImageButton ibDone) {
        ibDone.setClickable(false);
        if (mPhoneNumberFieldShowing)
            sendCode();
        else
            doLogin();
    }

    @OnClick(R.id.back)
    public void onIbBackPressed(ImageButton ibBack) {
        phoneNumberUI();
        if (getPhoneNumber().length() > 0) {
            CustomAnimations.circularReveal(ibDone).start();
        }
    }

    private void phoneNumberUI() {
        vsPhoneSMS.setDisplayedChild(0);
        mPhoneNumberFieldShowing = true;
        CustomAnimations.circularHide(ibBack).start();
        tvDescription.setText(R.string.phone_number_disclaimer);
        ibDone.setClickable(true);
    }

    private void codeUI(String phoneNumber) {
        vsPhoneSMS.setDisplayedChild(1);
        mPhoneNumberFieldShowing = false;
        tvDescription.setText(getString(R.string.validate_sms_code, phoneNumber));
        CustomAnimations.circularReveal(ibBack).start();
        ibDone.setClickable(true);
    }

    private void sendCode() {
        String phoneNumber = getPhoneNumber();

        if (phoneNumber.length() > 0) {
            //validate phone number
//            PhoneNumberUtil.getInstance().parse(phoneNumber)
            //change done button to spinner
            CustomAnimations.circularHide(ibDone).start();

            //if phone number is valid
            //call sendCode
            ParseUserHelper.sendCode(phoneNumber, getString(R.string.sms_body_javascript)).subscribe(
                    response -> {
                        Log.d("Cloud Response", response.toString());
                        //switch to sendSMS edittext
                        codeUI(phoneNumber);
                    },
                    error -> {
                        Log.d("Cloud Response", "Error received from sendCode cloud function: ", error);
                        CustomAnimations.circularReveal(ibDone).start();
                        phoneNumberUI();
                    });
        } else {
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            ibDone.startAnimation(shake);
        }

    }

    private void doLogin() {
        if (etSMSCode.getText().toString().length() == 4) {
            String phoneNumber = getPhoneNumber();
            int code = Integer.parseInt(etSMSCode.getText().toString());
            ParseUserHelper.logIn(phoneNumber, code).subscribe(
                    sessionToken -> {
                        // ParseUserHelper.signUpOrLogin(phoneNumber, this::onUserLoginComplete);
                        ParseObservable.become(sessionToken.toString()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(becameUser -> {
                                    Log.d("Onboarding", "Became user " + becameUser.getUsername());
                                    ParseUserHelper.associateWithDevice(becameUser);
                                    onUserLoginComplete();
                                });
                    },
                    error -> {
                        Log.d("Cloud Response", "Error received from logIn cloud function: ", error);
                        phoneNumberUI();
                    }
            );
        }
    }

    @OnClick(R.id.btnAddPhoneNumberLater)
    public void onAddPhoneNumberLater(Button btnAddPhoneNumberLater) {
        onUserLoginComplete();
    }

    public void onUserLoginComplete() {
        //set first time var
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", true);
        editor.apply();

        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        //change done button to givenow smiley
        ibDone.setImageResource(R.mipmap.ic_launcher);
        Animator reveal = CustomAnimations.circularReveal(ibDone);
        reveal.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("Onboarding", "Starting MainActivity");
                startActivity(mainIntent);
                getActivity().finish();
            }
        });
        reveal.start();
    }
}
