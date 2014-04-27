package org.onewarmcoat.onewarmcoat.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.DonateFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.ProfileFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.VolunteerFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.ConfirmPickupLocationFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer.PickUpRequestsFragment;

public class MainActivity extends Activity implements PickUpRequestsFragment.OnMarkerClickListener {
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

    private Fragment mCurrentFragment;
    private DonateFragment donateFragment;
    private VolunteerFragment volunteerFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFragments();
        initializeDrawer();
        if (savedInstanceState == null) {
            //create fragments
            initializeFragments();
            mCurrentFragment = null;
            selectItem(0);
        }
//        else {
//            // restore fragments
//            donateFragment = (DonateFragment) getFragmentManager().findFragmentByTag("don");
//            volunteerFragment = (VolunteerFragment) getFragmentManager().findFragmentByTag("vol");
//            profileFragment = (ProfileFragment) getFragmentManager().findFragmentByTag("prof");
//            Log.d("MainActivity", "Fragments restored");
//        }

        //all API initialization should be done in some function
        //Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");

//        Donation row6 = new Donation(ParseUser.getCurrentUser(), "Cash", 6000);
//        row6.saveInBackground();
//
//        Donation row7 = new Donation(ParseUser.getCurrentUser(), "Coats", 45244);
//        row7.saveInBackground();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        getFragmentManager().putFragment(outState, "donateFragment", donateFragment);
//        getFragmentManager().putFragment(outState, "volunteerFragment", volunteerFragment);
//        getFragmentManager().putFragment(outState, "profileFragment", profileFragment);
        getFragmentManager().putFragment(outState, "mCurrentFragment", mCurrentFragment);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
//        donateFragment = (DonateFragment) getFragmentManager().getFragment(inState, "donateFragment");
//        volunteerFragment = (VolunteerFragment) getFragmentManager().getFragment(inState, "volunteerFragment");
//        profileFragment = (ProfileFragment) getFragmentManager().getFragment(inState, "profileFragment");
        mCurrentFragment = getFragmentManager().getFragment(inState, "mCurrentFragment");
        Log.w("MainActivity", "onRestoreInstanceState Fragments restored");
    }

    private void initializeFragments() {
//        donateFragment = DonateFragment.newInstance();
//        volunteerFragment = VolunteerFragment.newInstance();
//        profileFragment = ProfileFragment.newInstance();
    }

    private void initializeDrawer() {
        //        mNavigationDrawerFragment = (NavigationDrawerFragment)
//                getFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mTitle = getTitle();
//
//        // Set up the drawer.
//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));
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

    //    @Override
//    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getFragmentManager();
//
////        if(position == 1){
////            fragmentManager.beginTransaction()
////                    .replace(R.id.container, new DonateFragment())
////                    .commit();
////        }else {
////            fragmentManager.beginTransaction()
////                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
////                    .commit();
////        }
//    }

//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_volunteer);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_donate);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_profile);
//                break;
//            case 4:
//                mTitle = getString(R.string.title_sign_out);
//                break;
//        }
//    }

//    public void restoreActionBar() {
//        ActionBar actionBar = getActionBar();
//
////        //in donor, create tabs
////        if(mTitle == getString(R.string.title_donate)){
////
////        }else {
////            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
////        }
//
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
//            return true;
//        }
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

//            case R.id.action_contact:
            // QuickContactFragment dialog = new QuickContactFragment();
            // dialog.show(getSupportFragmentManager(), "QuickContactFragment");
            // return true;

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
//        if (profileFragment == null) {
//
//        }

        //TODO: hide fragment in container, simplify code
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

//        Fragment fragmentInContainer = getFragmentManager().findFragmentById(R.id.content);
//        if (fragmentInContainer != null) {
//            ft.hide(fragmentInContainer);
//            //after more than 1 fragment is added to the container, the wrong (maybe top most fragment)
//            // becomes the one in the container??!
//        }

        if (mCurrentFragment == null) {
            // do nothing?
        } else {
            ft.hide(mCurrentFragment);
        }

        switch (position) {
            case 0: //Donate
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
                mCurrentFragment = donateFragment;
//                if (donateFragment.isAdded()) {
//                    ft.show(donateFragment);
//                } else {
//                    Log.d("MainActivity", "Adding donateFragment to content.");
//                    ft.add(R.id.content,
//                            donateFragment,
////                            donateFragment.TAG);
//                            "don");
//                }
                break;
            case 1: //Volunteer
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
                mCurrentFragment = volunteerFragment;

//                if (volunteerFragment.isAdded()) {
//                    ft.show(volunteerFragment);
//                } else {
//                    Log.d("MainActivity", "Adding volunteerFragment to content.");
//                    ft.add(R.id.content,
//                            volunteerFragment,
////                            volunteerFragment.TAG);
//                            "vol");
//                }
                break;
            case 2: // Profile
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
                mCurrentFragment = profileFragment;

//                if (profileFragment.isAdded()) {
//                    ft.show(profileFragment);
//                } else {
//                    Log.d("MainActivity", "Adding profileFragment to content.");
//                    ft.add(R.id.content,
//                            profileFragment,
////                            profileFragment.TAG);
//                            "prof");
//                }
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
        ft.commit();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    // dTitle and dAddr come from OnMarkerClick handler
    @Override
    public void onMarkerClicked(String dTitle, String dAddr) {
        //TODO: move this to Volunteer Fragment, its rightful home
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ConfirmPickupLocationFragment confirmationPickupFrag = ConfirmPickupLocationFragment.newInstance(dTitle, dAddr);
//        if (volunteerFragment.isAdded()) {
        ft.hide(volunteerFragment);
//        }
        ft.add(R.id.content, confirmationPickupFrag);
        ft.addToBackStack("markerdetail");
        ft.commit();
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
