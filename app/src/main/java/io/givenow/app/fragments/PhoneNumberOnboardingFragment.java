package io.givenow.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.R;
import io.givenow.app.helpers.CustomAnimations;

/**
 * Created by aphex on 11/23/15.
 */
public class PhoneNumberOnboardingFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_DRAWABLE = "drawable";
    private static final String ARG_COLOUR = "colour";


    private int drawable, colour;
    private String title, description;

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
    private AppIntroInteractionListener mListener;

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

    public PhoneNumberOnboardingFragment() {
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
                    CustomAnimations.circularReveal(mListener.getDoneButton()).start();
                } else {
                    CustomAnimations.circularHide(mListener.getDoneButton()).start();
                }
            }
        });
        return v;
    }

    public String getPhoneNumber() {
        return etPhoneNumber.getText().toString();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AppIntroInteractionListener) activity;
    }

    @OnClick(R.id.btnAddPhoneNumberLater)
    public void onAddPhoneNumberLater(Button btnAddPhoneNumberLater) {
        mListener.onUserLoginComplete();
    }

    public interface AppIntroInteractionListener {
        void onUserLoginComplete();

        ImageView getDoneButton();
    }
}
