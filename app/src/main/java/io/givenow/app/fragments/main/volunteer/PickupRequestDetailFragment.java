package io.givenow.app.fragments.main.volunteer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import io.givenow.app.R;
import io.givenow.app.customviews.SlidingRelativeLayout;
import io.givenow.app.helpers.CroutonHelper;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.PickupRequest;

public class PickupRequestDetailFragment extends Fragment {

    @Bind(R.id.rlInfoContainer)
    SlidingRelativeLayout rlInfoContainer;
    @Bind(R.id.rlButtonContainer)
    SlidingRelativeLayout rlButtonContainer;
    @Bind(R.id.tvDonorAddress)
    TextView tvDonorAddress;
    @Bind(R.id.btnClaim)
    Button btnAccept;

    private PickupRequest mPickupRequest;
    private Animator slide_down_from_top;
    private Animator slide_up_to_top;
    private Animator slide_up_from_bottom;
    private Animator slide_down_to_bottom;
    private boolean mKeyCodeBackEventHandled = false;
    private boolean mRequestAccepted = false;
    private PickupRequestConfirmedListener mListener;
    private ObjectAnimator mBtnAnim;

    public PickupRequestDetailFragment() {

    }

    // keeps pickup request bundle 'memory' for retrieval later in onCreateView
    public static PickupRequestDetailFragment newInstance(PickupRequest pickupRequest) {
        PickupRequestDetailFragment f = new PickupRequestDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("mPickupRequest", pickupRequest);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPickupRequest = (PickupRequest) getArguments().getSerializable("mPickupRequest");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View fragmentView = inf.inflate(R.layout.fragment_pickup_request_detail, parent, false);
        ButterKnife.bind(this, fragmentView);

        //Hack to catch back button and animate away before popping backstack
        fragmentView.setFocusableInTouchMode(true);
        fragmentView.requestFocus();
        fragmentView.setOnKeyListener(new View.OnKeyListener() {
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

        mBtnAnim = CustomAnimations.buttonFlashCTA(btnAccept);

        tvDonorAddress.setText(mPickupRequest.getAddress());

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // entering animations
        slide_down_from_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_from_top);
        slide_up_from_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_from_bottom);

        //exiting animations
        slide_up_to_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_to_top);
        slide_down_to_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_to_bottom);

        mListener = (PickupRequestConfirmedListener) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        slide_down_from_top.setTarget(rlInfoContainer);
        slide_up_from_bottom.setTarget(rlButtonContainer);

        AnimatorSet set = new AnimatorSet();
        set.play(slide_down_from_top).with(slide_up_from_bottom);
        set.start();

        mBtnAnim.start();
    }

    public void animateAndDetach() {
        slide_up_to_top.setTarget(rlInfoContainer);
        slide_down_to_bottom.setTarget(rlButtonContainer);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                slide_up_to_top,
                slide_down_to_bottom
        );

        final FragmentManager fragmentManager = getFragmentManager();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationEndedBeforeDetach();
                try {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.remove(PickupRequestDetailFragment.this);
//                     ft.commitAllowingStateLoss(); // SO answer had this, try if problems
                    ft.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    /* on claim, query parse to see if volunteer is either a donor or a previous volunteer.
      If he isn't either pop up dialogfragment for name and phone.*/
    @OnClick(R.id.btnClaim)
    public void OnClaim(Button b) {
//        mPickupRequest.setPendingVolunteer(ParseUser.getCurrentUser());

        showConfirmPickupDialog();

    }

    private void showConfirmPickupDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_volunteer_claim_pickuprequest_title)
                .setMessage(R.string.dialog_volunteer_claim_pickuprequest_message)
                .setPositiveButton(R.string.claim, (dialog, which) -> claimPickupRequest())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void claimPickupRequest() {
        //Claim pickup request (set pending volunteer on this pickup request to current user)
        mPickupRequest.claim().subscribe(
                response -> {
                    Log.d("Cloud Response", response.toString());
                    mRequestAccepted = true;
                    removePinFromMap(); //TODO excise and should probably use parent fragment
//                    mPickupRequest.generatePendingVolunteerAssignedNotif(getActivity());
                    // detach this detail fragment, we're done here
                    animateAndDetach();
                },
                error -> ErrorDialogs.connectionFailure(getActivity(), error) //TODO: maybe implement Retry dialog here?
        );
    }

    private void removePinFromMap() {
        // listener will be implemented by MainActivity
        mListener.onPickupConfirmed(mPickupRequest);
    }
//
//    private void savePickupRequest() {
//        // assign pending volunteer in PickupRequest table and send push notif to donor
//        getActivity().setProgressBarIndeterminateVisibility(true);
//
//        mPickupRequest.saveInBackground(e -> {
//            shouldWeRetrySave(e);
//            removePinFromMap();
//        });
//    }
//
//
//    public void shouldWeRetrySave(ParseException e) {
//        if (e == null) {
//            // saved successfully
//            mRequestAccepted = true;
//            mPickupRequest.generatePendingVolunteerAssignedNotif(getActivity());
//            // detach this detail fragment, we're done here
//            animateAndDetach();
//        } else {
//            // save did not succeed
//            getActivity().setProgressBarIndeterminateVisibility(false);
//
//            // show error notification dialog with retry or cancel
//            new AlertDialog.Builder(getActivity())
//                    .setTitle(R.string.pickupRequest_retryDialog_title)
//                    .setMessage(R.string.pickupRequest_retryDialog_message)
//                    .setPositiveButton(R.string.pickupRequest_retryDialog_retryLabel, (dialog, which) -> savePickupRequest())
//                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
//                        // do nothing
//                    })
//                    .setIconAttribute(android.R.attr.alertDialogIcon)
//                    .show();
//        }
//    }

    public void onAnimationEndedBeforeDetach() {
        if (mRequestAccepted) {
            //stop displaying the spinning indicator
            getActivity().setProgressBarIndeterminateVisibility(false);

            // show accepted confirmation
            Crouton crouton = CroutonHelper.createInfoCrouton(getActivity(), getResources().getString(R.string.acceptRequest_submittedDialog_title), getResources().getString(R.string.acceptRequest_submittedDialog_msg));
            crouton.show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface PickupRequestConfirmedListener {
        void onPickupConfirmed(PickupRequest pickupRequest);
    }
}