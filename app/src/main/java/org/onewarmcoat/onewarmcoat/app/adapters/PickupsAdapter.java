package org.onewarmcoat.onewarmcoat.app.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class PickupsAdapter extends ParseQueryAdapter {

    ParseUser foundUser;

    public PickupsAdapter(Context context) {

        super(context, new ParseQueryAdapter.QueryFactory<PickupRequest>() {
            public ParseQuery create() {
                //TODO: this should be completed pickups
                return PickupRequest.getMyConfirmedPickups();
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.pickup_history_item, null);
        }

        super.getItemView(object, v, parent);

        PickupRequest pickupRequest = (PickupRequest) object;

        TextView pickupDateView = (TextView) v.findViewById(R.id.pickupDate);
        Date d = pickupRequest.getCreatedAt();

        String dateStr = DateFormat.getInstance().format(d);
        pickupDateView.setText(dateStr);

        TextView pickupAddressView = (TextView) v.findViewById(R.id.pickupAddress);
        String pickupAddress = pickupRequest.getAddresss();
        pickupAddressView.setText(pickupAddress);

        TextView numItemsView = (TextView) v.findViewById(R.id.numItems);
        double numItems = pickupRequest.getDonationValue();
        DecimalFormat df = new DecimalFormat("#.00");
        numItemsView.setText("$" + df.format(numItems));

        //TODO: this should get the number of coats

        TextView itemTypeView = (TextView) v.findViewById(R.id.itemType);
        String itemType = pickupRequest.getDonationType();
        itemTypeView.setText(itemType.toString());


        return v;
    }

}
