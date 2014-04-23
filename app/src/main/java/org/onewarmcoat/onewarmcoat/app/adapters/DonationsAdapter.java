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

public class DonationsAdapter extends ParseQueryAdapter {

    public DonationsAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery<ParseObject> query = new ParseQuery("Donation");
                //REPLACE HARDCODED donorID LATER
                //query.whereEqualTo("donor", "Alex");
                query.whereEqualTo("donor", ParseUser.getCurrentUser());
                return query;
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.history_item, null);
        }

        super.getItemView(object, v, parent);

        TextView donationDateView = (TextView) v.findViewById(R.id.donationDate);

        Date d = object.getCreatedAt();
        String dateStr = DateFormat.getInstance().format(d);
        //String dateStr = object.getString("createdAt");
        donationDateView.setText(dateStr);


        TextView donationTypeView = (TextView) v.findViewById(R.id.donationType);
        String donationType = object.getString("donationType");
        donationTypeView.setText(donationType);

        TextView donationValueView = (TextView) v.findViewById(R.id.donationValue);
        Number donationVal = object.getNumber("donationValue");
        donationValueView.setText(donationVal.toString());

        return v;
    }

}
