package io.givenow.app.fragments.main.profile


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import io.givenow.app.R
import io.givenow.app.activities.MainActivity
import io.givenow.app.adapters.DonationHistoryAdapter
import io.givenow.app.helpers.CustomAnimations
import io.givenow.app.interfaces.AnythingChangedDataObserver
import io.givenow.app.models.Donation
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable


class DonationHistoryFragment : Fragment() {

    @BindView(R.id.rvDonations)
    lateinit var rvDonations: RecyclerView

    @BindView(R.id.swipeContainer)
    lateinit var swipeContainer: SwipeRefreshLayout
    @BindView(R.id.emptyView)
    lateinit var emptyView: LinearLayout

    private lateinit var mDonationHistoryAdapter: DonationHistoryAdapter
    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater!!.inflate(R.layout.fragment_donation_history, container,
                false)

        unbinder = ButterKnife.bind(this, rootView)

        mDonationHistoryAdapter = DonationHistoryAdapter()

        mDonationHistoryAdapter.registerAdapterDataObserver(object : AnythingChangedDataObserver() {
            override fun onAnythingChanged() {
                if (mDonationHistoryAdapter.itemCount > 0) {
                    CustomAnimations.circularHide(emptyView).start()
                } else {
                    CustomAnimations.circularReveal(emptyView).start()
                }
            }
        })

        rvDonations.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvDonations.itemAnimator = SlideInUpAnimator()
        rvDonations.adapter = mDonationHistoryAdapter

        swipeContainer.setOnRefreshListener { this.loadList() }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryLight)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    fun loadList() {
        if (isResumed) {
            swipeContainer.isRefreshing = true
            Log.d("DashboardFragment", "Finding Dashboard items...")
            ParseObservable.find(Donation.queryAllMyDonations()) //todo possible hang is here
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { mDonationHistoryAdapter.setItems(it) },
                            { error: Throwable -> Log.e("DashboardFragment", "Error getting dashboard pickups: " + error.message) },
                            { swipeContainer.isRefreshing = false })
        }
    }

    @OnClick(R.id.btnGoToDonate)
    fun onGoToDonateClick(btn: Button) {
        //TODO maybe nice ripple transition
        (activity as MainActivity).selectMenuItem(R.id.navigation_give)
    }

    companion object {

        fun newInstance(): DonationHistoryFragment {
            val f = DonationHistoryFragment()
            return f
        }
    }
}
