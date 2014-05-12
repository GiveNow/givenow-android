package org.onewarmcoat.onewarmcoat.app.fragments.main.profile;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.PickupsAdapter;
import org.onewarmcoat.onewarmcoat.app.interfaces.ViewPagerChangeListener;


public class PickupHistoryFragment extends Fragment implements ViewPagerChangeListener {
    private ListView pickupHist;
    private PickupsAdapter mPickupsAdapter;

    public PickupHistoryFragment() {
        // Required empty public constructor
    }

    public static PickupHistoryFragment newInstance() {
        PickupHistoryFragment f = new PickupHistoryFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_pickup_history, container,
                false);

        mPickupsAdapter = new PickupsAdapter(getActivity());
        pickupHist = (ListView) rootView.findViewById(R.id.pickups);
        pickupHist.setAdapter(mPickupsAdapter);

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
        mPickupsAdapter.loadObjects();
    }
}
