package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.Donation;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

public class ConfirmPickupLocationFragment extends Fragment implements View.OnClickListener {
    private static PickupRequest pickupRequest;

    public ConfirmPickupLocationFragment() {

    }

    // keeps name and addr in bundle 'memory' for retrieval later in onCreateView
    public static ConfirmPickupLocationFragment newInstance(PickupRequest pickupRequest) {
        ConfirmPickupLocationFragment f = new ConfirmPickupLocationFragment();
        Bundle args = new Bundle();
        args.putSerializable("pickupRequest", pickupRequest);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View fragmentView = inf.inflate(R.layout.fragment_confirm_pickup_location, parent, false);
        /* name could be anything ("My Clothes", "Two Fur Coats", "Alex Tam", etc
         * name originated from scraped javascript
         */
        pickupRequest = (PickupRequest) getArguments().getSerializable("pickupRequest");

        TextView donorNameTV = (TextView) fragmentView.findViewById(R.id.donorNameTV);
        donorNameTV.setText(pickupRequest.getName());

        TextView donorAddressTV = (TextView) fragmentView.findViewById(R.id.donorAddressTV);
        donorAddressTV.setText(pickupRequest.getAddresss());


        Button driverConfirmationBtn = (Button) fragmentView.findViewById(R.id.driverConfirmationBtn);
        driverConfirmationBtn.setOnClickListener(this);
        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.driverConfirmationBtn:
                //this should be set when the volunteer confirms pickup
//                Donation row1 = new Donation(pickupRequest.getDonor(), "Coat", 100);
//                row1.saveInBackground();

                //set the pending volunteer on the PickupRequest.  This marks the pickup request as pending, and not shown on the map to other volunteers
                pickupRequest.setPendingVolunteer(ParseUser.getCurrentUser());
                pickupRequest.saveInBackground();
                Toast.makeText(getActivity(), "saved the current volunteer as pending", Toast.LENGTH_SHORT).show();

                //launch congrats fragment, waiting for user to confirm

                //need to re-draw pin in new color

                //TODO: fix this, need to not use child fragments
                getActivity().getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}