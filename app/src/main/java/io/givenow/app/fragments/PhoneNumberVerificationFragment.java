package io.givenow.app.fragments;

/**
 * Created by aphex on 11/26/15.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.givenow.app.R;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.models.ParseInstallationHelper;
import io.givenow.app.models.ParseUserHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

/**
 * Created by aphex on 11/23/15.
 */
@FragmentWithArgs
public class PhoneNumberVerificationFragment extends DialogFragment {

    @Arg(required = false) //optiona args with an initializer here doesn't seem to work, the initializer seems to clobber whatever magic fragmentargs is doing
            int mMessageResource = R.string.phone_number_disclaimer;

    @BindView(R.id.llContainer)
    LinearLayout llContainer;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.tsDescription)
    TextSwitcher tsDescription;
    @BindView(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @BindView(R.id.etSMSCode)
    EditText etSMSCode;
    @BindView(R.id.back)
    ImageButton ibBack;
    @BindView(R.id.done)
    ImageButton ibDone;
    @BindView(R.id.vsPhoneSMS)
    ViewSwitcher vsPhoneSMS;
    @BindView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    private boolean mPhoneNumberFieldShowing = true;
    private Unbinder unbinder;

//    private OnUserLoginCompleteListener mListener;

//    public PhoneNumberVerificationFragment() {
//    }
//
//    public static PhoneNumberVerificationFragment newInstance() {
//        PhoneNumberVerificationFragment phoneNumberVerificationFragment = new PhoneNumberVerificationFragment();
//
//        return phoneNumberVerificationFragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this); // read @Arg fields
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phone_verification, container, false);
        unbinder = ButterKnife.bind(this, v);

//        llMain.setBackgroundColor(colour);
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
                    progressIndicator.setVisibility(View.VISIBLE);
                } else {
                    if (ibDone.getVisibility() == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start();
                    }
                    progressIndicator.setVisibility(View.INVISIBLE);
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

        String locale = getResources().getConfiguration().locale.getCountry();
        Log.d("phfrag", "locale is " + locale);
        etPhoneNumber.setText("+" + String.valueOf(PhoneNumberUtil.getInstance().getCountryCodeForRegion(locale)));
        etPhoneNumber.setSelection(etPhoneNumber.getText().length());

        if (getShowsDialog()) {
            //If we're being displayed in a dialog, modify a few views.
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(R.string.phone_number_verification_title);
            progressIndicator.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryLight), android.graphics.PorterDuff.Mode.SRC_ATOP);
            int pad = getResources().getDimensionPixelSize(R.dimen.dialog_container_padding);
            llContainer.setPadding(pad, pad, pad, pad);
            llContainer.requestLayout();

            tsDescription.setFactory(() -> {
                //Dialogs show their text left-justified rather than centered.
                TextView tv = tvFactory();
                tv.setGravity(Gravity.START);
                tv.setTextSize(18);
                return tv;
            });
        } else {
            tsDescription.setFactory(this::tvFactory);
        }

        tsDescription.setText(getString(mMessageResource));
        return v;
    }

    private TextView tvFactory() {
        return (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.textview_phone_verify_description, tsDescription, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //seems to happen before onCreateView
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //no title for this dialog please (a title bar appears on API<21)
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
        uiPhoneNumber();
    }

    private void uiPhoneNumber() {
        uiPhoneNumber(mMessageResource);
    }

    private void uiPhoneNumber(@StringRes int messageResource) {
        vsPhoneSMS.setDisplayedChild(0);
        mPhoneNumberFieldShowing = true;
        CustomAnimations.circularHide(ibBack).start();
        if (getPhoneNumber().length() > 0) {
            CustomAnimations.circularReveal(ibDone).start();
        }
        tsDescription.setText(getString(messageResource));
        ibDone.setClickable(true);
    }

    private void uiSMSCode(String phoneNumber) {
        vsPhoneSMS.setDisplayedChild(1);
        mPhoneNumberFieldShowing = false;
        tsDescription.setText(getString(R.string.validate_sms_code, phoneNumber));
        CustomAnimations.circularReveal(ibBack).start();
        ibDone.setClickable(true);
    }

    private void sendCode() {
        String phoneNumber = getPhoneNumber();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (phoneNumber.length() > 0) {
            if (phoneNumber.contains("+")) {
                //validate phone number
                try {
                    Phonenumber.PhoneNumber pn = phoneUtil.parse(phoneNumber, null);
                    if (phoneUtil.isValidNumber(pn)) {
                        //phone number is valid
                        //change done button to spinner
                        CustomAnimations.circularHide(ibDone).start();
                        //request a code
                        ParseUserHelper.INSTANCE.sendCode(phoneNumber, getString(R.string.sms_body_javascript)).subscribe(
                                response -> {
                                    Log.d("Cloud Response", response.toString());
                                    //switch to sendSMS edittext
                                    uiSMSCode(phoneNumber);
                                },
                                error -> {
                                    Log.d("Cloud Response", "Error received from sendCode cloud function: ", error);
                                    uiPhoneNumber();
                                });
                    } else {
                        tsDescription.setText(getString(R.string.phone_number_verification_error_number_invalid));
                        ibDone.setClickable(true);
                    }
                } catch (NumberParseException e) {
                    e.printStackTrace();
                    tsDescription.setText(getString(R.string.phone_number_verification_error_number_failed_to_parse));
                    ibDone.setClickable(true);
                }
            } else {
                tsDescription.setText(getString(R.string.phone_number_verification_error_no_country_code));
                ibDone.setClickable(true);
            }
        } else { //Or just hide ibdone
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            ibDone.startAnimation(shake);
            ibDone.setClickable(true);
        }
    }

    private void doLogin() {
        if (etSMSCode.getText().toString().length() == 4) {
            CustomAnimations.circularHide(ibDone).start();

            String phoneNumber = getPhoneNumber();
            int code = Integer.parseInt(etSMSCode.getText().toString());
            ParseUserHelper.INSTANCE.logIn(phoneNumber, code).subscribe(
                    sessionToken -> {
                        ParseObservable.become(sessionToken.toString()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(becameUser -> {
                                    Log.d("PhoneVerification", "Became user " + becameUser.getUsername());
                                    ParseInstallationHelper.associateUserWithDevice(becameUser);
                                    userLoginComplete();
                                });
                    },
                    error -> {
                        Log.d("Cloud Response", "Error received from logIn cloud function: ", error);
                        uiPhoneNumber(R.string.phone_number_verification_error_sms_code);
                    }
            );
        }
    }

    private void userLoginComplete() {
        //change done button to givenow smiley
        ibDone.setImageResource(R.mipmap.ic_launcher);
        Animator reveal = CustomAnimations.circularReveal(ibDone);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof OnUserLoginCompleteListener) {
                    ((OnUserLoginCompleteListener) parentFragment).onUserLoginComplete();
                }
                if (getDialog() != null) {
                    dismiss();
                }
            }
        });
        reveal.start();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) parentFragment).onDismiss(dialog);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnUserLoginCompleteListener {
        void onUserLoginComplete();
    }
}
