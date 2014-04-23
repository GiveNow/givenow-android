package org.onewarmcoat.onewarmcoat.app.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.onewarmcoat.onewarmcoat.app.R;

import java.text.DateFormat;
import java.util.Date;

public class DonationsAdapter extends ParseQueryAdapter {

    public DonationsAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery<ParseObject> query = new ParseQuery("Donation");
                //REPLACE HARDCODED donorID LATER
                query.whereEqualTo("donorID", "Alex");

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


        TextView donationNameView = (TextView) v.findViewById(R.id.donationName);
        String donationName = object.getString("donationName");
        donationNameView.setText(donationName);

        TextView donationValueView = (TextView) v.findViewById(R.id.donationValue);
        Number donationVal = object.getNumber("donationValue");
        donationValueView.setText(donationVal.toString());

        return v;
    }

}
