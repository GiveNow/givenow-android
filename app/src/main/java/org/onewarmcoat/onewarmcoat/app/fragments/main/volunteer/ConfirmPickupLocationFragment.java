package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.customModels.Donation;

public class ConfirmPickupLocationFragment extends Fragment {


    public ConfirmPickupLocationFragment() {

    }

    // keeps name and addr in bundle 'memory' for retrieval later in onCreateView
    public static ConfirmPickupLocationFragment newInstance(String name, String addr) {
        ConfirmPickupLocationFragment f = new ConfirmPickupLocationFragment();
        Bundle args = new Bundle();
        args.putString("pickupName", name);
        args.putString("pickupAddr", addr);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View fragmentView = inf.inflate(R.layout.fragment_confirm_pickup_location, parent, false);
        /* name could be anything ("My Clothes", "Two Fur Coats", "Alex Tam", etc
         * name originated from scraped javascript
         */
        String name = getArguments().getString("pickupName");
        String addr = getArguments().getString("pickupAddr");

        TextView donorNameTV = (TextView) fragmentView.findViewById(R.id.donorNameTV);
        donorNameTV.setText(name);

        TextView donorAddressTV = (TextView) fragmentView.findViewById(R.id.donorAddressTV);
        donorAddressTV.setText(addr);


        Button driverConfirmationBtn = (Button) fragmentView.findViewById(R.id.driverConfirmationBtn);
        driverConfirmationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get anon donorID assoc with name or addr
                //Donation row1 = new Donation("xZyy12", "Misc", 100);
                //this is actually volunteer user . . . should be getting user object from the pickupRequest table, and putting in here
                Donation row1 = new Donation(ParseUser.getCurrentUser(), "Misc", 100);
                row1.saveInBackground();
            }
        });
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}