package io.givenow.app.fragments.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.parse.ParseUser
import io.givenow.app.R
import io.givenow.app.fragments.PageSlidingTabStripFragment
import io.givenow.app.fragments.PhoneNumberVerificationFragment
import io.givenow.app.fragments.SuperAwesomeCardFragment
import io.givenow.app.fragments.main.volunteer.DashboardFragment
import io.givenow.app.fragments.main.volunteer.PickupRequestsFragment
import io.givenow.app.models.ParseUserHelper
import io.givenow.app.models.PickupRequest
import io.givenow.app.models.Volunteer
import org.jetbrains.anko.support.v4.withArguments
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable

class VolunteerFragment : PageSlidingTabStripFragment(), PhoneNumberVerificationFragment.OnUserLoginCompleteListener {

    @BindView(R.id.button)
    lateinit var button: Button
    @BindView(R.id.description)
    lateinit var tvDescription: TextView
    @BindView(R.id.overlay)
    lateinit var llOverlay: LinearLayout

    private var pickupRequestsFragment: PickupRequestsFragment? = null
    private var dashboardFragment: DashboardFragment? = null

    override fun getTitles(): Array<String> {
        return arrayOf("My Dashboard", "PickUp Requests")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            //create fragments
            pickupRequestsFragment = PickupRequestsFragment.newInstance()
            dashboardFragment = DashboardFragment.newInstance()
            Log.w("VolunteerFragment", "onCreate: Fragments created")
        }
    }

    private lateinit var unbinder: Unbinder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //TODO maybe dont even initialize the tabs if volunteer not approved
        Log.w("VolunteerFragment", "onViewCreated")
        unbinder = ButterKnife.bind(this, view)

        checkVolunteerEligibility()
        Log.d("VolunteerFragment", "OnViewCreated complete.")
    }

    fun checkVolunteerEligibility() {
        Log.d("VolunteerFragment", "Checking volunteer eligibility...")
        Volunteer.findUser(ParseUser.getCurrentUser()).subscribe(
                { this.uiVolunteerApplied(it) },
                { error ->
                    //Never applied to be a volunteer
                    Log.d("VolunteerFragment", "Never applied to be a volunteer")
                    llOverlay.visibility = View.VISIBLE
                    tvDescription.setText(R.string.volunteer_label_user_has_not_applied)
                    button.setText(R.string.volunteer_button_user_has_not_applied)
                })
    }

    @OnClick(R.id.button)
    fun onClick(btn: Button) {
        if (ParseUserHelper.isRegistered) {
            applyToVolunteer()
        } else {
            //show phone number dialog
            PhoneNumberVerificationFragment()
                    .withArguments("messageResource" to R.string.dialog_phoneNumber_for_volunteer)
                    .show(childFragmentManager, "phdialog")
        }
    }

    override fun onUserLoginComplete() {
        applyToVolunteer()
    }

    private fun applyToVolunteer() {
        Volunteer.findUser(ParseUser.getCurrentUser()).subscribe(
                { this.uiVolunteerApplied(it) },
                { error ->
                    // User logged in, and they never applied to be a volunteer before.
                    // Add them to volunteer table.
                    Log.d("VolunteerFragment", "User logged in, and they never applied to be a volunteer before. Adding them to volunteer table.")
                    val volunteer = Volunteer(ParseUser.getCurrentUser(), false)
                    ParseObservable.save(volunteer)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { vol -> uiAwaitingApproval() }
                })

    }

    private fun uiVolunteerApplied(volunteer: Volunteer) {
        if (volunteer.isApproved) {
            //Approved volunteer
            Log.d("VolunteerFragment", "Approved volunteer")
            //Show volunteer fragment
            llOverlay.visibility = View.GONE
        } else {
            //Awaiting approval
            uiAwaitingApproval()
        }
    }

    private fun uiAwaitingApproval() {
        Log.d("VolunteerFragment", "Awaiting approval")
        llOverlay.visibility = View.VISIBLE
        tvDescription.text = getString(R.string.volunteer_label_user_has_applied, ParseUserHelper.phoneNumber)
        button.setText(R.string.volunteer_button_user_has_applied)
        button.isEnabled = false
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        childFragmentManager.putFragment(outState, "pickupRequestsFragment", pickupRequestsFragment)
        childFragmentManager.putFragment(outState, "dashboardFragment", dashboardFragment)
        Log.w("VolunteerFragment", "onSaveInstanceState: Fragments saved")
    }

    override fun onActivityCreated(inState: Bundle?) {
        super.onActivityCreated(inState)
        Log.w("VolunteerFragment", "onActivityCreated called.")
        if (inState != null) {
            pickupRequestsFragment = childFragmentManager.getFragment(inState, "pickupRequestsFragment") as PickupRequestsFragment
            dashboardFragment = childFragmentManager.getFragment(inState, "dashboardFragment") as DashboardFragment
            Log.w("VolunteerFragment", "onActivityCreated: Fragments restored")
        }
    }

    override fun getFragmentForPosition(position: Int): Fragment {
        val frag: Fragment
        when (position) {
            0 -> frag = dashboardFragment!! //Dashboard
            1 -> frag = pickupRequestsFragment!! //PickUp Requests
            else -> {
                Log.w("VolunteerFragment", "default case hit in getFragmentForPosition, weird tab/position number!")
                frag = SuperAwesomeCardFragment.newInstance(position)
            }
        }
        return frag
    }

    fun loadMarkers() {
        //TODO: probably just move this to PickupRequestFragment onStart / onResume
        pickupRequestsFragment?.loadMarkers()
    }

    fun removePickupRequestFromMap(pickupRequest: PickupRequest) {
        pickupRequestsFragment?.removePickupRequestFromMap(pickupRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
    }

    companion object {

        fun newInstance(): VolunteerFragment {
            return VolunteerFragment()
        }
    }
}
