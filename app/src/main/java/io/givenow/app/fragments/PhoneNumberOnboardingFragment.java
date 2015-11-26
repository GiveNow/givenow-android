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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.R;
import io.givenow.app.activities.MainActivity;

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
    //    @Bind(R.id.description)
//    TextView tvDescription;
    @Bind(R.id.description2)
    TextView tvDescription2;
    @Bind(R.id.main)
    LinearLayout llMain;
    //    @Bind(R.id.etPhoneNumber)
//    EditText etPhoneNumber;
//    @Bind(R.id.etSMSCode)
//    EditText etSMSCode;
//    @Bind(R.id.back)
//    ImageButton ibBack;
//    @Bind(R.id.done)
//    ImageButton ibDone;
//    @Bind(R.id.vsPhoneSMS)
//    ViewSwitcher vsPhoneSMS;
    private int drawable, colour;
    private String title, description;
//    private boolean mPhoneNumberFieldShowing = true;

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
//        tvDescription.setText(description);
        tvDescription2.setText(R.string.phone_number_later);
        llMain.setBackgroundColor(colour);

        return v;
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
        Log.d("Onboarding", "Starting MainActivity");
        startActivity(mainIntent);
        getActivity().finish();
    }
}
