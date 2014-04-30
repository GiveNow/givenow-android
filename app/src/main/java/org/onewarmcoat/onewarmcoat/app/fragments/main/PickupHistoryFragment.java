package org.onewarmcoat.onewarmcoat.app.fragments.main;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.PickupsAdapter;


public class PickupHistoryFragment extends Fragment {
    private ListView pickupHist;

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

        PickupsAdapter pa = new PickupsAdapter(getActivity());
        pickupHist = (ListView) rootView.findViewById(R.id.pickups);
        pickupHist.setAdapter(pa);

        return rootView;
    }
}
