package org.onewarmcoat.onewarmcoat.app.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Data model for a post.
 */
@ParseClassName("PickupRequest")
public class PickupRequest extends ParseObject implements ClusterItem, Serializable {
    public PickupRequest() {
        // A default constructor is required.
    }

    //Full constructor, not sure if it will ever actually be used.
    public PickupRequest(ParseGeoPoint location, Date pickupDate, String name, String address, String phoneNumber,
                         ParseUser donor, String donationType, double donationValue, ParseUser pendingVolunteer, Donation donation, ParseUser confirmedVolunteer) {
        super();
        setLocation(location);
        setPickupDate(pickupDate);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
        setPendingVolunteer(pendingVolunteer);
        setDonation(donation);
        setconfirmedVolunteer(confirmedVolunteer);
    }

    //Normal use case, the donation and volunteer shouldn't exist.
    public PickupRequest(ParseGeoPoint location, String name, String address, String phoneNumber,
                         ParseUser donor, String donationType, double donationValue) {
        super();
        setLocation(location);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
    }

    public static ParseQuery<PickupRequest> getQuery() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        return q;
    }

    public static ParseQuery<PickupRequest> getAllActiveRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        return q.whereDoesNotExist("volunteer");
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public Date getPickupDate() {
        return getDate("pickupDate");
    }

    public void setPickupDate(Date value) {
        put("pickupDate", value);
    }

    public String getName() {
        String name = getString("name");

        if (name == null || name.isEmpty()) {
            name = "Anonymous";
        }
        return name;
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getAddresss() {
        return getString("address");
    }

    public void setAddress(String value) {
        put("address", value);
    }

    public String getPhoneNumber() {
        return getString("phoneNumber");
    }

    public void setPhoneNumber(String value) {
        put("phoneNumber", value);
    }

    public ParseUser getDonor() {
        return getParseUser("donor");
    }

    public void setDonor(ParseUser value) {
        put("donor", value);
    }

    public String getDonationType() {
        return getString("donationType");
    }

    public void setDonationType(String value) {
        put("donationType", value);
    }

    public double getDonationValue() {
        return getDouble("donationValue");
    }

    public void setDonationValue(double value) {
        put("donationValue", value);
    }

    public ParseUser getPendingVolunteer() {
        return getParseUser("pendingVolunteer");
    }

    public void setPendingVolunteer(ParseUser value) {
        put("pendingVolunteer", value);
    }

    public Donation getDonation() {
        return (Donation) getParseObject("donation");
    }

    public void setDonation(Donation donation) {
        put("donation", donation);
    }

    public ParseUser getConfirmedVolunteer() {
        return getParseUser("confirmedVolunteer");
    }

    public void setconfirmedVolunteer(ParseUser value) {
        put("confirmedVolunteer", value);
    }

    @Override
    public LatLng getPosition() {
        ParseGeoPoint loc = getLocation();
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        return ll;
    }


}
