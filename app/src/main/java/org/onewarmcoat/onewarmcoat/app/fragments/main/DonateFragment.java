package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;

 import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;
 import org.onewarmcoat.onewarmcoat.app.fragments.SuperAwesomeCardFragment;
 import org.onewarmcoat.onewarmcoat.app.fragments.main.common.DropOffLocationsFragment;
 import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.CashFragment;
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
