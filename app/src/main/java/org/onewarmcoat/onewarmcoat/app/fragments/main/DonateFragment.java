package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.SuperAwesomeCardFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.CashFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.DropOffLocationsFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.RequestPickupFragment;


public class DonateFragment extends PageSlidingTabStripFragment {

    private RequestPickupFragment requestPickupFragment;
    private DropOffLocationsFragment dropoffLocationsFragment;
    private CashFragment cashFragment;

    public DonateFragment() {
        // Required empty public constructor
    }
    
//    private OnFragmentInteractionListener mListener;

    public static DonateFragment newInstance() {
        DonateFragment f = new DonateFragment();
        return f;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"Request PickUp", "DropOff", "Donate Cash"};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            //create fragments
            requestPickupFragment = RequestPickupFragment.newInstance();
            dropoffLocationsFragment = DropOffLocationsFragment.newInstance();
            cashFragment = CashFragment.newInstance();
            Log.w("DonateFragment", "onCreate: Fragments created");
        }
//        else {
//            // restore fragments
//            requestPickupFragment = (RequestPickupFragment) getFragmentManager()..findFragmentByTag("don");
//            dropoffLocationsFragment = (DropOffLocationsFragment) getFragmentManager().findFragmentByTag("vol");
//            cashFragment = (CashFragment) getFragmentManager().findFragmentByTag("prof");
//            Log.d("MainActivity", "Fragments restored");
//        }
//        setRetainInstance(true); // coupled with changing MyPagerAdapter to extent FragmentPagerAdapter, this seems to work differently but causes subfragment fuckery (they disappear on orientation change)

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getChildFragmentManager().putFragment(outState, "requestPickupFragment", requestPickupFragment);
        getChildFragmentManager().putFragment(outState, "dropoffLocationsFragment", dropoffLocationsFragment);
        getChildFragmentManager().putFragment(outState, "cashFragment", cashFragment);
        Log.w("DonateFragment", "onSaveInstanceState: Fragments saved");
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        Log.w("DonateFragment", "onActivityCreated called.");
        if (inState != null) {
            // ain't reaching here for some reason
            requestPickupFragment = (RequestPickupFragment) getChildFragmentManager().getFragment(inState, "requestPickupFragment");
            dropoffLocationsFragment = (DropOffLocationsFragment) getChildFragmentManager().getFragment(inState, "dropoffLocationsFragment");
            cashFragment = (CashFragment) getChildFragmentManager().getFragment(inState, "cashFragment");
            Log.w("DonateFragment", "onActivityCreated: Fragments restored");
        }
    }

    @Override
    protected Fragment getFragmentForPosition(int position) {
        Fragment frag;
        switch (position) {
            case 0: //Pickup
                frag = requestPickupFragment;
                break;
            case 1: //Dropoff
                frag = dropoffLocationsFragment;
                break;
            case 2: //Donate Cash
                frag = cashFragment;
                break;
            default:
                Log.w("DonateFragment", "default case hit in getFragmentForPosition, weird tab/position number!");
                frag = SuperAwesomeCardFragment.newInstance(position);
                break;
        }
        return frag;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View root = inflater.inflate(R.layout.fragment_donate, container, false);
//
//        return root;
//    }

//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();


//        ActionBar actionBar = getActivity().getActionBar();
//
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        mListener = null;
    }
//
//    @Override
//    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//    }
//
//    @Override
//    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//        Log.d("donatefrag", "tabunselected");
//
//    }
//
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
//
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
