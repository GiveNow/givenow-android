package io.givenow.app.fragments.main.volunteer;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.parse.ParseQueryAdapter;

import io.givenow.app.R;
import io.givenow.app.adapters.DashboardItemAdapter;
import io.givenow.app.interfaces.ViewPagerChangeListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class DashboardFragment extends Fragment implements
        ViewPagerChangeListener, OnDismissCallback, ContextualUndoAdapter.DeleteItemCallback {

    @Bind(R.id.emptyView)
    LinearLayout emptyView;
    @Bind(R.id.lvItems)
    ListView lvItems;
    PullToRefreshLayout mPullToRefreshLayout;
    private DashboardItemAdapter mAdapter;

    public DashboardFragment() {
    }

    public static DashboardFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        DashboardFragment f = new DashboardFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new DashboardItemAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.bind(this, v);
        setHasOptionsMenu(true);

        lvItems.setEmptyView(emptyView);

        // Convert view to ViewGroup
        ViewGroup viewGroup = (ViewGroup) v;
        // Create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        // Setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)
                        // We need to mark the ListView and it's Empty View as pullable
                        // This is because they are not dirent children of the ViewGroup
                .theseChildrenArePullable(lvItems, lvItems.getEmptyView())
                        // Set a OnRefreshListener
                .listener(view -> loadDashboardItems())
                        // Commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                mPullToRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onLoaded(List list, Exception e) {
                mPullToRefreshLayout.setRefreshComplete();
            }
        });

        // Appearance Animation
        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
        swingBottomInAnimationAdapter.setAbsListView(lvItems);

        // Swipe-to-dismiss // actually, this doesnt make sense atm
//        SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(swingBottomInAnimationAdapter, this);
//        swipeDismissAdapter.setAbsListView(lvItems);

//        // Undo support (used to confirm whether pickup was dropped off)
//        ContextualUndoAdapter contextualUndoAdapter = new ContextualUndoAdapter(swipeDismissAdapter, R.layout.dashboard_item_contextual_undo, R.id.btnCompleteDropoff, this);
//        contextualUndoAdapter.setAbsListView(lvItems);

//        lvItems.setAdapter(contextualUndoAdapter);
        lvItems.setAdapter(swingBottomInAnimationAdapter);

        return v;
    }

    private void loadDashboardItems() {
        if (mAdapter != null) {
            mAdapter.loadObjects();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dashboard_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                loadDashboardItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewPagerShow() {
        loadDashboardItems();
    }

    @Override
    public void onViewPagerHide() {
    }

    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            //create the contextual view asking if complete
            Log.w("df", "dismiss");
        }
    }

    @Override
    public void deleteItem(int position) {
        //pickup dropped off. complete
        Log.w("df", "delete");

    }
}
