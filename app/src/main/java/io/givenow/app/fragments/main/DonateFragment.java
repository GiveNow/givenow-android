package io.givenow.app.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import io.givenow.app.fragments.PageSlidingTabStripFragment;
import io.givenow.app.fragments.SuperAwesomeCardFragment;
import io.givenow.app.fragments.main.common.DropOffLocationsFragment;
import io.givenow.app.fragments.main.donate.RequestPickupFragment;


public class DonateFragment extends PageSlidingTabStripFragment {

     private RequestPickupFragment requestPickupFragment;
     private DropOffLocationsFragment dropoffLocationsFragment;

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
             Log.w("DonateFragment", "onCreate: Fragments created");
         }
         //        setRetainInstance(true); // coupled with changing MyPagerAdapter to extent FragmentPagerAdapter, this seems to work differently but causes subfragment fuckery (they disappear on orientation change)

     }

     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         getChildFragmentManager().putFragment(outState, "requestPickupFragment", requestPickupFragment);
         getChildFragmentManager().putFragment(outState, "dropoffLocationsFragment", dropoffLocationsFragment);
         Log.w("DonateFragment", "onSaveInstanceState: Fragments saved");
     }

     @Override
     public void onActivityCreated(Bundle inState) {
         super.onActivityCreated(inState);
         Log.w("DonateFragment", "onActivityCreated called.");
         if (inState != null) {
             requestPickupFragment = (RequestPickupFragment) getChildFragmentManager().getFragment(inState, "requestPickupFragment");
             dropoffLocationsFragment = (DropOffLocationsFragment) getChildFragmentManager().getFragment(inState, "dropoffLocationsFragment");
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
             default:
                 Log.w("DonateFragment", "default case hit in getFragmentForPosition, weird tab/position number!");
                 frag = SuperAwesomeCardFragment.newInstance(position);
                 break;
         }
         return frag;
     }

     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
     }

     @Override
     public void onPause() {
         super.onPause();
     }

     @Override
     public void onDetach() {
         super.onDetach();
     }


 }
