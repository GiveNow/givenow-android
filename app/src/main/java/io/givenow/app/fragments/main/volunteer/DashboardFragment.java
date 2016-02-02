package io.givenow.app.fragments.main.volunteer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.adapters.DashboardItemAdapter;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.ViewPagerChangeListener;
import io.givenow.app.models.PickupRequest;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

public class DashboardFragment extends Fragment implements
        ViewPagerChangeListener {

    @Bind(R.id.emptyView)
    LinearLayout emptyView;
    @Bind(R.id.rvItems)
    RecyclerView rvItems;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    private DashboardItemAdapter mAdapter;

    public DashboardFragment() {
    }

    public static DashboardFragment newInstance() {
        DashboardFragment f = new DashboardFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.bind(this, v);

        mAdapter = new DashboardItemAdapter();

        //Grab the Recycler View and list all conversation objects in a vertical list
        rvItems.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvItems.setItemAnimator(new SlideInUpAnimator());
        rvItems.setAdapter(mAdapter);

        swipeContainer.setOnRefreshListener(this::loadDashboardItems);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryLight);

        // Consider adding Swipe-to-dismiss later

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardItems();
    }

    private void loadDashboardItems() {
//        if (mAdapter != null) {
//            mAdapter.loadObjects();
//        }
//        mAdapter.clearItems();

        if (isResumed()) {
            swipeContainer.setRefreshing(true);
            Log.d("DashboardFragment", "Finding Dashboard items...");
            ParseObservable.find(PickupRequest.queryMyDashboardPickups()) //todo possible hang is here
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            mAdapter::setItems,
                            error -> Log.e("DashboardFragment", "Error getting dashboard pickups: " + error.getMessage()),
                            () -> { //OnComplete:
                                Log.d("DashboardFragment", "queryMyDashboardPickups OnComplete");
                                if (mAdapter.getItemCount() > 0) {
                                    CustomAnimations.circularHide(emptyView).start();
                                } else {
                                    CustomAnimations.circularReveal(emptyView).start();
                                }
                                swipeContainer.setRefreshing(false);
                            }
                    );
        }
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.dashboard_menu, menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                loadDashboardItems();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onViewPagerShow() {
        if (isResumed()) {
            loadDashboardItems();
        }
    }

    @Override
    public void onViewPagerHide() {
    }

    public void scrollDashboardTo(int i) {
        rvItems.scrollToPosition(i);
    }

//    @Override
//    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
//        for (int position : reverseSortedPositions) {
//            //create the contextual view asking if complete
//            Log.w("df", "dismiss");
//        }
//    }
//
//    @Override
//    public void deleteItem(int position) {
//        //pickup dropped off. complete
//        Log.w("df", "delete");
//
//    }
}
