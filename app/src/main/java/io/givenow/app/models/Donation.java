package io.givenow.app.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;

@ParseClassName("Donation")
public class Donation extends ParseObject {
    public static final String CASH = "Cash";
    public static final String COAT = "Coat";
    public static final String Misc = "Misc";

    public Donation() {
        super();
    }

//    public Donation(ParseUser donor, String donationType, double donationValue) {
//        super();
//        setDonor(donor);
//        setDonationCategories(donationType);
//    }

    public Donation(ParseUser donor, Collection<DonationCategory> donationCategories) {
        super();
        setDonor(donor);
        setDonationCategories(donationCategories);
    }

    public static ParseQuery<Donation> getAllMyDonations() {
        ParseQuery<Donation> q = ParseQuery.getQuery(Donation.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        return q;
    }

    public String getDonor() {
        return getString("donor");
    }

    public void setDonor(ParseUser donor) {
        put("donor", donor);
    }

    public Collection<DonationCategory> getDonationCategories() {
        return getList("donationCategories");
    }

    public void setDonationCategories(Collection<DonationCategory> donationCategories) {
        put("donationCategories", new ArrayList<>(donationCategories));

    }

}
