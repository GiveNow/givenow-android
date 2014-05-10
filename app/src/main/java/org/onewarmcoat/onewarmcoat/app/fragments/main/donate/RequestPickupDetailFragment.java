package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customviews.AdaptableGradientRectView;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class RequestPickupDetailFragment extends Fragment implements
        NumberPickerDialogFragment.NumberPickerDialogListener,
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
    @InjectView(R.id.tvNumCoatsValue)
    TextView tvNumCoatsValue;

    @InjectView(R.id.btnSubmitPickup)
    Button btnSubmitPickup;

    private String mAddress;
    private double mLat;
    private double mLng;
    private Animator slide_down_from_top;
    private Animator slide_up_from_bottom;
    private PickupRequest mPickupRequest;
    private boolean mRequestSubmitted;
    private Animator fade_in;
    private Animator slide_down_to_bottom;
    private Animator slide_up_to_top;
    private Animator fade_out;
    private boolean mKeyCodeBackEventHandled = false;


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
                //onAnimationEndedBeforeDetach();
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
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pickupRequest_submittedDialog_title)
                    .setMessage(R.string.pickupRequest_submittedDialog_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                            //TODO: perhaps add another button to this dialog - 'view profile'?
                    .setIcon(R.drawable.ic_launcher)
                    .show();
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

    @OnClick(R.id.rlNumberCoats)
    public void showNumberPickerDialog(View v) {
        int numcoats = Integer.parseInt(tvNumCoatsValue.getText().toString());
        FragmentManager fm = getChildFragmentManager();
        NumberPickerDialogFragment numberPickerDialogFragment =
                NumberPickerDialogFragment.newInstance("Number of Coats", numcoats);
        numberPickerDialogFragment.show(fm, "fragment_number_picker_dialog");
    }

    @Override
    public void onFinishNumberPickerDialog(int value) {
        tvNumCoatsValue.setText(String.valueOf(value));
        btnSubmitPickup.setEnabled(true);
        ObjectAnimator btnAnim = ObjectAnimator.ofObject(btnSubmitPickup, "backgroundColor", new ArgbEvaluator(),
          /*LightBlue*/0xFF3D89C2, /*Blue*/0xff246d9e);
        btnAnim.setDuration(500).setRepeatCount(ValueAnimator.INFINITE);
        btnAnim.setRepeatMode(ValueAnimator.REVERSE);
        btnAnim.start();

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

    @OnClick(R.id.btnSubmitPickup)
    protected void onSubmitPickup(View v) {
        if (tvNumCoatsValue.getText().toString().equals("0")) {
            //TODO: Highlight rlNumberCoats background to hint user to enter number of coats
        } else {
            ParseUser currUser = ParseUser.getCurrentUser();
            String currUserPhoneNo = currUser.getString("phoneNumber");
            if (currUserPhoneNo == null)
                showConfirmPickupDialog();
            else {
                String existingName = currUser.getString("name");
                prepPickupRequest(existingName, currUserPhoneNo);
                savePickupRequest();
                showOKDialog();
            }

        }
    }

    private void showOKDialog() {
        //stop displaying the spinning indicator
        getActivity().setProgressBarIndeterminateVisibility(false);

        // show submitted confirmation
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pickupRequest_submittedDialog_title)
                .setMessage(R.string.pickupRequest_submittedDialog_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        animateAndDetach();
                    }
                })
                        //TODO: perhaps add another button to this dialog - 'view profile'?
                .setIcon(R.drawable.ic_launcher)
                .show();
    }

    private void showConfirmPickupDialog() {
        FragmentManager fm = getChildFragmentManager();
        ConfirmRequestDialogFragment confirmRequestDialogFragment =
                ConfirmRequestDialogFragment.newInstance("Confirm Pickup");
        confirmRequestDialogFragment.show(fm, "fragment_confirm_request_dialog");
    }


    public void prepPickupRequest(String donorName, String donorNumber) {
        double donationValue;
        String estimatedValueString = etEstimatedValue.getText().toString();
        if (!estimatedValueString.equals("")) {
            donationValue = Double.parseDouble(estimatedValueString);
        } else {
            donationValue = 0.0;
        }

        CharityUserHelper.setNameAndNumber(donorName, donorNumber);

        int numcoats = Integer.parseInt(tvNumCoatsValue.getText().toString());

        mPickupRequest = new PickupRequest(
                new ParseGeoPoint(mLat, mLng),
                donorName,
                mAddress,
                donorNumber,
                ParseUser.getCurrentUser(),
                "Coat",
                donationValue,
                numcoats
        );
    }

    // after donor enters name and number and hits Confirm
    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        prepPickupRequest(name, phoneNumber);
        savePickupRequest();
        showOKDialog();
    }

    private void savePickupRequest() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        mPickupRequest.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                shouldWeRetrySave(e);
            }
        });
    }


    public void shouldWeRetrySave(ParseException e) {
        if (e == null) {
            // saved successfully
            mRequestSubmitted = true;

            // detach this detail fragment, we're done here
            //animateAndDetach();



        } else {
            // save did not succeed
            getActivity().setProgressBarIndeterminateVisibility(false);
            // show error notification dialog with retry or cancel
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pickupRequest_retryDialog_title)
                    .setMessage(R.string.pickupRequest_retryDialog_message)
                    .setPositiveButton(R.string.pickupRequest_retryDialog_retryLabel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with retry
                            savePickupRequest();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        }
    }
}
