package io.givenow.app.fragments.main.profile;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.givenow.app.R;
import io.givenow.app.activities.MainActivity;
import io.givenow.app.adapters.DonationHistoryAdapter;
import io.givenow.app.fragments.main.BaseFragment;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.AnythingChangedDataObserver;
import io.givenow.app.models.Donation;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;


public class DonationHistoryFragment extends BaseFragment {

    @BindView(R.id.rvDonations)
    RecyclerView rvDonations;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.emptyView)
    LinearLayout emptyView;
    private DonationHistoryAdapter mDonationHistoryAdapter;
    private Unbinder unbinder;

    public DonationHistoryFragment() {
        // Required empty public constructor
    }

    public static DonationHistoryFragment newInstance() {
        DonationHistoryFragment f = new DonationHistoryFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_donation_history, container,
                false);

        unbinder = ButterKnife.bind(this, rootView);

        mDonationHistoryAdapter = new DonationHistoryAdapter();

        mDonationHistoryAdapter.registerAdapterDataObserver(new AnythingChangedDataObserver() {
            @Override
            public void onAnythingChanged() {
                if (mDonationHistoryAdapter.getItemCount() > 0) {
                    CustomAnimations.circularHide(emptyView).start();
                } else {
                    CustomAnimations.circularReveal(emptyView).start();
                }
            }
        });

        rvDonations.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvDonations.setItemAnimator(new SlideInUpAnimator());
        rvDonations.setAdapter(mDonationHistoryAdapter);

        swipeContainer.setOnRefreshListener(this::loadList);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark,
                R.color.colorPrimaryLight);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadList();
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

//    @Override
//    public void onViewPagerShow() {
//        refreshList();
//    }
//
//    @Override
//    public void onViewPagerHide() {
//
//    }

    public void loadList() {
//        if (mDonationHistoryAdapter != null) {
//            mDonationHistoryAdapter.loadObjects();
//        }
        if (isResumed()) {
            swipeContainer.setRefreshing(true);
            Log.d("DashboardFragment", "Finding Dashboard items...");
            ParseObservable.find(Donation.queryAllMyDonations()) //todo possible hang is here
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            mDonationHistoryAdapter::setItems,
                            error -> Log.e("DashboardFragment", "Error getting dashboard pickups: " + error.getMessage()),
                            () -> { //OnComplete:
                                Log.d("DashboardFragment", "queryMyDashboardPickups OnComplete");
                                //TODO if i add an empty view...
//                                if (mDonationHistoryAdapter.getItemCount() > 0) {
//                                    CustomAnimations.circularHide(emptyView);
//                                } else {
//                                    CustomAnimations.circularReveal(emptyView);
//                                }
                                swipeContainer.setRefreshing(false);
                            }
                    );
        }
    }

    @OnClick(R.id.btnGoToDonate)
    public void onGoToDonateClick(Button btn) {
        //TODO maybe nice ripple transition
        ((MainActivity) getActivity()).selectMenuItem(R.id.navigation_give);
    }
}
