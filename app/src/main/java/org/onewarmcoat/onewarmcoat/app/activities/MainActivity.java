package org.onewarmcoat.onewarmcoat.app.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.VolunteerFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.DropOffLocationsFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.RequestPickupDetailFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.donate.RequestPickupFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.profile.ProfileFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickupRequestDetailFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickupRequestsFragment;
import org.onewarmcoat.onewarmcoat.app.helpers.CroutonHelper;
import org.onewarmcoat.onewarmcoat.app.models.ParseUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import fj.data.Option;

public class MainActivity extends BaseActivity implements
        RequestPickupFragment.PickUpDetailInteractionListener,
        PickupRequestsFragment.PickupRequestDetailInteractionListener,
        PickupRequestDetailFragment.PickupRequestConfirmedListener,
        NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    private int mSelectedItemId;
    private RequestPickupDetailFragment requestPickUpDetailFragment;
    private PickupRequestDetailFragment pickupRequestDetailFragment;
    private AlertDialog acceptPendingDialog;
    private Intent mIntent;
    private Fragment fragToHide = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Now that we use a toolbar, the action bar progressbar doesn't exist anymore.
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        initializeDrawer();

        if (savedInstanceState == null) {
            mSelectedItemId = R.id.navigation_give;
            selectItem(mSelectedItemId);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mSelectedItemId", mSelectedItemId);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mSelectedItemId = inState.getInt("mSelectedItemId");
        selectItem(mSelectedItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if the user resumed the app by entering through a Volunteer push notif, show dashboard
        boolean isPushNotif = handlePushNotifResume();

        if (isPushNotif) {
            selectItem(R.id.navigation_volunteer);
        } else {
            checkForPendingRequests();
//            selectItem(mSelectedItemId);
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
//                    selectItem(POSITION_VOLUNTEER);
                    //move to dashboard duey
//                    VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
                    //0 means dashboard
//                    volunteerFragment.setCurrentItem(0);

                    return true;
                } else if (notifType.equals(PickupRequest.PICKUP_COMPLETE)) {
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
        query.getFirstInBackground((pickupRequest, e) -> {
            if (pickupRequest != null) {
//                    Toast.makeText(getBaseContext(), "found pickup confirmation = " + pickupRequest.getNumberOfCoats() + pickupRequest.getDonationCategories(), Toast.LENGTH_LONG).show();
                Log.d("query", "me = " + ParseUser.getCurrentUser().getObjectId() + " donor = " + pickupRequest.getDonor().getObjectId() + " pending = " + getId(pickupRequest.getPendingVolunteer()) + " confirmed " + getId(pickupRequest.getConfirmedVolunteer()));

                //show dialog to user
                createAcceptPendingVolunteerDialog(pickupRequest);
            }
        });
    }

    private void createAcceptPendingVolunteerDialog(final PickupRequest pickupRequest) {
        ParseUser pendingVolunteer = pickupRequest.getPendingVolunteer();
        String title = ParseUserHelper.getFirstName(ParseUserHelper.getName(pendingVolunteer)) + " would like to pick up your coats!";
        String address = "<br><br><font color='#858585'>Address: " + pickupRequest.getAddresss() + "</font>";

        if (acceptPendingDialog != null && acceptPendingDialog.isShowing()) {
            acceptPendingDialog.dismiss();
        }
        acceptPendingDialog = new AlertDialog.Builder(this)
//                .setTitle(R.string.acceptRequest_submittedDialog_title)
//                .setMessage(R.string.acceptRequest_submittedDialog_msg)
                .setTitle(Html.fromHtml("<font color='#246d9e'>" + title + "</font>")) //TODO: fix message
                .setMessage(Html.fromHtml("Is your donation of " + pickupRequest.getDonationCategories().toString() + " available for pickup today?" + address))
                .setPositiveButton("Yes", (dialog, which) -> pendingVolunteerConfirmed(pickupRequest))
                .setNegativeButton("No", (dialog, which) -> cancelPendingVolunteer(pickupRequest))
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
        pickupRequest.generateVolunteerConfirmedNotif(this);
        pickupRequest.setConfirmedVolunteer(pickupRequest.getPendingVolunteer());

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

    private void initializeDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(this);

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                ImageView navigation_profile_image = (ImageView) drawerView.findViewById(R.id.navigation_profile_image);
                TextView navigation_label_username = (TextView) drawerView.findViewById(R.id.navigation_label_username);
                TextView navigation_label_phone = (TextView) drawerView.findViewById(R.id.navigation_label_phone);

                if (!ParseUserHelper.isStillAnonymous()) {
                    //User is not anonymous
                    navigation_label_username.setText(ParseUserHelper.getName());
                    navigation_label_phone.setText(ParseUserHelper.getPhoneNumber());
                    Option.fromNull(ParseUserHelper.getProfileImage()).foreachDoEffect(parseFile ->
                            Picasso.with(getApplicationContext()).load(parseFile.getUrl()).into(navigation_profile_image));
                }

                navigation_profile_image.setOnClickListener(v -> {
                    //Deselect current item and close drawer
                    navigationView.getMenu().findItem(mSelectedItemId).setChecked(false);
                    mDrawerLayout.closeDrawers();
                    //Select Profile page
                    selectItem(R.id.navigation_profile_image);
                });
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for any request codes, then call super, so fragments can catch the result
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int itemId) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

//        Fragment fragToHide = getFragmentManager().findFragmentById(R.id.content_frame);
        if (fragToHide != null) {
            ft.hide(fragToHide);
        }

        RequestPickupFragment requestPickupFragment = (RequestPickupFragment) getFragmentManager().findFragmentByTag("don");
        VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentByTag("prof");
        DropOffLocationsFragment dropoffFragment = (DropOffLocationsFragment) getFragmentManager().findFragmentByTag("drop");
        switch (itemId) {
            case R.id.navigation_give: //Donate
                if (requestPickupFragment == null) {
                    requestPickupFragment = RequestPickupFragment.newInstance();
                    Log.w("MainActivity", "Adding requestPickupFragment to content.");
                    ft.add(R.id.content_frame,
                            requestPickupFragment,
                            "don");
                } else {
                    ft.show(requestPickupFragment);
                }
                fragToHide = requestPickupFragment;
                break;
            case R.id.navigation_volunteer: //Volunteer
                if (volunteerFragment == null) {
                    volunteerFragment = VolunteerFragment.newInstance();
                    Log.w("MainActivity", "Adding volunteerFragment to content.");
                    ft.add(R.id.content_frame,
                            volunteerFragment,
                            "vol");
                } else {
                    volunteerFragment.loadMarkers();
                    ft.show(volunteerFragment);
                }
                fragToHide = volunteerFragment;
                break;
            case R.id.navigation_dropoff: //Dropoff Centers
                if (dropoffFragment == null) {
                    dropoffFragment = DropOffLocationsFragment.newInstance();
                    Log.w("MainActivity", "Adding dropoffFragment to content.");
                    ft.add(R.id.content_frame,
                            dropoffFragment,
                            "drop");
                } else {
                    ft.show(dropoffFragment);
                }
                fragToHide = dropoffFragment;
                break;
            case R.id.navigation_profile_image: // Profile
                if (profileFragment == null) {
                    profileFragment = ProfileFragment.newInstance();
                    Log.w("MainActivity", "Adding profileFragment to content.");
                    ft.add(R.id.content_frame,
                            profileFragment,
                            "prof");
                } else {
                    profileFragment.refreshProfile();
                    ft.show(profileFragment);
                }
                fragToHide = profileFragment;
                break;
            case R.id.navigation_sign_out: //Sign Out
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
        mSelectedItemId = itemId;
    }

    public void onLaunchRequestPickUpDetail(String address, double lat, double lng) {
//        requestPickUpDetailFragment = RequestPickupDetailFragment.newInstance(address, lat, lng);
//        getFragmentManager().beginTransaction()
//                .add(R.id.content_frame, requestPickUpDetailFragment,
//                        "RequestPickupDetailFragment")
//                .addToBackStack("RequestPickupDetailFragment")
//                .commit();
    }

    public void updateAddress(Address address) {
//        if (requestPickUpDetailFragment != null) {
//            requestPickUpDetailFragment.setAddressFieldText(address.getAddressLine(0));
//        }
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
                .add(R.id.content_frame, pickupRequestDetailFragment,
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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.isChecked()) {
            //Closing drawer on item click
            mDrawerLayout.closeDrawers();
            return true; // Current item is already selected
        } else {
            //Check to see which item was being clicked and perform appropriate action
            selectItem(menuItem.getItemId());
            menuItem.setChecked(true);
            //Closing drawer on item click
            mDrawerLayout.closeDrawers();
            return true;
        }

    }

}
