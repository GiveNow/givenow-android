package io.givenow.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseAnalytics;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import fj.data.Option;
import io.givenow.app.GiveNowApplication;
import io.givenow.app.R;
import io.givenow.app.fragments.main.VolunteerFragment;
import io.givenow.app.fragments.main.common.DropOffLocationsFragment;
import io.givenow.app.fragments.main.donate.RequestPickupFragment;
import io.givenow.app.fragments.main.profile.ProfileFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestDetailFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment;
import io.givenow.app.helpers.CroutonHelper;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import rx.parse.ParseObservable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends BaseActivity implements
        PickupRequestsFragment.PickupRequestDetailInteractionListener,
        PickupRequestDetailFragment.PickupRequestConfirmedListener,
        NavigationView.OnNavigationItemSelectedListener {

    ActionBarDrawerToggle mDrawerToggle;
    @Bind(R.id.navigation_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    ImageView mProfileImage;
    private int mSelectedItemId;
    private PickupRequestDetailFragment pickupRequestDetailFragment;
    private AlertDialog acceptPendingDialog;
    private Fragment fragToHide = null;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Now that we use a toolbar, the action bar progressbar doesn't exist anymore.
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        // Obtain the shared Tracker instance.
        GiveNowApplication application = (GiveNowApplication) getApplication();
        mTracker = application.getDefaultTracker();

        String projectToken = application.getMixPanelProjectToken();
        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, projectToken);
        mixpanel.identify(ParseUser.getCurrentUser().getObjectId());
        try {
            JSONObject props = new JSONObject();
            props.put("IsRegistered", ParseUserHelper.isRegistered());
            mixpanel.track("MainActivity - onCreate called", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }

        initializeDrawer();

        if (savedInstanceState != null) {
            fragToHide = getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString("fragToHideTag"));
            mSelectedItemId = savedInstanceState.getInt("mSelectedItemId");
            Log.d("MainActivity", "OnCreate state restored. mSelectedItemId=" + mSelectedItemId + " fragToHide=" + fragToHide.getTag());
            selectItem(mSelectedItemId);
        } else {
            mSelectedItemId = R.id.navigation_give;
            selectItem(mSelectedItemId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d("MainActivity", "Saving state. mSelectedItemId=" + mSelectedItemId + " fragToHideTag=" + fragToHide.getTag());
        savedInstanceState.putInt("mSelectedItemId", mSelectedItemId);
        savedInstanceState.putString("fragToHideTag", fragToHide.getTag());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

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
        String notificationData = getIntent().getStringExtra("com.parse.Data");
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
                    getIntent().removeExtra("com.parse.Data");

                    //try setting us to the Volunteer fragment
//                    selectItem(POSITION_VOLUNTEER);
                    //move to dashboard duey
//                    VolunteerFragment volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
                    //0 means dashboard
//                    volunteerFragment.setCurrentItem(0);

                    return true;
                } else if (notifType.equals(PickupRequest.PICKUP_COMPLETE)) {
                    //remove the push notif data, so we don't process it next app resume
                    getIntent().removeExtra("com.parse.Data");

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getSupportActionBar()
        getMenuInflater().inflate(R.menu.main, menu);
        return true; // super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                displayInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayInfo() {
        if (mSelectedItemId == R.id.navigation_give) {
            RequestPickupFragment requestPickupFragment = (RequestPickupFragment) getSupportFragmentManager().findFragmentByTag("don");
            requestPickupFragment.displayInfo();
        }
    }

    private void checkForPendingRequests() {
        ParseQuery<PickupRequest> query = PickupRequest.queryMyPendingRequests();

        //only want to get 1 at a time (there shouldn't be more than 1 anyway)
        query.getFirstInBackground((pickupRequest, e) -> {
            if (pickupRequest != null) {
//                    Toast.makeText(getBaseContext(), "found pickup confirmation = " + pickupRequest.getNumberOfCoats() + pickupRequest.getDonationCategories(), Toast.LENGTH_LONG).show();
                Log.d("MainActivity", "Pending Volunteer waiting for response, creating accept volunteer dialog.");
                //show dialog to user
                createAcceptPendingVolunteerDialog(pickupRequest);
            }
        });
    }

    private void createAcceptPendingVolunteerDialog(final PickupRequest pickupRequest) {
        pickupRequest.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).observeOn(mainThread()).subscribe(
                        volunteer -> {
                            Option<String> nameOption = ParseUserHelper.getName(volunteer);
                            String name = getString(R.string.push_notif_volunteer_default_name);
                            if (nameOption.isSome()) {
                                name = ParseUserHelper.getFirstName(volunteer).orSome(name);
                            }
                            String title = name + getString(R.string.push_notif_volunteer_is_ready_to_pickup);
                            String address = "<br><br><font color='#858585'>Address: " + pickupRequest.getAddress() + "</font>";


                            if (acceptPendingDialog != null && acceptPendingDialog.isShowing()) {
                                acceptPendingDialog.dismiss();
                            }
                            acceptPendingDialog = new AlertDialog.Builder(this)
//                .setTitle(R.string.acceptRequest_submittedDialog_title)
//                .setMessage(R.string.acceptRequest_submittedDialog_msg)
                                    .setTitle(Html.fromHtml(title)) //TODO: include in message?: + pickupRequest.getDonationCategories().toString() +
                                    .setMessage(Html.fromHtml(getString(R.string.dialog_accept_pending_volunteer) + address))
                                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> pendingVolunteerConfirmed(pickupRequest))
                                    .setNegativeButton(R.string.no, (dialog, which) -> cancelPendingVolunteer(pickupRequest))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .show();
                        },
                        error -> ErrorDialogs.connectionFailure(this, error)));
    }

    private void cancelPendingVolunteer(PickupRequest pickupRequest) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("RequestPickup")
                .setAction("PendingVolunteerCanceled")
                .setLabel(ParseUser.getCurrentUser().getObjectId())
                .build());

        //donor doesn't accept volunteer request, so remove the pending volunteer
        pickupRequest.remove("pendingVolunteer");
        pickupRequest.saveInBackground();
    }

    private void pendingVolunteerConfirmed(final PickupRequest pickupRequest) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("RequestPickup")
                .setAction("PendingVolunteerConfirmed")
                .setLabel(ParseUser.getCurrentUser().getObjectId())
                .build());
        // if user accepts, send push notif to pendingVolunteer, and set confirmedVolunteer
        pickupRequest.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).subscribe(
                        volunteer -> {
                            pickupRequest.generateVolunteerConfirmedNotif(this);
                            pickupRequest.setConfirmedVolunteer(volunteer);

                            //removed this, and added it to done method below
                            pickupRequest.saveInBackground();
                        },
                        error -> ErrorDialogs.connectionFailure(this, error)));
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

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(this);

        // Initializing Drawer Layout and ActionBarToggle
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                updateProfileHeader(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(this::onProfileImageClick);
        mProfileImage = (ImageView) headerView.findViewById(R.id.navigation_profile_image);
        mProfileImage.setOnClickListener(this::onProfileImageClick);

        updateProfileHeader(headerView);
    }

    private void updateProfileHeader(View drawerView) {
        TextView labelUsername = (TextView) drawerView.findViewById(R.id.navigation_label_username);
        TextView labelPhone = (TextView) drawerView.findViewById(R.id.navigation_label_phone);

        labelUsername.setText(Option.fromNull(ParseUser.getCurrentUser().get("name")).orSome(getString(R.string.navigation_your_profile)).toString());
        labelPhone.setText(ParseUserHelper.getPhoneNumber());
        ParseUserHelper.getProfileImage().foreachDoEffect(parseFile ->
                Picasso.with(getApplicationContext()).load(parseFile.getUrl()).into(mProfileImage));
    }

    public void onProfileImageClick(View v) {
        //Deselect current item and close drawer
        if (mSelectedItemId != R.id.navigation_profile_image) {
            navigationView.getMenu().findItem(mSelectedItemId).setChecked(false);
        }
        navigationView.getHeaderView(0).findViewById(R.id.navigation_header)
                .setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mDrawerLayout.closeDrawers();
        //Select Profile page
        selectItem(R.id.navigation_profile_image);
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
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out); //not supported by SupportFM
//        ft.setTransition();
//        Fragment fragToHide = getFragmentManager().findFragmentById(R.id.content_frame);
        if (fragToHide != null) {
            ft.hide(fragToHide);
        }

        RequestPickupFragment requestPickupFragment = (RequestPickupFragment) getSupportFragmentManager().findFragmentByTag("don");
        VolunteerFragment volunteerFragment = (VolunteerFragment) getSupportFragmentManager().findFragmentByTag("vol");
        ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("prof");
        DropOffLocationsFragment dropoffFragment = (DropOffLocationsFragment) getSupportFragmentManager().findFragmentByTag("drop");
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
                    volunteerFragment.checkVolunteerEligibility(); //TODO minor optimization issue: this causes the check to be run twice.
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
                //take user to onboarding screen and set the pref so it shows up again if the app is relaunched
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("RanBefore", false);
                editor.apply();
                startActivity(new Intent(this, SplashActivity.class));
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
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, pickupRequestDetailFragment,
                        "PickupRequestDetailFragment")
                .addToBackStack("PickupRequestDetailFragment")
                .commit();
    }

    @Override
    public void onPickupConfirmed(PickupRequest pickupRequest) {
        VolunteerFragment volunteerFragment = (VolunteerFragment) getSupportFragmentManager().findFragmentByTag("vol");
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
            //Reset header to normal color (because a navigation item was clicked, not the profile)
            navigationView.getHeaderView(0).findViewById(R.id.navigation_header)
                    .setBackgroundColor(getResources().getColor(R.color.drawerHeaderBGColor));
            //Closing drawer on item click
            mDrawerLayout.closeDrawers();
            return true;
        }

    }

    /* If a given EditText is in focus, and something else is touched, clear focus from the given EditText. */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
