package org.onewarmcoat.onewarmcoat.app;

import android.app.Activity;
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

import com.parse.Parse;

import org.onewarmcoat.onewarmcoat.app.fragments.main.DonateFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.VolunteerFragment;

public class MainActivity extends Activity {
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
    private DonateFragment donateFragment;
    private VolunteerFragment volunteerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //all API initialization should be done in some function
        Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");

        //TODO: create a setupViews function to wrap all of this
        donateFragment = DonateFragment.newInstance();
        volunteerFragment = VolunteerFragment.newInstance();

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

        selectItem(0);
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

        switch (position) {
            case 0: //Donate
                //TODO: donateFragment seems to get instantiated a second time when I switch to this position. Why?
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content,
                                donateFragment,
                                donateFragment.TAG).commit();
                break;
            case 1: //Volunteer
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content,
                                volunteerFragment,
                                volunteerFragment.TAG).commit();
                break;
            default:

//                Fragment fragment = new PlanetFragment();
//                Bundle args = new Bundle();
//                args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//                fragment.setArguments(args);
//
//                getFragmentManager().beginTransaction()
//                        .add(R.id.content, fragment).commit();
                Log.d("MainActivity", "default case hit in selectItem, weird position number!");
                break;
        }

        mDrawerLayout.closeDrawer(mDrawerList);
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

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((MainActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

}
