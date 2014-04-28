package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customviews.AdaptableGradientRectView;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PickUpDetailFragment extends Fragment implements
        NumberPickerDialogFragment.NumberPickerDialogListener,
        ConfirmPickupDialogFragment.ConfirmPickupDialogListener {

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
    private String mAddress;
    private double mLat;
    private double mLng;
    private Animator slide_down_from_top;
    private Animator slide_up_from_bottom;


    public PickUpDetailFragment() {
        // Required empty public constructor
    }

    public static PickUpDetailFragment newInstance(String addr, double lat, double lng) {
        PickUpDetailFragment fragment = new PickUpDetailFragment();
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
        //TODO: Add 'Cancel' button to Actionbar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pick_up_detail, container, false);
        ButterKnife.inject(this, v);

        setAddressFieldText(mAddress);

        //Hack to catch back button and animate away before popping backstack
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i(((Object) this).getClass().getSimpleName(), "keyCode: " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.i(((Object) this).getClass().getSimpleName(), "onKey Back listener is working!!!");
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Animator slide_down_to_bottom = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_down_to_bottom);
                    Animator slide_up_to_top = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_up_to_top);
                    slide_down_to_bottom.setTarget(rlPickupDetailContainer);
                    slide_down_to_bottom.start();

                    slide_up_to_top.setTarget(rlAddressContainer);

                    final FragmentManager fm = getFragmentManager();
                    slide_up_to_top.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            //TODO: maybe fade out?
                            adaptableGradientRectView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fm.popBackStack();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    slide_up_to_top.start();

                    return true;
                } else {
                    return false;
                }
            }
        });

        return v;
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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        slide_down_from_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_from_top);
        slide_up_from_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_from_bottom);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        slide_down_from_top.setTarget(rlAddressContainer);
        slide_down_from_top.start();
        slide_up_from_bottom.setTarget(rlPickupDetailContainer);
        slide_up_from_bottom.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //TODO: Fade in
                adaptableGradientRectView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        slide_up_from_bottom.start();
//        rlAddressContainer.startAnimation(slide_down_from_top);
//        rlPickupDetailContainer.startAnimation(slide_up_from_bottom);
    }

    @Override
    public void onStop() {
        super.onStop();

//        rlAddressContainer.startAnimation(slide_up_from_bottom);
//        rlPickupDetailContainer.startAnimation(slide_down_from_top);
    }

    @OnClick(R.id.btnSubmitPickup)
    protected void onSubmitPickup(View v) {
        //spawn a dialogfragment
        //TODO: populate name + phone number from current user
        showConfirmPickupDialog();
    }

    private void showConfirmPickupDialog() {
        FragmentManager fm = getChildFragmentManager();
        ConfirmPickupDialogFragment confirmPickupDialogFragment =
                ConfirmPickupDialogFragment.newInstance("Confirm Pickup");
        confirmPickupDialogFragment.show(fm, "fragment_confirm_pickup_dialog");
    }

    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        double donationValue;
        String estimatedValueString = etEstimatedValue.getText().toString();
        if (!estimatedValueString.equals("")) {
            donationValue = Double.parseDouble(estimatedValueString);
        } else {
            donationValue = 0.0;
        }


        PickupRequest pickupRequest = new PickupRequest(
                new ParseGeoPoint(mLat, mLng),
                name,
                mAddress,
                phoneNumber,
                ParseUser.getCurrentUser(),
                "Coat",
                donationValue
        );
        pickupRequest.saveInBackground();

        Toast.makeText(getActivity(), "Pickup Confirmed! Saved " + name + " and " + phoneNumber + " to Parse!", Toast.LENGTH_LONG).show();

    }
}
