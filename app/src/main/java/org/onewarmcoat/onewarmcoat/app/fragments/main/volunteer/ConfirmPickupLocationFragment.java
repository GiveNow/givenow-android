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
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ConfirmPickupLocationFragment extends Fragment {
    private static PickupRequest pickupRequest;

    @InjectView(R.id.donorNameTV)
    TextView donorNameTV;

    @InjectView(R.id.donorAddressTV)
    TextView donorAddressTV;

    @InjectView(R.id.driverConfirmationBtn)
    Button driverConfirmationBtn;

    @InjectView(R.id.cancelBtn)
    Button cancelBtn;

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
        ButterKnife.inject(this, fragmentView);
        /* name could be anything ("My Clothes", "Two Fur Coats", "Alex Tam", etc
         * name originated from scraped javascript
         */
        pickupRequest = (PickupRequest) getArguments().getSerializable("pickupRequest");

        donorNameTV.setText(pickupRequest.getName());
        donorAddressTV.setText(pickupRequest.getAddresss());

        return fragmentView;
    }

    @OnClick(R.id.driverConfirmationBtn)
    public void onConfirm(Button b) {
        //this should be set when the volunteer confirms pickup
//                Donation row1 = new Donation(pickupRequest.getDonor(), Donation.COAT, 100);
//                row1.saveInBackground();

        //set the pending volunteer on the PickupRequest.  This marks the pickup request as pending, and not shown on the map to other volunteers
        pickupRequest.setPendingVolunteer(ParseUser.getCurrentUser());
        pickupRequest.saveInBackground();
        Toast.makeText(getActivity(), "saved the current volunteer as pending", Toast.LENGTH_SHORT).show();

        pickupRequest.generatePendingVolunteerAssignedNotif();

        //launch congrats fragment, waiting for user to confirm

        //need to re-draw pin in new color

        //TODO: fix this, need to not use child fragments
//        getActivity().getFragmentManager().popBackStack();
    }

    @OnClick(R.id.cancelBtn)
    public void onCancel(Button b) {
//        getActivity().getFragmentManager().popBackStack();
    }
}