package io.givenow.app.fragments.main.volunteer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import io.givenow.app.R
import io.givenow.app.adapters.DashboardItemAdapter
import io.givenow.app.helpers.CustomAnimations
import io.givenow.app.interfaces.AnythingChangedDataObserver
import io.givenow.app.interfaces.ViewPagerChangeListener
import io.givenow.app.models.PickupRequest
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable

class DashboardFragment : Fragment(), ViewPagerChangeListener {

    @BindView(R.id.emptyView)
    lateinit var emptyView: LinearLayout
    @BindView(R.id.rvItems)
    lateinit var rvItems: RecyclerView

    @BindView(R.id.swipeContainer)
    lateinit var swipeContainer: SwipeRefreshLayout

    private lateinit var mAdapter: DashboardItemAdapter

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_dashboard, container, false)
        unbinder = ButterKnife.bind(this, v)

        mAdapter = DashboardItemAdapter()

        mAdapter.registerAdapterDataObserver(object : AnythingChangedDataObserver() {
            override fun onAnythingChanged() {
                if (mAdapter.itemCount > 0) {
                    CustomAnimations.circularHide(emptyView).start()
                } else {
                    CustomAnimations.circularReveal(emptyView).start()
                }
            }
        })

        //Grab the Recycler View and list all conversation objects in a vertical list
        rvItems.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvItems.itemAnimator = SlideInUpAnimator()
        rvItems.adapter = mAdapter

        swipeContainer.setOnRefreshListener { this.loadDashboardItems() }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryLight)

        // Consider adding Swipe-to-dismiss later

        return v
    }

    override fun onResume() {
        super.onResume()
        loadDashboardItems()
    }

    private fun loadDashboardItems() {
        if (isResumed) {
            swipeContainer.isRefreshing = true
            Log.d("DashboardFragment", "Finding Dashboard items...")
            ParseObservable.find(PickupRequest.queryMyDashboardPickups()) //todo possible hang is here
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { mAdapter.setItems(it) },
                            { error -> Log.e("DashboardFragment", "Error getting dashboard pickups: " + error.message) },
                            { //OnComplete:
                                Log.d("DashboardFragment", "queryMyDashboardPickups OnComplete")
                                swipeContainer.isRefreshing = false
                            })
        }
    }

    override fun onViewPagerShow() {
        if (isResumed) {
            loadDashboardItems()
        }
    }

    override fun onViewPagerHide() {
    }

    fun scrollDashboardTo(i: Int) {
        rvItems.scrollToPosition(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
    }

    companion object {

        fun newInstance(): DashboardFragment {
            val f = DashboardFragment()
            return f
        }
    }

}
