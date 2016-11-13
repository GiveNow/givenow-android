package io.givenow.app.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.parse.ParseAnalytics
import com.parse.ParseUser
import com.squareup.picasso.Picasso
import fj.F
import fj.data.Option
import io.givenow.app.GiveNowApplication
import io.givenow.app.GiveNowPushReceiver
import io.givenow.app.R
import io.givenow.app.fragments.main.VolunteerFragment
import io.givenow.app.fragments.main.common.DropOffLocationsFragment
import io.givenow.app.fragments.main.donate.RequestPickupFragment
import io.givenow.app.fragments.main.profile.ProfileFragment
import io.givenow.app.fragments.main.volunteer.PickupRequestDetailFragment
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment
import io.givenow.app.helpers.Analytics
import io.givenow.app.helpers.ErrorDialogs
import io.givenow.app.models.ParseUserHelper
import io.givenow.app.models.PickupRequest
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers.mainThread
import rx.parse.ParseObservable

class MainActivity : BaseActivity(),
        PickupRequestsFragment.PickupRequestDetailInteractionListener,
        PickupRequestDetailFragment.PickupRequestConfirmedListener,
        NavigationView.OnNavigationItemSelectedListener {

    override val layoutResource: Int = R.layout.activity_main

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    @BindView(R.id.navigation_view)
    lateinit var navigationView: NavigationView

    @BindView(R.id.drawer_layout)
    lateinit var mDrawerLayout: DrawerLayout

    private lateinit var mProfileImage: ImageView
    private var mSelectedItemId: Int = 0
    private var pickupRequestDetailFragment: PickupRequestDetailFragment? = null
    private var acceptPendingDialog: AlertDialog? = null
    private var fragToHide: Fragment? = null
    private lateinit var mTracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Now that we use a toolbar, the action bar progressbar doesn't exist anymore.
        //        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        ParseAnalytics.trackAppOpenedInBackground(intent)

        // Obtain the shared Tracker instance.
        val application = application as GiveNowApplication
        mTracker = application.defaultTracker

        val mixpanel = MixpanelAPI.getInstance(this, application.mixPanelProjectToken)
        mixpanel.identify(ParseUser.getCurrentUser().objectId)
        Analytics.mixpanelTrackIsRegisteredUser(mixpanel, "MainActivity - onCreate called")

        initializeDrawer()

        if (savedInstanceState != null) {
            fragToHide = supportFragmentManager.findFragmentByTag(savedInstanceState.getString("fragToHideTag"))
            mSelectedItemId = savedInstanceState.getInt("mSelectedItemId")
            Log.d("MainActivity", "OnCreate state restored. mSelectedItemId=" + mSelectedItemId + " fragToHide=" + fragToHide!!.tag)
            selectItem(mSelectedItemId)
        } else {
            selectItem(chooseItemFromPushNotif())
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.d("MainActivity", "Saving state. mSelectedItemId=" + mSelectedItemId + " fragToHideTag=" + fragToHide!!.tag)
        savedInstanceState.putInt("mSelectedItemId", mSelectedItemId)
        savedInstanceState.putString("fragToHideTag", fragToHide!!.tag)
    }


    override fun onResume() {
        super.onResume()
        mTracker.setScreenName("MainActivity")
        mTracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    @IdRes
    private fun chooseItemFromPushNotif(): Int {
        return GiveNowPushReceiver.getPushData(intent).map<Int>(F { pushData: JSONObject ->
            when (pushData.optString("type")) {
                "claimPickupRequest" -> R.id.navigation_give
                "confirmVolunteer" -> R.id.navigation_volunteer // donation ready for pickup. show volunteer dashboard
                "pickupDonation" -> R.id.navigation_give
                else -> R.id.navigation_give
            }
        }).orSome(R.id.navigation_give)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                displayInfo()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun displayInfo() {
        if (mSelectedItemId == R.id.navigation_give) {
            (supportFragmentManager.findFragmentByTag("don") as RequestPickupFragment)?.displayInfo()
        }
    }

    private fun checkForPendingRequests() {
        //only want to get 1 at a time (there shouldn't be more than 1 anyway)
        if (ParseUserHelper.isRegistered) {
            ParseObservable.first(PickupRequest.queryMyPendingRequests()).observeOn(mainThread()).subscribe(
                    { this.createAcceptPendingVolunteerDialog(it) },
                    { it.printStackTrace() })
        }
    }

    private fun createAcceptPendingVolunteerDialog(pickupRequest: PickupRequest) {
        Log.d("MainActivity", "Pending Volunteer waiting for response, creating accept volunteer dialog.")

        pickupRequest.pendingVolunteer.foreachDoEffect { pendingVolunteer ->
            ParseObservable.fetchIfNeeded(pendingVolunteer).observeOn(mainThread()).subscribe(
                    { volunteer ->
                        val nameOption = ParseUserHelper.getName(volunteer)
                        var name = getString(R.string.default_volunteer_name)
                        if (nameOption.isSome) {
                            name = ParseUserHelper.getFirstName(volunteer).orSome(name)
                        }
                        val title = name + getString(R.string.push_notif_volunteer_is_ready_to_pickup)
                        val address = "<br><br><font color='#858585'>" + pickupRequest.address + "</font>"

                        if (acceptPendingDialog != null && acceptPendingDialog!!.isShowing) {
                            acceptPendingDialog!!.dismiss()
                        }
                        acceptPendingDialog = AlertDialog.Builder(this).setTitle(Html.fromHtml(title)) //TODO: include in message?: + pickupRequest.getDonationCategories().toString() +
                                .setMessage(Html.fromHtml(getString(R.string.dialog_accept_pending_volunteer) + address)).setPositiveButton(getString(R.string.yes)) { dialog, which -> pendingVolunteerConfirmed(pickupRequest) }.setNegativeButton(R.string.no) { dialog, which -> cancelPendingVolunteer(pickupRequest) }.setIcon(R.mipmap.ic_launcher).show()
                    },
                    { error -> ErrorDialogs.connectionFailure(this, error) })
        }
    }

    private fun pendingVolunteerConfirmed(pickupRequest: PickupRequest) {
        Analytics.sendHit(mTracker, "RequestPickup", "PendingVolunteerConfirmed", ParseUser.getCurrentUser().objectId)

        //TODO: maybe implement Retry dialog here?
        pickupRequest.confirmVolunteer().subscribe(
                { response -> Log.d("Cloud Response", response.toString()) },
                { error -> ErrorDialogs.connectionFailure(this, error) })
    }

    private fun cancelPendingVolunteer(pickupRequest: PickupRequest) {
        //donor doesn't accept volunteer request, so remove the pending volunteer
        Analytics.sendHit(mTracker, "RequestPickup", "PendingVolunteerCanceled", ParseUser.getCurrentUser().objectId)
        pickupRequest.cancelPendingVolunteer()
    }

    private fun initializeDrawer() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //Initializing NavigationView

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(this)

        // Initializing Drawer Layout and ActionBarToggle
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                updateProfileHeader(drawerView)
            }
        }

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.setDrawerListener(mDrawerToggle)

        //calling sync state is necessay or else your hamburger icon wont show up
        mDrawerToggle.syncState()

        val headerView = navigationView.getHeaderView(0)
        headerView.setOnClickListener { this.onProfileImageClick(it) }
        mProfileImage = headerView.findViewById(R.id.navigation_profile_image) as ImageView
        mProfileImage.setOnClickListener { this.onProfileImageClick(it) }

        updateProfileHeader(headerView)
    }

    private fun updateProfileHeader(drawerView: View) {
        val labelUsername = drawerView.findViewById(R.id.navigation_label_username) as TextView
        val labelPhone = drawerView.findViewById(R.id.navigation_label_phone) as TextView

        labelUsername.text = Option.fromNull(ParseUser.getCurrentUser().get("name")).orSome(getString(R.string.navigation_your_profile)).toString()
        labelPhone.text = ParseUserHelper.phoneNumber
        ParseUserHelper.profileImage.foreachDoEffect { parseFile -> Picasso.with(applicationContext).load(parseFile.url).into(mProfileImage) }
    }

    fun onProfileImageClick(v: View) {
        //Select Profile page
        selectItem(R.id.navigation_profile_image)
    }

    private fun showProfileHeaderAsSelected() {
        //Deselect current item and close drawer
        if (mSelectedItemId != R.id.navigation_profile_image) {
            navigationView.menu.findItem(mSelectedItemId).isChecked = false
        }
        navigationView.getHeaderView(0).findViewById(R.id.navigation_header).setBackgroundColor(resources.getColor(R.color.colorAccent))
        mDrawerLayout.closeDrawers()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.isChecked) {
            //Closing drawer on item click
            mDrawerLayout.closeDrawers()
            return true // Current item is already selected
        } else {
            menuItem.isChecked = true
            //Reset header to normal color (because a navigation item was clicked, not the profile)
            navigationView.getHeaderView(0).findViewById(R.id.navigation_header).setBackgroundColor(resources.getColor(R.color.drawerHeaderBGColor))

            //Select item corresponding to the menu choice
            selectItem(menuItem.itemId)

            //Closing drawer on item click
            mDrawerLayout.closeDrawers()
            return true
        }
    }

    fun selectMenuItem(@IdRes itemId: Int) {
        onNavigationItemSelected(navigationView.menu.findItem(itemId))
    }

    private fun selectItem(@IdRes itemId: Int) {
        val ft = supportFragmentManager.beginTransaction()
        Log.w("MainActivity", "Selecting item Id " + itemId.toString())

        if (fragToHide != null) {
            Log.w("MainActivity", "FragToHide is not null. Will hide frag " + fragToHide!!.tag)
            ft.hide(fragToHide)
        }

        var requestPickupFragment: RequestPickupFragment? = supportFragmentManager.findFragmentByTag("don") as RequestPickupFragment?
        var volunteerFragment: VolunteerFragment? = supportFragmentManager.findFragmentByTag("vol") as VolunteerFragment?
        var profileFragment: ProfileFragment? = supportFragmentManager.findFragmentByTag("prof") as ProfileFragment?
        var dropoffFragment: DropOffLocationsFragment? = supportFragmentManager.findFragmentByTag("drop") as DropOffLocationsFragment?
        when (itemId) {
            R.id.navigation_give //Donate
            -> {
                if (requestPickupFragment == null) {
                    requestPickupFragment = RequestPickupFragment.newInstance()
                    Log.w("MainActivity", "Adding requestPickupFragment to content.")
                    ft.add(R.id.content_frame,
                            requestPickupFragment,
                            "don")
                } else {
                    Log.w("MainActivity", "Will show frag" + requestPickupFragment.tag)
                    ft.show(requestPickupFragment)
                }
                fragToHide = requestPickupFragment
                checkForPendingRequests()
            }
            R.id.navigation_volunteer //Volunteer
            -> {
                if (volunteerFragment == null) {
                    volunteerFragment = VolunteerFragment.newInstance()
                    Log.w("MainActivity", "Adding volunteerFragment to content.")
                    ft.add(R.id.content_frame,
                            volunteerFragment,
                            "vol")
                } else {
                    Log.w("MainActivity", "Will show frag" + volunteerFragment.tag)
                    volunteerFragment.checkVolunteerEligibility() //TODO minor optimization issue: this causes the check to be run twice.
                    ft.show(volunteerFragment)
                }
                fragToHide = volunteerFragment
            }
            R.id.navigation_dropoff //Dropoff Centers
            -> {
                if (dropoffFragment == null) {
                    dropoffFragment = DropOffLocationsFragment.newInstance()
                    Log.w("MainActivity", "Adding dropoffFragment to content.")
                    ft.add(R.id.content_frame,
                            dropoffFragment,
                            "drop")
                } else {
                    Log.w("MainActivity", "Will show frag" + dropoffFragment.tag)
                    ft.show(dropoffFragment)
                }
                fragToHide = dropoffFragment
            }
            R.id.navigation_profile_image // Profile
            -> {
                if (profileFragment == null) {
                    profileFragment = ProfileFragment.newInstance()
                    Log.w("MainActivity", "Adding profileFragment to content.")
                    ft.add(R.id.content_frame,
                            profileFragment,
                            "prof")
                } else {
                    Log.w("MainActivity", "Will show frag" + profileFragment.tag)
                    profileFragment.refreshProfile()
                    ft.show(profileFragment)
                }
                fragToHide = profileFragment
                showProfileHeaderAsSelected()
            }
            R.id.navigation_sign_out //Sign Out
            -> signOut()
            else -> Log.d("MainActivity", "default case hit in selectItem, weird position number!")
        }
        //        ft.addToBackStack(String.valueOf(position));
        ft.commit()
        mSelectedItemId = itemId
    }

    private fun signOut() {
        ParseUser.logOut()
        //take user to onboarding screen and set the pref so it shows up again if the app is relaunched
        val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val editor = preferences.edit()
        editor.putBoolean("RanBefore", false)
        editor.apply()
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onLaunchPickupRequestDetail(pickupRequest: PickupRequest) {
        if (pickupRequestDetailFragment != null) {
            if (pickupRequestDetailFragment!!.isAdded) {
                //already displaying a detail fragment, close that one
                pickupRequestDetailFragment!!.animateAndDetach()
            }
        }
        pickupRequestDetailFragment = PickupRequestDetailFragment.newInstance(pickupRequest)
        supportFragmentManager.beginTransaction()
                .add(R.id.content_frame, pickupRequestDetailFragment, "PickupRequestDetailFragment")
                .addToBackStack("PickupRequestDetailFragment")
                .commit()
    }

    override fun onPickupConfirmed(pickupRequest: PickupRequest) {
        // removing individual marker for this pickup request
        // won't work if the orientation changed during the PickupConfirmDialog, because the pickupRequest we get back from the dialog
        // isn't the same one that's now in the since-recreated volunteerFragment.
        (supportFragmentManager.findFragmentByTag("vol") as VolunteerFragment)?.removePickupRequestFromMap(pickupRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        //check for any request codes, then call super, so fragments can catch the result
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    /* If a given EditText is in focus, and something else is touched, clear focus from the given EditText. */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
