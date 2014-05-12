package org.onewarmcoat.onewarmcoat.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.DonateFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.VolunteerFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.RequestPickupDetailFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.RequestPickupFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.profile.ProfileFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickupRequestDetailFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickupRequestsFragment;
import org.onewarmcoat.onewarmcoat.app.helpers.CroutonHelper;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends Activity implements
        RequestPickupFragment.PickUpDetailInteractionListener,
        PickupRequestsFragment.PickupRequestDetailInteractionListener,
        PickupRequestDetailFragment.PickupRequestConfirmedListener {
    private static final int POSITION_DONATE = 0;
    private static final int POSITION_VOLUNTEER = 1;
    private static final int POSITION_PROFILE = 2;
    private static final int POSITION_SIGN_OUT = 3;
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
    private RequestPickupDetailFragment requestPickUpDetailFragment;
    private PickupRequestDetailFragment pickupRequestDetailFragment;
    private AlertDialog acceptPendingDialog;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_main);

        mIntent = getIntent();
        initializeFragments();
        initializeDrawer();
        if (savedInstanceState == null) {
            //create fragments
            initializeFragments();
//            mCurrentFragment = null;
            mSelectedItem = 0;
            selectItem(mSelectedItem);
        }

        //TODO: Open Navigation drawer on first launch to hint user that navigation drawer exists, per google UX design spec

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

        //if the user resumed the app by entering through a Volunteer push notif, show dashboard
        boolean isPushNotif = handlePushNotifResume();

        if (!isPushNotif) {
            checkForPendingRequests();
        }
    }

    private boolean handlePushNotifResume() {
//        Log.d("detectPushNotificationMessage", "handlePushNotif");
        String notificationData = mIntent.getStringExtra("com.parse.Data");
        if (notificationData != null) {
//                Log.d("detectPushNotificationMessage", "notificationData =" + notificationData);

            try {
                JSONObject json = new JSONObject(notificationData);
//                    Log.d("detectPushNotificationMessage", "made json object " + json.toString());

                String notifType = json.getString("type");
//                    Log.d("detectPushNotificationMessage", "notifType = " + notifType);
                if (notifType.equals(PickupRequest.VOLUNTEER_CONFIRMED)) {
//                        Log.d("detectPushNotificationMessage", "switch to volunteer fragment");

                    //remove the push notif data, so we don't process it next app resume
                    mIntent.removeExtra("com.parse.Data");

                    //try setting us to the Volunteer fragment
                    selectItem(POSITION_VOLUNTEER);
                    //move to dashboard duey
//                    VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
                    //0 means dashboard
//                    volunteerFragment.setCurrentItem(0);

                    return true;
                }else if(notifType.equals(PickupRequest.PICKUP_COMPLETE)){
                    //remove the push notif data, so we don't process it next app resume
                    mIntent.removeExtra("com.parse.Data");

                    String title = getResources().getString(R.string.donate_success);
                    String message = getResources().getString(R.string.donate_pickup_complete);

                    Crouton crouton = CroutonHelper.createInfoCrouton(this, title, message);
                    crouton.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void checkForPendingRequests() {
        ParseQuery<PickupRequest> query = PickupRequest.getMyPendingRequests();

        //only want to get 1 at a time (there shouldn't be more than 1 anyway)
        query.getFirstInBackground(new GetCallback<PickupRequest>() {
            @Override
            public void done(PickupRequest pickupRequest, ParseException e) {
                if (pickupRequest != null) {
//                    Toast.makeText(getBaseContext(), "found pickup confirmation = " + pickupRequest.getNumberOfCoats() + pickupRequest.getDonationType(), Toast.LENGTH_LONG).show();
                    Log.d("query", "me = " + ParseUser.getCurrentUser().getObjectId() + " donor = " + pickupRequest.getDonor().getObjectId() + " pending = " + getId(pickupRequest.getPendingVolunteer()) + " confirmed " + getId(pickupRequest.getConfirmedVolunteer()));

                    //show dialog to user
                    createAcceptPendingVolunteerDialog(pickupRequest);
                }
            }
        });
    }

    private void createAcceptPendingVolunteerDialog(final PickupRequest pickupRequest) {
        ParseUser pendingVolunteer = pickupRequest.getPendingVolunteer();
        String title = CharityUserHelper.getFirstName(CharityUserHelper.getName(pendingVolunteer)) + " would like to pick up your coats!";
        String address = "<br><br><font color='#858585'>Address: " + pickupRequest.getAddresss() + "</font>";

        if (acceptPendingDialog != null && acceptPendingDialog.isShowing()) {
            acceptPendingDialog.dismiss();
        }
        acceptPendingDialog = new AlertDialog.Builder(this)
//                .setTitle(R.string.acceptRequest_submittedDialog_title)
//                .setMessage(R.string.acceptRequest_submittedDialog_msg)
                .setTitle(Html.fromHtml("<font color='#246d9e'>" + title + "</font>"))
                .setMessage(Html.fromHtml("Is your donation of " + pickupRequest.getNumberOfCoats() + " coats available for pickup today?" + address))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pendingVolunteerConfirmed(pickupRequest);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cancelPendingVolunteer(pickupRequest);
                    }
                })
                .setIcon(R.drawable.ic_launcher)
                .show();
    }

    private void cancelPendingVolunteer(PickupRequest pickupRequest) {
        //donor doesn't accept volunteer request, so remove the pending volunteer
        pickupRequest.remove("pendingVolunteer");
        pickupRequest.saveInBackground();
    }

    private void pendingVolunteerConfirmed(final PickupRequest pickupRequest) {
        // if user accepts, send push notif to pendingVolunteer, and set confirmedVolunteer
        pickupRequest.generateVolunteerConfirmedNotif();
        pickupRequest.setconfirmedVolunteer(pickupRequest.getPendingVolunteer());

        //removed this, and added it to done method below
        pickupRequest.saveInBackground();
    }

    //stupid helper method, can go away whenever
    private String getId(ParseUser volunteer) {
        String id = null;

        if (volunteer != null) {
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
        DonateFragment donateFragment = (DonateFragment) getFragmentManager().findFragmentByTag("don");
        VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentByTag("prof");
        switch (position) {
            case POSITION_DONATE: //Donate
                if (volunteerFragment != null) {
                    ft.hide(volunteerFragment);
                }
                if (profileFragment != null) {
                    ft.hide(profileFragment);
                }

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
            case POSITION_VOLUNTEER: //Volunteer
                if (donateFragment != null) {
                    ft.hide(donateFragment);
                }
                if (profileFragment != null) {
                    ft.hide(profileFragment);
                }

                if (volunteerFragment == null) {
                    volunteerFragment = VolunteerFragment.newInstance();
                    Log.w("MainActivity", "Adding volunteerFragment to content.");
                    ft.add(R.id.content,
                            volunteerFragment,
//                            volunteerFragment.TAG);
                            "vol");
                } else {
                    volunteerFragment.loadMarkers();
                    ft.show(volunteerFragment);
                }
                break;
            case POSITION_PROFILE: // Profile
                if (volunteerFragment != null) {
                    ft.hide(volunteerFragment);
                }
                if (donateFragment != null) {
                    ft.hide(donateFragment);
                }

                if (profileFragment == null) {
                    profileFragment = ProfileFragment.newInstance();
                    Log.w("MainActivity", "Adding profileFragment to content.");
                    ft.add(R.id.content,
                            profileFragment,
//                            profileFragment.TAG);
                            "prof");
                } else {
                    profileFragment.refreshProfile();
                    ft.show(profileFragment);
                }
                break;
            case POSITION_SIGN_OUT: //Sign Out
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

    public void onLaunchRequestPickUpDetail(String address, double lat, double lng) {
        requestPickUpDetailFragment = RequestPickupDetailFragment.newInstance(address, lat, lng);
        getFragmentManager().beginTransaction()
                .add(R.id.content, requestPickUpDetailFragment,
                        "RequestPickupDetailFragment")
                .addToBackStack("RequestPickupDetailFragment")
                .commit();
    }

    public void updateAddress(Address address) {
        if (requestPickUpDetailFragment != null) {
            requestPickUpDetailFragment.setAddressFieldText(address.getAddressLine(0));
        }
    }

    public void onLaunchPickupRequestDetail(PickupRequest pickupRequest) {
        if (pickupRequestDetailFragment != null) {
            if (pickupRequestDetailFragment.isAdded()) {
                //already displaying a detail fragment, close that one
                pickupRequestDetailFragment.animateAndDetach();
            }
        }
        pickupRequestDetailFragment = PickupRequestDetailFragment.newInstance(pickupRequest);
        getFragmentManager().beginTransaction()
                .add(R.id.content, pickupRequestDetailFragment,
                        "PickupRequestDetailFragment")
                .addToBackStack("PickupRequestDetailFragment")
                .commit();
    }

    @Override
    public void onPickupConfirmed(PickupRequest pickupRequest) {
        VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
        if (volunteerFragment != null) {
            // removing individual marker for this pickup request
            // won't work if the orientation changed during the PickupConfirmDialog, because the pickupRequest we get back from the dialog
            // isn't the same one that's now in the since-recreated volunteerFragment.
            volunteerFragment.removePickupRequestFromMap(pickupRequest);
        }
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
