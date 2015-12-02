package io.givenow.app.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.GiveNowApplication;
import io.givenow.app.R;
import io.givenow.app.activities.MainActivity;

/**
 * Created by aphex on 11/23/15.
 */
public class PhoneNumberOnboardingFragment extends Fragment implements
        PhoneNumberVerificationFragment.OnUserLoginCompleteListener {

    private static final String ARG_TITLE = "title";
    private static final String ARG_COLOUR = "colour";
    @Bind(R.id.title)
    TextView tvTitle;
    @Bind(R.id.description2)
    TextView tvDescription2;
    @Bind(R.id.main)
    LinearLayout llMain;
    private int colour;
    private String title;

    public PhoneNumberOnboardingFragment() {
    }

    public static PhoneNumberOnboardingFragment newInstance(String title, int colour) {
        PhoneNumberOnboardingFragment sampleSlide = new PhoneNumberOnboardingFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_COLOUR, colour);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().size() != 0) {
            title = getArguments().getString(ARG_TITLE);
            colour = getArguments().getInt(ARG_COLOUR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_phone_number, container, false);
        ButterKnife.bind(this, v);

        tvTitle.setText(title);
        tvDescription2.setText(R.string.phone_number_later);
        llMain.setBackgroundColor(colour);

        PhoneNumberVerificationFragment phoneNumberVerificationFragment =
                new PhoneNumberVerificationFragmentBuilder()
                        .build();
        getChildFragmentManager().beginTransaction()
                .add(R.id.phoneNumberFragmentContainer,
                        phoneNumberVerificationFragment,
                        "phoneNumberVerificationFragment")
                .commit();
        return v;
    }

    @OnClick(R.id.btnAddPhoneNumberLater)
    public void onAddPhoneNumberLater(Button btnAddPhoneNumberLater) {
        ((GiveNowApplication) getActivity().getApplication()).getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("OnBoarding")
                        .setAction("AddPhoneNumberLaterClicked")
//                        .setLabel()
                        .setValue(1)
                        .build());

        onUserLoginComplete();
    }

    @Override
    public void onUserLoginComplete() {
        //set first time var
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", true);
        editor.apply();

        ((GiveNowApplication) getActivity().getApplication()).getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("OnBoarding")
                        .setAction("UserLoginComplete")
                        .setLabel(ParseUser.getCurrentUser().getObjectId())
                        .build());

        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        Log.d("Onboarding", "Starting MainActivity");
        startActivity(mainIntent);
        getActivity().finish();
    }
}
