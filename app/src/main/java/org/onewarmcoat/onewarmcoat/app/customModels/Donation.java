package org.onewarmcoat.onewarmcoat.app.customModels;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by atam on 4/22/2014.
 */
@ParseClassName("Donation")
public class Donation extends ParseObject {
    public Donation() {
        super();
    }

    public Donation(ParseUser donor, String donationType, int donationValue) {
        super();
        setDonor(donor);
        setdonationType(donationType);
        setDonationValue(donationValue);
    }

    public String getDonor() {
        return getString("donor");
    }

    public void setDonor(ParseUser donor) {
        put("donor", donor);

    }

    public String getdonationType() {
        return getString("donationType");
    }

    public void setdonationType(String donationType) {
        put("donationType", donationType);

    }

    public int getDonationValue() {
        return getInt("donationValue");
    }

    public void setDonationValue(int donationValue) {
        put("donationValue", donationValue);
    }

}
