package org.onewarmcoat.onewarmcoat.app.customModels;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by atam on 4/22/2014.
 */
@ParseClassName("Donation")
public class Donation extends ParseObject {
    public Donation() {
        super();
    }

    public Donation(String donorId, String donationName, int donationVal) {
        super();
        setDonorId(donorId);
        setDonationName(donationName);
        setDonationValue(donationVal);
    }

    public String getDonorId() {
        return getString("donorId");
    }

    public void setDonorId(String dId) {
        put("donorID", dId);

    }

    public String getDonationName() {
        return getString("donationName");
    }

    public void setDonationName(String donationName) {
        put("donationName", donationName);

    }

    public int getDonationValue() {
        return getInt("donationValue");
    }

    public void setDonationValue(int donationVal) {
        put("donationValue", donationVal);
    }

}
