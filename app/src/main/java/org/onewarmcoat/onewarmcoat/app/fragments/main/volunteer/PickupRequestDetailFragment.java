package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PickupRequestDetailFragment extends Fragment {
    private static PickupRequest pickupRequest;
    @InjectView(R.id.rlInfoContainer)
    SlidingRelativeLayout rlInfoContainer;
    @InjectView(R.id.rlButtonContainer)
    SlidingRelativeLayout rlButtonContainer;
    @InjectView(R.id.tvDonorName)
    TextView tvDonorName;
    @InjectView(R.id.tvDonorAddress)
    TextView tvDonorAddress;
    @InjectView(R.id.btnAccept)
    Button btnAccept;
    private long mTag;
    private Animator slide_down_from_top;
    private Animator slide_up_to_top;
    private Animator slide_up_from_bottom;
    private Animator slide_down_to_bottom;
    private boolean mKeyCodeBackEventHandled = false;

    public PickupRequestDetailFragment() {

    }

    // keeps name and addr in bundle 'memory' for retrieval later in onCreateView
    public static PickupRequestDetailFragment newInstance(PickupRequest pickupRequest) {
        PickupRequestDetailFragment f = new PickupRequestDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("pickupRequest", pickupRequest);
        f.setArguments(args);
        f.setGeneratedTag(System.currentTimeMillis());
        return f;
    }

    public String getGeneratedTag() {
        return String.valueOf(mTag);
    }

    public void setGeneratedTag(long mTag) {
        this.mTag = mTag;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View fragmentView = inf.inflate(R.layout.fragment_pickup_request_detail, parent, false);
        ButterKnife.inject(this, fragmentView);
        setHasOptionsMenu(true);

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

        /* name could be anything ("My Clothes", "Two Fur Coats", "Alex Tam", etc
         * name originated from scraped javascript
         */
        pickupRequest = (PickupRequest) getArguments().getSerializable("pickupRequest");

        tvDonorName.setText(pickupRequest.getName());
        tvDonorAddress.setText(pickupRequest.getAddresss());

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pickup_request_detail_menu, menu);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // entering animations
        slide_down_from_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_from_top);
        slide_up_from_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_from_bottom);

        //exiting animations
        slide_up_to_top = AnimatorInflater.loadAnimator(activity, R.animator.slide_up_to_top);
        slide_down_to_bottom = AnimatorInflater.loadAnimator(activity, R.animator.slide_down_to_bottom);
    }

    @Override
    public void onStart() {
        super.onStart();
        slide_down_from_top.setTarget(rlInfoContainer);
        slide_up_from_bottom.setTarget(rlButtonContainer);

        AnimatorSet set = new AnimatorSet();
        set.play(slide_down_from_top).with(slide_up_from_bottom);
        set.start();

        ObjectAnimator btnAnim = ObjectAnimator.ofObject(btnAccept, "backgroundColor", new ArgbEvaluator(),
          /*LightBlue*/0xFF3D89C2, /*Blue*/0xff246d9e);
        btnAnim.setDuration(500).setRepeatCount(ValueAnimator.INFINITE);
        btnAnim.setRepeatMode(ValueAnimator.REVERSE);
        btnAnim.start();
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
//                onAnimationEndedBeforeDetach();
                fragmentManager.popBackStack(getGeneratedTag(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    @OnClick(R.id.btnAccept)
    public void onAccept(Button b) {
        //TODO: Add indeterminate progress bar
        //this should be set when the volunteer confirms pickup
//                Donation row1 = new Donation(pickupRequest.getDonor(), Donation.COAT, 100);
//                row1.saveInBackground();

        //set the pending volunteer on the PickupRequest.  This marks the pickup request as pending, and not shown on the map to other volunteers
        pickupRequest.setPendingVolunteer(ParseUser.getCurrentUser());
        pickupRequest.saveInBackground();
//        Toast.makeText(getActivity(), "saved the current volunteer as pending", Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), "Thank you!", Toast.LENGTH_SHORT).show();

        pickupRequest.generatePendingVolunteerAssignedNotif();

        //somehow need to get pickupRequestsFragment, so we can reload markers
//        pickupRequestsFragment.loadMarkers();

        //launch congrats fragment, waiting for user to confirm

        //need to re-draw pin in new color

//        getActivity().getFragmentManager().popBackStack();
        animateAndDetach();
    }
}