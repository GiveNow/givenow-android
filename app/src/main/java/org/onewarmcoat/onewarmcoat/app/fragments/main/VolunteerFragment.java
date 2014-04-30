package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.SuperAwesomeCardFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.DropOffLocationsFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickupRequestsFragment;

public class VolunteerFragment extends PageSlidingTabStripFragment {

    private PickupRequestsFragment pickupRequestsFragment;
    private DropOffLocationsFragment dropOffLocationsFragment;

    public VolunteerFragment() {
        // Required empty public constructor
    }

    public static VolunteerFragment newInstance() {
        VolunteerFragment f = new VolunteerFragment();
        return f;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"PickUp Requests", "DropOff Locations"};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            //create fragments
            pickupRequestsFragment = PickupRequestsFragment.newInstance();
            dropOffLocationsFragment = DropOffLocationsFragment.newInstance();
            Log.w("VolunteerFragment", "onCreate: Fragments created");
        }
    }

    @Override
    protected Fragment getFragmentForPosition(int position) {
        Fragment frag;
        switch (position) {
            case 0: //PickUp Requests
                frag = pickupRequestsFragment;
                break;
            case 1: //Drop Off Locations
                frag = dropOffLocationsFragment;
                break;
            default:
                Log.w("VolunteerFragment", "default case hit in getFragmentForPosition, weird tab/position number!");
                frag = SuperAwesomeCardFragment.newInstance(position);
                break;
        }
        return frag;
    }

    public void loadMarkers() {
        if(pickupRequestsFragment != null){
            Toast.makeText(getActivity(), "query and reload markers", Toast.LENGTH_SHORT).show();
            pickupRequestsFragment.loadMarkers();
        }
    }
}
