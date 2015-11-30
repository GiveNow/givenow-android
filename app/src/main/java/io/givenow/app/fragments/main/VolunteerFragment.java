package io.givenow.app.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.fragments.PageSlidingTabStripFragment;
import io.givenow.app.fragments.PhoneNumberVerificationFragment;
import io.givenow.app.fragments.PhoneNumberVerificationFragmentBuilder;
import io.givenow.app.fragments.SuperAwesomeCardFragment;
import io.givenow.app.fragments.main.common.DropOffLocationsFragment;
import io.givenow.app.fragments.main.volunteer.DashboardFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import io.givenow.app.models.Volunteer;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

public class VolunteerFragment extends PageSlidingTabStripFragment
        implements PhoneNumberVerificationFragment.OnUserLoginCompleteListener {

    @Bind(R.id.button)
    Button button;
    @Bind(R.id.description)
    TextView tvDescription;
    @Bind((R.id.overlay))
    LinearLayout llOverlay;
    private PickupRequestsFragment pickupRequestsFragment;
    private DropOffLocationsFragment dropOffLocationsFragment;
    private DashboardFragment dashboardFragment;

    public VolunteerFragment() {
        // Required empty public constructor
    }

    public static VolunteerFragment newInstance() {
        VolunteerFragment f = new VolunteerFragment();
        return f;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"My Dashboard", "PickUp Requests"};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            //create fragments
            pickupRequestsFragment = PickupRequestsFragment.newInstance();
//            dropOffLocationsFragment = DropOffLocationsFragment.newInstance();
            dashboardFragment = DashboardFragment.newInstance();
            Log.w("VolunteerFragment", "onCreate: Fragments created");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); //TODO maybe dont even initialize the tabs if volunteer not approved
        Log.w("VolunteerFragment", "onViewCreated");
        ButterKnife.bind(this, view);

        button.setOnClickListener(btn -> {
            if (ParseUserHelper.isRegistered()) {
                applyToVolunteer();
            } else {
                //show phone number dialog
                new PhoneNumberVerificationFragmentBuilder()
                        .messageResource(R.string.dialog_phoneNumber_for_volunteer)
                        .build()
                        .show(getChildFragmentManager(), "phdialog");
            }
        });
        Volunteer.findUser(ParseUser.getCurrentUser()).subscribe(
                volunteer -> {
                    uiVolunteerApplied(volunteer);
                },
                error -> {
                    //Never applied to be a volunteer
                    Log.w("VolunteerFragment", "Never applied to be a volunteer");
                    llOverlay.setVisibility(View.VISIBLE);
                    tvDescription.setText(R.string.volunteer_label_user_has_not_applied);
                    button.setText(R.string.volunteer_button_user_has_not_applied);
                });
    }

    @Override
    public void onUserLoginComplete() {
        applyToVolunteer();
    }

    private void applyToVolunteer() {
        Volunteer.findUser(ParseUser.getCurrentUser()).subscribe(
                volunteer -> {
                    uiVolunteerApplied(volunteer);
                },
                error -> {
                    // User logged in, and they never applied to be a volunteer before.
                    // Add them to volunteer table.
                    Log.w("VolunteerFragment", "User logged in, and they never applied to be a volunteer before. Adding them to volunteer table.");
                    Volunteer volunteer = new Volunteer(ParseUser.getCurrentUser(), false);
                    ParseObservable.save(volunteer).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(vol -> uiAwaitingApproval());
                });

    }

    private void uiVolunteerApplied(Volunteer volunteer) {
        if (volunteer.isApproved()) {
            //Approved volunteer
            //Create volunteer fragment
            Log.w("VolunteerFragment", "Approved volunteer");
            llOverlay.setVisibility(View.GONE);
        } else {
            //Awaiting approval
            uiAwaitingApproval();
        }
    }

    private void uiAwaitingApproval() {
        Log.w("VolunteerFragment", "Awaiting approval");
        llOverlay.setVisibility(View.VISIBLE);
        tvDescription.setText(getString(R.string.volunteer_label_user_has_applied, ParseUserHelper.getPhoneNumber()));
        button.setText(R.string.volunteer_button_user_has_applied);
        button.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getChildFragmentManager().putFragment(outState, "pickupRequestsFragment", pickupRequestsFragment);
//        getChildFragmentManager().putFragment(outState, "dropoffLocationsFragment", dropOffLocationsFragment);
        getChildFragmentManager().putFragment(outState, "dashboardFragment", dashboardFragment);
        Log.w("VolunteerFragment", "onSaveInstanceState: Fragments saved");
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        Log.w("VolunteerFragment", "onActivityCreated called.");
        if (inState != null) {
            pickupRequestsFragment = (PickupRequestsFragment) getChildFragmentManager().getFragment(inState, "pickupRequestsFragment");
//            dropOffLocationsFragment = (DropOffLocationsFragment) getChildFragmentManager().getFragment(inState, "dropoffLocationsFragment");
            dashboardFragment = (DashboardFragment) getChildFragmentManager().getFragment(inState, "dashboardFragment");
            Log.w("VolunteerFragment", "onActivityCreated: Fragments restored");
        }
    }

    @Override
    protected Fragment getFragmentForPosition(int position) {
        Fragment frag;
        switch (position) {
            case 0: //Dashboard
                frag = dashboardFragment;
                break;
            case 1: //PickUp Requests
                frag = pickupRequestsFragment;
                break;
//            case 2: //Drop Off Locations
//                frag = dropOffLocationsFragment;
//                break;
            default:
                Log.w("VolunteerFragment", "default case hit in getFragmentForPosition, weird tab/position number!");
                frag = SuperAwesomeCardFragment.newInstance(position);
                break;
        }
        return frag;
    }

    public void loadMarkers() {
        //TODO: probably just move this to PickupRequestFragment onStart / onResume
        if (pickupRequestsFragment != null) {

//            Toast.makeText(getActivity(), "query and reload markers", Toast.LENGTH_SHORT).show();
            pickupRequestsFragment.loadMarkers();
        }
    }

    public void removePickupRequestFromMap(PickupRequest pickupRequest) {
        if (pickupRequestsFragment != null) {
            pickupRequestsFragment.removePickupRequestFromMap(pickupRequest);
        }
    }
}
