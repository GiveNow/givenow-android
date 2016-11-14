package io.givenow.app.fragments.main.profile


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import io.givenow.app.R
import io.givenow.app.adapters.PickupsAdapter
import io.givenow.app.interfaces.ViewPagerChangeListener


class PickupHistoryFragment : Fragment(), ViewPagerChangeListener {
    private lateinit var pickupHist: ListView
    private var mPickupsAdapter: PickupsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater!!.inflate(R.layout.fragment_pickup_history, container, false)

        mPickupsAdapter = PickupsAdapter(activity)
        pickupHist = rootView.findViewById(R.id.pickups) as ListView
        pickupHist.adapter = mPickupsAdapter

        return rootView
    }

    override fun onViewPagerShow() {
        refreshList()
    }

    override fun onViewPagerHide() {
    }

    fun refreshList() {
        mPickupsAdapter?.loadObjects()
    }

    companion object {

        fun newInstance(): PickupHistoryFragment {
            val f = PickupHistoryFragment()
            return f
        }
    }
}
