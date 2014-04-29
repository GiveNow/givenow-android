package org.onewarmcoat.onewarmcoat.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.DonateFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.ProfileFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.VolunteerFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.PickUpDetailFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.PickUpFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.ConfirmPickupLocationFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickUpRequestsFragment;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

public class MainActivity extends Activity implements
        PickUpFragment.PickUpDetailInteractionListener, PickUpRequestsFragment.ConfirmPickupInteractionListener {
//        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
//    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in { #restoreActionBar()}.
     */
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;
    private int mSelectedItem;
    private PickUpDetailFragment pickUpDetailFragment;
    private ConfirmPickupLocationFragment confirmPickupLocationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFragments();
        initializeDrawer();
        if (savedInstanceState == null) {
            //create fragments
            initializeFragments();
//            mCurrentFragment = null;
            mSelectedItem = 0;
            selectItem(mSelectedItem);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSelectedItem", mSelectedItem);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mSelectedItem = inState.getInt("mSelectedItem");
        selectItem(mSelectedItem);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ParseQuery<PickupRequest> query = PickupRequest.getMyPendingRequests();

        //only want to get 1 at a time (there shouldn't be more than 1 anyway)
        query.getFirstInBackground(new GetCallback<PickupRequest>() {
            @Override
            public void done(PickupRequest pickupRequest, ParseException e) {
                if (pickupRequest != null) {
                    Toast.makeText(getBaseContext(), "found pickup confirmation = " + pickupRequest.getNumberOfCoats() + pickupRequest.getDonationType(), Toast.LENGTH_LONG).show();
                    Log.d("query", "me = " + ParseUser.getCurrentUser().getObjectId() + " donor = " + pickupRequest.getDonor().getObjectId() + " pending = " + getId(pickupRequest.getPendingVolunteer()) + " confirmed " + getId(pickupRequest.getConfirmedVolunteer()));

                    //show dialog to user


                    // if user accepts, send push notif to pendingVolunteer, and set confirmedVolunteer

                    //**this works, just needs to be in the accept callback from the dialog
//                    pickupRequest.generateVolunteerConfirmedNotif();
//                    pickupRequest.setconfirmedVolunteer(pickupRequest.getPendingVolunteer());
//                    pickupRequest.saveInBackground();
                }
            }
        });
    }

    //stupid helper method, can go away whenever
    private String getId(ParseUser volunteer) {
        String id = null;

        if(volunteer != null){
            id = volunteer.getObjectId();
        }

        return id;
    }

    private void initializeFragments() {
    }

    private void initializeDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mDrawerTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                Gravity.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for any request codes, then call super, so fragments can catch the result
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int position) {
        //exTODO: hide fragment in container, simplify code

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        //here is the retarded way of doing fragment hiding, in all its shitty glory. BUT IT WORKS
        Fragment don = getFragmentManager().findFragmentByTag("don");
        Fragment vol = getFragmentManager().findFragmentByTag("vol");
        Fragment prof = getFragmentManager().findFragmentByTag("prof");
        switch (position) {
            case 0: //Donate
                if (vol != null) {
                    ft.hide(vol);
                }
                if (prof != null) {
                    ft.hide(prof);
                }
                DonateFragment donateFragment = (DonateFragment) getFragmentManager().findFragmentByTag("don");
                if (donateFragment == null) {
                    donateFragment = DonateFragment.newInstance();
                    Log.w("MainActivity", "Adding donateFragment to content.");
                    ft.add(R.id.content,
                            donateFragment,
//                            donateFragment.TAG);
                            "don");
                } else {
                    ft.show(donateFragment);
                }
                break;
            case 1: //Volunteer
                if (don != null) {
                    ft.hide(don);
                }
                if (prof != null) {
                    ft.hide(prof);
                }
                VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
                if (volunteerFragment == null) {
                    volunteerFragment = VolunteerFragment.newInstance();
                    Log.w("MainActivity", "Adding volunteerFragment to content.");
                    ft.add(R.id.content,
                            volunteerFragment,
//                            volunteerFragment.TAG);
                            "vol");
                } else {
                    ft.show(volunteerFragment);
                }
                break;
            case 2: // Profile
                if (vol != null) {
                    ft.hide(vol);
                }
                if (don != null) {
                    ft.hide(don);
                }
                ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentByTag("prof");
                if (profileFragment == null) {
                    profileFragment = ProfileFragment.newInstance();
                    Log.w("MainActivity", "Adding profileFragment to content.");
                    ft.add(R.id.content,
                            profileFragment,
//                            profileFragment.TAG);
                            "prof");
                } else {
                    ft.show(profileFragment);
                }
                break;
            case 3: //Sign Out
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                //take user to login screen
                finish();
                break;
            default:
                Log.d("MainActivity", "default case hit in selectItem, weird position number!");
                break;
        }
//        ft.addToBackStack(String.valueOf(position));
        ft.commit();
        mSelectedItem = position;
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void onLaunchPickUpDetail(String address, double lat, double lng) {
        pickUpDetailFragment = PickUpDetailFragment.newInstance(address, lat, lng);
        getFragmentManager().beginTransaction()
                .add(R.id.content, pickUpDetailFragment,
                        "PickUpDetailFragment")
                .addToBackStack("PickUpDetailFragment")
                .commit();
    }

    public void updateAddress(Address address) {
        if (pickUpDetailFragment != null) {
            pickUpDetailFragment.setAddressFieldText(address.getAddressLine(0));
        }
    }

    public void onLaunchConfirmPickup(PickupRequest pickupRequest) {
        confirmPickupLocationFragment = ConfirmPickupLocationFragment.newInstance(pickupRequest);
        getFragmentManager().beginTransaction()
                .add(R.id.content, confirmPickupLocationFragment,
                        "ConfirmPickupLocationFragment")
                .addToBackStack("ConfirmPickupLocationFragment")
                .commit();
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }


}
