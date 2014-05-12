package org.onewarmcoat.onewarmcoat.app.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Donation")
public class Donation extends ParseObject {
    public static final String CASH = "Cash";
    public static final String COAT = "Coat";
    public static final String Misc = "Misc";

    public Donation() {
        super();
    }

    public Donation(ParseUser donor, String donationType, double donationValue) {
        super();
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
    }

    public Donation(ParseUser donor, String donationType, double donationValue, int numberOfCoats) {
        super();
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
        setNumberOfCoats(numberOfCoats);
    }

    public String getDonor() {
        return getString("donor");
    }

    public void setDonor(ParseUser donor) {
        put("donor", donor);

    }

    public String getDonationType() {
        return getString("donationType");
    }

    public void setDonationType(String donationType) {
        put("donationType", donationType);

    }

    public double getDonationValue() {
        return getDouble("donationValue");
    }

    public void setDonationValue(double value) {
        put("donationValue", value);
    }

    public int getNumberOfCoats() {
        int coats = getInt("numberOfCoats");

        if (coats < 1) {
            coats = 1;
        }
        return coats;
    }

    public void setNumberOfCoats(int numCoats) {
        put("numberOfCoats", numCoats);
    }

    public static ParseQuery<Donation> getAllMyDonations() {
        ParseQuery<Donation> q = ParseQuery.getQuery(Donation.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        return q;
    }

}
