package io.givenow.app.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;

import io.givenow.app.R;
import io.givenow.app.fragments.PageSlidingTabStripFragment;
import io.givenow.app.fragments.SuperAwesomeCardFragment;
import io.givenow.app.fragments.main.common.DropOffLocationsFragment;
import io.givenow.app.fragments.main.volunteer.DashboardFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import io.givenow.app.models.Volunteer;

public class VolunteerFragment extends PageSlidingTabStripFragment {

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
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(btn -> {
            Volunteer volunteer = new Volunteer(ParseUser.getCurrentUser(), false);
            volunteer.saveInBackground(e -> {
                if (e == null) {
                    uiAwaitingApproval(view, button);
                }
            });
        });
        Volunteer.findUser(ParseUser.getCurrentUser()).subscribe(
                volunteer -> {
                    if (volunteer.isApproved()) {
                        //Approved volunteer
                        //Create volunteer fragment
                        Log.w("VolunteerFragment", "Approved volunteer");
                        view.findViewById(R.id.overlay).setVisibility(View.GONE);
                    } else {
                        //Awaiting approval
                        uiAwaitingApproval(view, button);
                    }
                },
                error -> {
                    //Never applied to be a volunteer
                    Log.w("VolunteerFragment", "Never applied to be a volunteer");
                    view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.description)).setText("Want to volunteer to pickup donations?\nThe only thing you need is a car and some spare time!");
                    button.setText("Apply to volunteer");
                });
    }

    private void uiAwaitingApproval(View view, Button button) {
        Log.w("VolunteerFragment", "Awaiting approval");
        view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.description)).setText("Thanks for applying to volunteer!\nWe'll contact you soon at " + ParseUserHelper.getPhoneNumber());
        button.setText("You applied to volunteer");
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
