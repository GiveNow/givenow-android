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

import java.text.DateFormat;
import java.util.Date;

public class PickupsAdapter extends ParseQueryAdapter {

    ParseUser foundUser;

    public PickupsAdapter(Context context) {

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {

                ParseQuery<ParseObject> query = new ParseQuery("PickupRequest");
                //REPLACE HARDCODED donorID LATER
                //query.whereEqualTo("donor", "Alex");
                String currentUsername = ParseUser.getCurrentUser().getUsername();
                query.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
                //query.whereEqualTo("donor", ParseUser.getCurrentUser());
                return query;
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.pickup_history_item, null);
        }

        super.getItemView(object, v, parent);

        TextView pickupDateView = (TextView) v.findViewById(R.id.pickupDate);
        Date d = object.getCreatedAt();
        String dateStr = DateFormat.getInstance().format(d);
        //String dateStr = object.getString("createdAt");
        pickupDateView.setText(dateStr);

        TextView pickupAddressView = (TextView) v.findViewById(R.id.pickupAddress);
        String pickupAddress = object.getString("address");
        pickupAddressView.setText(pickupAddress);

        TextView numItemsView = (TextView) v.findViewById(R.id.numItems);
        Number numItems = object.getNumber("donationValue");
        numItemsView.setText(numItems.toString());

        TextView itemTypeView = (TextView) v.findViewById(R.id.itemType);
        String itemType = object.getString("donationType");
        numItemsView.setText(itemType.toString());


        return v;
    }

}
