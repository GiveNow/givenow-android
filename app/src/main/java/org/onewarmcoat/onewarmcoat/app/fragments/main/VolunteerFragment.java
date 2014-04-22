package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Activity;
import android.os.Bundle;

import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;

public class VolunteerFragment extends PageSlidingTabStripFragment {

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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

//        ActionBar actionBar = getActivity().getActionBar();
//
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //this is ghetto, but just placeholder
//        ((MainActivity) activity).onSectionAttached(2);

//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

//        ActionBar actionBar = getActivity().getActionBar();
//
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        mListener = null;
    }
}
