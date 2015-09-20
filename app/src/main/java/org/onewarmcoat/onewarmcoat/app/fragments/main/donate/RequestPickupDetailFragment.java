package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customviews.AdaptableGradientRectView;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.ConfirmRequestDialogFragment;
import org.onewarmcoat.onewarmcoat.app.helpers.CroutonHelper;
import org.onewarmcoat.onewarmcoat.app.helpers.CustomAnimations;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.keyboardsurfer.android.widget.crouton.Crouton;


public class RequestPickupDetailFragment extends Fragment implements
        ConfirmRequestDialogFragment.ConfirmPickupDialogListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ADDRESS = "addr";
    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";

    @InjectView(R.id.rlAddressContainer)
    SlidingRelativeLayout rlAddressContainer;
    @InjectView(R.id.rlPickupDetailContainer)
    SlidingRelativeLayout rlPickupDetailContainer;
    @InjectView(R.id.agrv)
    AdaptableGradientRectView adaptableGradientRectView;
    @InjectView(R.id.etAddress)
    EditText etAddress;
    @InjectView(R.id.etEstimatedValue)
    EditText etEstimatedValue;
    @InjectView(R.id.etNumCoatsValue)
    EditText etNumCoatsValue;
    @InjectView(R.id.rlNumberCoats)
    RelativeLayout rlNumberCoats;

    @InjectView(R.id.btnSubmitPickup)
    Button btnSubmitPickup;

    private String mAddress;
    private double mLat;
    private double mLng;
    private Animator slide_down_from_top;
    private Animator slide_up_from_bottom;
    private PickupRequest mPickupRequest;
    private boolean mRequestSubmitted = false;
    private Animator fade_in;
    private Animator slide_down_to_bottom;
    private Animator slide_up_to_top;
    private Animator fade_out;
    private boolean mKeyCodeBackEventHandled = false;
    private ObjectAnimator mBtnAnim;
    private boolean mInputValidated = false;


    public RequestPickupDetailFragment() {
        // Required empty public constructor
    }

    public static RequestPickupDetailFragment newInstance(String addr, double lat, double lng) {
        RequestPickupDetailFragment fragment = new RequestPickupDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, addr);
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAddress = getArguments().getString(ARG_ADDRESS);
            mLat = getArguments().getDouble(ARG_LAT);
            mLng = getArguments().getDouble(ARG_LNG);
        }
        getActivity().getActionBar().setTitle("Confirmation");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request_pickup_detail, container, false);
        ButterKnife.inject(this, v);
        setHasOptionsMenu(true);

        //Hack to catch back button and animate away before popping backstack
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //This event is raised twice for a back button press. Not sure why, but
                // here's a hack to only handle the first event.
                if (mKeyCodeBackEventHandled) {
                    Log.d(((Object) this).getClass().getSimpleName(), "Ignoring extra Back event.");
                    return true;
                } else {
                    mKeyCodeBackEventHandled = true;
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        animateAndDetach();
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });

        setAddressFieldText(mAddress);

        mBtnAnim = CustomAnimations.buttonFlashCTA(btnSubmitPickup);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.request_pickup_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_cancel:
                animateAndDetach();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void animateAndDetach() {
        fade_out.setTarget(adaptableGradientRectView);
        slide_down_to_bottom.setTarget(rlPickupDetailContainer);
        slide_up_to_top.setTarget(rlAddressContainer);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                fade_out,
                slide_down_to_bottom,
                slide_up_to_top);

        final FragmentManager fragmentManager = getFragmentManager();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationEndedBeforeDetach();
                fragmentManager.popBackStack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        set.start();
    }

    private void onAnimationEndedBeforeDetach() {
        if (mRequestSubmitted) {
            //stop displaying the spinning indicator
            getActivity().setProgressBarIndeterminateVisibility(false);

            // show submitted confirmation
            Crouton crouton = CroutonHelper.createInfoCrouton(getActivity(), getResources().getString(R.string.pickupRequest_submittedDialog_title), getResources().getString(R.string.pickupRequest_submittedDialog_message));
            crouton.show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getActionBar().setTitle(R.string.app_name);
    }

    public void setAddressFieldText(String text) {
        if (etAddress != null) {
            etAddress.setText(text);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // entering animations
        slide_down_from_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_from_top);
        slide_up_from_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_from_bottom);
        fade_in = AnimatorInflater.loadAnimator(activity, R.animator.fade_in);

        //exiting animations
        slide_down_to_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_to_bottom);
        slide_up_to_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_to_top);
        fade_out = AnimatorInflater.loadAnimator(activity, R.animator.fade_out);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();

        fade_in.setTarget(adaptableGradientRectView);
        slide_down_from_top.setTarget(rlAddressContainer);
        slide_up_from_bottom.setTarget(rlPickupDetailContainer);

        AnimatorSet set = new AnimatorSet();
        set.play(slide_down_from_top).with(slide_up_from_bottom).before(fade_in);
        set.start();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnTextChanged(value = R.id.etNumCoatsValue, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAfterTextChanged(Editable editable) {
        if (editable.length() > 0) {
            if (!editable.toString().equals("0")) {
                enableSubmitButton();
            }
        } else {
            disableSubmitButton();
        }
    }

    public void enableSubmitButton() {
        // we don't actually *disable* the button per se, just use the tag, so we can still click on it
        mInputValidated = true;
        mBtnAnim.end();
        mBtnAnim.start();
    }

    public void disableSubmitButton() {
        mBtnAnim.end();
        mInputValidated = false;
        btnSubmitPickup.setBackgroundColor(getResources().getColor(R.color.disabled));
    }

    @OnClick(R.id.btnSubmitPickup)
    protected void onSubmitPickup(View v) {
        if (!mInputValidated) {
            //TODO: Highlight rlNumberCoats background to hint user to enter number of coats
            ObjectAnimator anim = CustomAnimations.highlightIncompleteInput(rlNumberCoats);
            anim.start();
        } else {
            ParseUser currUser = ParseUser.getCurrentUser();
            String myPhoneNumber = currUser.getString("phoneNumber");
            if (myPhoneNumber == null) {
                //user hasn't entered their phone before
                showConfirmPickupDialog("", "");
            }
            else {
                //they have entered their phone before, let's pre-populate it and their name
                String myName = currUser.getString("name");
                showConfirmPickupDialog(myName, myPhoneNumber);
//                savePickupRequest();
            }

        }
    }

    private void showConfirmPickupDialog(String name, String phoneNumber) {
        FragmentManager fm = getChildFragmentManager();
        ConfirmRequestDialogFragment confirmRequestDialogFragment =
                ConfirmRequestDialogFragment.newInstance("Confirm Pickup", name, phoneNumber,
                        getResources().getText(R.string.donor_dialog_disclaimer));
        confirmRequestDialogFragment.show(fm, "fragment_confirm_request_dialog");
    }

    // after donor enters name and number and hits Confirm
    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        //update the current user's name and phone
        CharityUserHelper.setName(name);
        CharityUserHelper.setPhoneNumber(phoneNumber);

        //grab donation details
        double donationValue;
        String estimatedValueString = etEstimatedValue.getText().toString();
        if (!estimatedValueString.equals("")) {
            donationValue = Double.parseDouble(estimatedValueString);
        } else {
            donationValue = 0.0;
        }
        int numcoats = Integer.parseInt(etNumCoatsValue.getText().toString());

        //ship it off to parse
        mPickupRequest = new PickupRequest(
                new ParseGeoPoint(mLat, mLng),
                name,
                mAddress,
                phoneNumber,
                ParseUser.getCurrentUser(),
                "Coat",
                donationValue,
                numcoats
        );
        savePickupRequest();
    }

    private void savePickupRequest() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        mPickupRequest.saveInBackground(this::shouldWeRetrySave);
    }


    public void shouldWeRetrySave(ParseException e) {
        if (e == null) {
            // saved successfully
            mRequestSubmitted = true;
            // detach this detail fragment, we're done here
            animateAndDetach();
        } else {
            // save did not succeed
            getActivity().setProgressBarIndeterminateVisibility(false);
            // show error notification dialog with retry or cancel
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pickupRequest_retryDialog_title)
                    .setMessage(R.string.pickupRequest_retryDialog_message)
                    .setPositiveButton(R.string.pickupRequest_retryDialog_retryLabel, (dialog, which) -> savePickupRequest())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // do nothing
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        }
    }
}
