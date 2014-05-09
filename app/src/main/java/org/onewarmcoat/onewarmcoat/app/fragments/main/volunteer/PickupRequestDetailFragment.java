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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PickupRequestDetailFragment extends Fragment implements
        AcceptPickupDialogFragment.AcceptPickupDialogListener {

    @InjectView(R.id.rlInfoContainer)
    SlidingRelativeLayout rlInfoContainer;
    @InjectView(R.id.rlButtonContainer)
    SlidingRelativeLayout rlButtonContainer;
    @InjectView(R.id.tvDonorName)
    TextView tvDonorName;
    @InjectView(R.id.tvDonorAddress)
    TextView tvDonorAddress;
    @InjectView(R.id.llPhone)
    LinearLayout llPhone;
    @InjectView(R.id.tvPhone)
    TextView tvPhone;
    /*@InjectView(R.id.ivPhoneIcon)
    ImageView ivPhone;*/
    @InjectView(R.id.btnAccept)
    Button btnAccept;
    private PickupRequest pickupRequest;
    private long mTag;
    private Animator slide_down_from_top;
    private Animator slide_up_to_top;
    private Animator slide_up_from_bottom;
    private Animator slide_down_to_bottom;

    public PickupRequestDetailFragment() {

    }


    // keeps pickup request bundle 'memory' for retrieval later in onCreateView
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //pickupRequest = getArguments().getSerializable("pickupRequest");
        }
        getActivity().getActionBar().setTitle("Pickup Confirmation");

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
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    animateAndDetach();
                    return true;
                } else {
                    return false;
                }
            }
        });

        pickupRequest = (PickupRequest) getArguments().getSerializable("pickupRequest");

        tvDonorName.setText(pickupRequest.getName());
        tvDonorAddress.setText(pickupRequest.getAddresss());
        tvPhone.setText(pickupRequest.getPhoneNumber());

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

        ObjectAnimator llAnim = ObjectAnimator.ofObject(llPhone, "backgroundColor", new ArgbEvaluator(),
                0xffdde8ed, 0xffffffff);
        llAnim.setDuration(500).setRepeatCount(ValueAnimator.INFINITE);
        llAnim.setRepeatMode(ValueAnimator.REVERSE);
        llAnim.start();
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


    /* on accept, query parse to see if volunteer is either a donor or a previous volunteer.
      If he isn't either pop up dialogfragment for name and phone.*/
    @OnClick(R.id.btnAccept)
    public void onAccept(Button b) {

        // assign pending volunteer in PickupRequest table and send push notif to donor
        pickupRequestHelper();

        ParseUser currUser = ParseUser.getCurrentUser();
        String currUserPhoneNo = currUser.getString("phoneNumber");
        if (currUserPhoneNo == null) {
            FragmentManager fm = getChildFragmentManager();
            AcceptPickupDialogFragment acceptanceDialogFragment =
                    AcceptPickupDialogFragment.newInstance("Confirm Pickup");
            acceptanceDialogFragment.show(fm, "fragment_accept_pickup_dialog");
        } else {
            animateAndDetach();
        }

        /*ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<PickupRequest> donorQuery = ParseQuery.getQuery(PickupRequest.class);
        donorQuery.whereEqualTo("donor", currUser);
        ParseQuery<PickupRequest> pendingVolunteerQuery = ParseQuery.getQuery(PickupRequest.class);
        pendingVolunteerQuery.whereEqualTo("pendingVolunteer", currUser);

        List<ParseQuery<PickupRequest>> queries = new ArrayList<ParseQuery<PickupRequest>>();
        queries.add(donorQuery);
        queries.add(pendingVolunteerQuery);

        ParseQuery<PickupRequest> compoundQuery = ParseQuery.or(queries);
        compoundQuery.findInBackground(new FindCallback<PickupRequest>() {
            // CALLBACK FUNCTION, QUERY HAS FINISHED IF ENTERING THIS FUNCTION
            public void done(List<PickupRequest> results, ParseException e) {
                // not an existing donor or volunteer
                if(results.size() == 0) {

                    Activity act = getActivity();
                    FragmentManager fm = getChildFragmentManager();
                    AcceptPickupDialogFragment acceptanceDialogFragment =
                            AcceptPickupDialogFragment.newInstance("Confirm Pickup");
                    acceptanceDialogFragment.show(fm, "fragment_accept_pickup_dialog");

                }
                // IS an existing donor or volunteer
                else {
                    animateAndDetach();
                }

            }
        }); */
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    @OnClick(R.id.llPhone)
    public void onCallDonor(LinearLayout phoneRow) {
        Toast.makeText(getActivity(), "Phone call callback activated!", Toast.LENGTH_LONG);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        String donorPhoneNum = pickupRequest.getPhoneNumber();
        donorPhoneNum = donorPhoneNum.replaceAll("[^0-9]", "");
        String uriStr = "tel:" + donorPhoneNum;
        callIntent.setData(Uri.parse(uriStr));
        startActivity(callIntent);
    }


    /* after clicking accept in the ConfirmRequestDialogFragment update User with
     * volunteer name and phone
     */
    @Override
    public void onConfirmAcceptDialog(String name, String phoneNumber) {
        CharityUserHelper cuh = new CharityUserHelper();
        cuh.setNameAndNumber(name, phoneNumber);
        Toast.makeText(getActivity(), "Thanks " + name + "!", Toast.LENGTH_LONG).show();
        animateAndDetach();
    }


    public void pickupRequestHelper() {
        //set the pending volunteer on the PickupRequest.  This marks the pickup request as pending, and not shown on the map to other volunteers
        pickupRequest.setPendingVolunteer(ParseUser.getCurrentUser());
        pickupRequest.saveInBackground();
        Activity currActivity = getActivity();
        //Toast.makeText(currActivity, "Thank you!", Toast.LENGTH_SHORT).show();
        pickupRequest.generatePendingVolunteerAssignedNotif();

    }
}