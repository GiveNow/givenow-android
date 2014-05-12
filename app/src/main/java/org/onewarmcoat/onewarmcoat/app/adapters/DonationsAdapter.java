package org.onewarmcoat.onewarmcoat.app.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.Donation;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class DonationsAdapter extends ParseQueryAdapter {

    //public DonationsAdapter(Context context, final ParseUser donor) {
    public DonationsAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<Donation>() {
            public ParseQuery create() {
                return Donation.getAllMyDonations();
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.history_item, null);
        }

        super.getItemView(object, v, parent);

        Donation donation = (Donation) object;

        TextView donationDateView = (TextView) v.findViewById(R.id.donationDate);

        Date d = donation.getCreatedAt();
        String dateStr = DateFormat.getInstance().format(d);
        donationDateView.setText(dateStr);


        TextView donationTypeView = (TextView) v.findViewById(R.id.donationType);
        String donationType = donation.getDonationType();
        donationTypeView.setText(donationType);

        TextView donationValueView = (TextView) v.findViewById(R.id.donationValue);
        double donationVal = donation.getDonationValue();
        DecimalFormat df = new DecimalFormat("#");
        donationValueView.setText("$" + df.format(donationVal));

        return v;
    }

}
