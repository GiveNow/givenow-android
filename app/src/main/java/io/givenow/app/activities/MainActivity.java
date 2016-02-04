package io.givenow.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
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
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fj.data.Option;
import io.givenow.app.GiveNowApplication;
import io.givenow.app.GiveNowPushReceiver;
import io.givenow.app.R;
import io.givenow.app.fragments.main.VolunteerFragment;
import io.givenow.app.fragments.main.common.DropOffLocationsFragment;
import io.givenow.app.fragments.main.donate.RequestPickupFragment;
import io.givenow.app.fragments.main.profile.ProfileFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestDetailFragment;
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment;
import io.givenow.app.helpers.Analytics;
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

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, application.getMixPanelProjectToken());
        mixpanel.identify(ParseUser.getCurrentUser().getObjectId());
        Analytics.mixpanelTrackIsRegisteredUser(mixpanel, "MainActivity - onCreate called");

        initializeDrawer();

        if (savedInstanceState != null) {
            fragToHide = getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString("fragToHideTag"));
            mSelectedItemId = savedInstanceState.getInt("mSelectedItemId");
            Log.d("MainActivity", "OnCreate state restored. mSelectedItemId=" + mSelectedItemId + " fragToHide=" + fragToHide.getTag());
            selectItem(mSelectedItemId);
        } else {
            selectItem(chooseItemFromPushNotif());
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
    }

    @IdRes
    private int chooseItemFromPushNotif() {
        return GiveNowPushReceiver.getPushData(getIntent()).map(pushData -> {
            switch (pushData.optString("type")) {
                case "claimPickupRequest":
                    return R.id.navigation_give;
                case "confirmVolunteer":
                    return R.id.navigation_volunteer; // donation ready for pickup. show volunteer dashboard
                case "pickupDonation":
                    return R.id.navigation_give;
                default:
                    return R.id.navigation_give;
            }
        }).orSome(R.id.navigation_give);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        //only want to get 1 at a time (there shouldn't be more than 1 anyway)
        ParseObservable.first(PickupRequest.queryMyPendingRequests()).observeOn(mainThread()).subscribe(
                this::createAcceptPendingVolunteerDialog,
                Throwable::printStackTrace
        );
    }

    private void createAcceptPendingVolunteerDialog(final PickupRequest pickupRequest) {
        Log.d("MainActivity", "Pending Volunteer waiting for response, creating accept volunteer dialog.");

        pickupRequest.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).observeOn(mainThread()).subscribe(
                        volunteer -> {
                            Option<String> nameOption = ParseUserHelper.getName(volunteer);
                            String name = getString(R.string.push_notif_volunteer_default_name);
                            if (nameOption.isSome()) {
                                name = ParseUserHelper.getFirstName(volunteer).orSome(name);
                            }
                            String title = name + getString(R.string.push_notif_volunteer_is_ready_to_pickup);
                            String address = "<br><br><font color='#858585'>" + pickupRequest.getAddress() + "</font>";


                            if (acceptPendingDialog != null && acceptPendingDialog.isShowing()) {
                                acceptPendingDialog.dismiss();
                            }
                            acceptPendingDialog = new AlertDialog.Builder(this)
                                    .setTitle(Html.fromHtml(title)) //TODO: include in message?: + pickupRequest.getDonationCategories().toString() +
                                    .setMessage(Html.fromHtml(getString(R.string.dialog_accept_pending_volunteer) + address))
                                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> pendingVolunteerConfirmed(pickupRequest))
                                    .setNegativeButton(R.string.no, (dialog, which) -> cancelPendingVolunteer(pickupRequest))
                                    .setIcon(R.mipmap.ic_launcher)
                                    .show();
                        },
                        error -> ErrorDialogs.connectionFailure(this, error)));
    }

    private void pendingVolunteerConfirmed(final PickupRequest pickupRequest) {
        Analytics.sendHit(mTracker, "RequestPickup", "PendingVolunteerConfirmed", ParseUser.getCurrentUser().getObjectId());

        pickupRequest.confirmVolunteer().subscribe(
                response -> Log.d("Cloud Response", response.toString()),
                error -> ErrorDialogs.connectionFailure(this, error) //TODO: maybe implement Retry dialog here?
        );
    }

    private void cancelPendingVolunteer(PickupRequest pickupRequest) {
        //donor doesn't accept volunteer request, so remove the pending volunteer
        Analytics.sendHit(mTracker, "RequestPickup", "PendingVolunteerCanceled", ParseUser.getCurrentUser().getObjectId());
        pickupRequest.cancelPendingVolunteer();
    }

    private void initializeDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(this);

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerToggle = new ActionBarDrawerToggle(this,
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
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        mDrawerToggle.syncState();

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
        //Select Profile page
        selectItem(R.id.navigation_profile_image);
    }

    private void showProfileHeaderAsSelected() {
        //Deselect current item and close drawer
        if (mSelectedItemId != R.id.navigation_profile_image) {
            navigationView.getMenu().findItem(mSelectedItemId).setChecked(false);
        }
        navigationView.getHeaderView(0).findViewById(R.id.navigation_header)
                .setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.isChecked()) {
            //Closing drawer on item click
            mDrawerLayout.closeDrawers();
            return true; // Current item is already selected
        } else {
            menuItem.setChecked(true);
            //Reset header to normal color (because a navigation item was clicked, not the profile)
            navigationView.getHeaderView(0).findViewById(R.id.navigation_header)
                    .setBackgroundColor(getResources().getColor(R.color.drawerHeaderBGColor));

            //Select item corresponding to the menu choice
            selectItem(menuItem.getItemId());

            //Closing drawer on item click
            mDrawerLayout.closeDrawers();
            return true;
        }
    }

    public void selectMenuItem(@IdRes int itemId) {
        onNavigationItemSelected(navigationView.getMenu().findItem(itemId));
    }

    private void selectItem(@IdRes int itemId) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Log.w("MainActivity", "Selecting item Id " + String.valueOf(itemId));

        if (fragToHide != null) {
            Log.w("MainActivity", "FragToHide is not null. Will hide frag " + fragToHide.getTag());
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
                    Log.w("MainActivity", "Will show frag" + requestPickupFragment.getTag());
                    ft.show(requestPickupFragment);
                }
                fragToHide = requestPickupFragment;
                checkForPendingRequests();
                break;
            case R.id.navigation_volunteer: //Volunteer
                if (volunteerFragment == null) {
                    volunteerFragment = VolunteerFragment.newInstance();
                    Log.w("MainActivity", "Adding volunteerFragment to content.");
                    ft.add(R.id.content_frame,
                            volunteerFragment,
                            "vol");
                } else {
                    Log.w("MainActivity", "Will show frag" + volunteerFragment.getTag());
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
                    Log.w("MainActivity", "Will show frag" + dropoffFragment.getTag());
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
                    Log.w("MainActivity", "Will show frag" + profileFragment.getTag());
                    profileFragment.refreshProfile();
                    ft.show(profileFragment);
                }
                fragToHide = profileFragment;
                showProfileHeaderAsSelected();
                break;
            case R.id.navigation_sign_out: //Sign Out
                signOut();
                break;
            default:
                Log.d("MainActivity", "default case hit in selectItem, weird position number!");
                break;
        }
//        ft.addToBackStack(String.valueOf(position));
        ft.commit();
        mSelectedItemId = itemId;
    }

    private void signOut() {
        ParseUser.logOut();
        //take user to onboarding screen and set the pref so it shows up again if the app is relaunched
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("RanBefore", false);
        editor.apply();
        startActivity(new Intent(this, SplashActivity.class));
        finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for any request codes, then call super, so fragments can catch the result
        super.onActivityResult(requestCode, resultCode, data);
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
