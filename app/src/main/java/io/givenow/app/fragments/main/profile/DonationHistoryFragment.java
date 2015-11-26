package io.givenow.app.fragments.main.profile;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.givenow.app.R;
import io.givenow.app.adapters.DonationsAdapter;
import io.givenow.app.interfaces.ViewPagerChangeListener;


public class DonationHistoryFragment extends Fragment implements ViewPagerChangeListener {

    private ListView donationsHist;
    private DonationsAdapter mDonationsAdapter;

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

        mDonationsAdapter = new DonationsAdapter(getActivity());
        donationsHist = (ListView) rootView.findViewById(R.id.donations);
        donationsHist.setAdapter(mDonationsAdapter);

        return rootView;
    }

    @Override
    public void onViewPagerShow() {
        refreshList();
    }

    @Override
    public void onViewPagerHide() {

    }

    public void refreshList() {
        if (mDonationsAdapter != null) {
            mDonationsAdapter.loadObjects();
        }
    }
}
