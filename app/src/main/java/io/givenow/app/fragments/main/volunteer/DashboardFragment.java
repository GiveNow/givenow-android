package io.givenow.app.fragments.main.volunteer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class DashboardFragment extends Fragment implements
        ViewPagerChangeListener {

    @Bind(R.id.emptyView)
    LinearLayout emptyView;
    @Bind(R.id.rvItems)
    RecyclerView rvItems;
    PullToRefreshLayout mPullToRefreshLayout;
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

//        // Convert view to ViewGroup
//        ViewGroup viewGroup = (ViewGroup) v;

//        rvItemssetEmptyView(emptyView);

        //Grab the Recycler View and list all conversation objects in a vertical list
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvItems.setLayoutManager(layoutManager);
        rvItems.setItemAnimator(new SlideInUpAnimator());

        //The Query Adapter drives the recycler view, and calls back to this activity when the user
        // taps on a Conversation
        mAdapter = new DashboardItemAdapter();

        //Attach the Query Adapter to the Recycler View
        rvItems.setAdapter(mAdapter);
        // Create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(container.getContext());
        // Setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(container)
                // We need to mark the ListView and it's Empty View as pullable
                // This is because they are not dirent children of the ViewGroup
                .theseChildrenArePullable(container)
                // Set a OnRefreshListener
                .listener(view -> loadDashboardItems())
                // Commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);


//        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
//            @Override
//            public void onLoading() {
//                mPullToRefreshLayout.setRefreshing(true);
//            }
//
//            @Override
//            public void onLoaded(List list, Exception e) {
//                mPullToRefreshLayout.setRefreshComplete();
//            }
//        });

//        // Swipe-to-dismiss // actually, this doesnt make sense atm
////        SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(swingBottomInAnimationAdapter, this);
////        swipeDismissAdapter.setAbsListView(lvItems);
//
////        // Undo support (used to confirm whether pickup was dropped off)
////        ContextualUndoAdapter contextualUndoAdapter = new ContextualUndoAdapter(swipeDismissAdapter, R.layout.dashboard_item_contextual_undo, R.id.btnCompleteDropoff, this);
////        contextualUndoAdapter.setAbsListView(lvItems);
//
////        lvItems.setAdapter(contextualUndoAdapter);
//        lvItems.setAdapter(swingBottomInAnimationAdapter);

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
            mPullToRefreshLayout.setRefreshing(true);
            ParseObservable.find(PickupRequest.getMyDashboardPickups())
//                .observeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toList()
                    .subscribe(
                            mAdapter::setItems,
                            error -> Log.e("FeedFragment", "Error getting dashboard pickups: " + error.getMessage()),
                            () -> { //OnComplete:
                                if (mAdapter.getItemCount() > 0) {
                                    CustomAnimations.circularHide(emptyView);
                                } else {
                                    CustomAnimations.circularReveal(emptyView);
                                }
                                mPullToRefreshLayout.setRefreshComplete();
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
