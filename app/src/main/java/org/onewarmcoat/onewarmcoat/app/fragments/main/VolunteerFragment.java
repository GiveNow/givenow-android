package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.SuperAwesomeCardFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.DropOffLocationsFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickUpRequestsFragment;

public class VolunteerFragment extends PageSlidingTabStripFragment {

    private PickUpRequestsFragment pickUpRequestsFragment;
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

        pickUpRequestsFragment = PickUpRequestsFragment.newInstance();
        dropOffLocationsFragment = DropOffLocationsFragment.newInstance();
    }

    @Override
    protected Fragment getFragmentForPosition(int position) {
        Fragment frag;
        switch (position) {
            case 0: //PickUp Requests
                frag = pickUpRequestsFragment;
                break;
            case 1: //Drop Off Locations
                frag = dropOffLocationsFragment;
                break;
            default:
                Log.d("DonateFragment", "default case hit in getFragmentForPosition, weird tab/position number!");
                frag = SuperAwesomeCardFragment.newInstance(position);
                break;
        }
        return frag;
    }

}
