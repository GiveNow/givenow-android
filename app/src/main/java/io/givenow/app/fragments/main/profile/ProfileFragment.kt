package io.givenow.app.fragments.main.profile

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.parse.ParseUser
import io.givenow.app.R
import io.givenow.app.adapters.ViewPagerAdapter
import io.givenow.app.helpers.ErrorDialogs
import io.givenow.app.models.ParseUserHelper
import rx.android.schedulers.AndroidSchedulers.mainThread
import rx.parse.ParseObservable


class ProfileFragment : Fragment() {

    @BindView(R.id.silhouette)
    lateinit var profileIV: ImageView
    @BindView(R.id.username)
    lateinit var usernameTV: TextView
    @BindView(R.id.phoneno)
    lateinit var phonenoTV: TextView
    @BindView(R.id.viewpager)
    lateinit var mViewPager: ViewPager
    @BindView(R.id.tablayout)
    lateinit var tabLayout: TabLayout

    private lateinit var mAdapter: ViewPagerAdapter

    private lateinit var dhFragment: DonationHistoryFragment
    private lateinit var phFragment: PickupHistoryFragment
    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_profile, container, false)

        unbinder = ButterKnife.bind(this, rootView!!)

        phonenoTV.text = ParseUserHelper.phoneNumber
        usernameTV.text = ParseUserHelper.name.orSome("")

        usernameTV.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> saveName()
            }
            false
        }

        usernameTV.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                saveName()
            }
        }

        setupViewPager()
        tabLayout.setupWithViewPager(mViewPager)

        return rootView
    }

    private fun setupViewPager() {
        mAdapter = ViewPagerAdapter(childFragmentManager)
        dhFragment = DonationHistoryFragment.newInstance()
        phFragment = PickupHistoryFragment.newInstance()
        mAdapter.addFrag(dhFragment, getString(R.string.tab_title_donation_history))
        mAdapter.addFrag(phFragment, getString(R.string.tab_title_volunteer_history))
        mViewPager.adapter = mAdapter
    }

    private fun saveName() {
        ParseUserHelper.setName(usernameTV.text.toString())
        ParseObservable.save(ParseUser.getCurrentUser())
                .observeOn(mainThread())
                .subscribe(
                        { parseUser -> Log.d("ProfileFragment", "Name saved") },
                        { error -> ErrorDialogs.connectionFailure(context, error) })
    }

    fun refreshProfile() {
        //        dhFragment.refreshList(); //TODO crahses
        //        phFragment.refreshList();
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    companion object {

        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}
