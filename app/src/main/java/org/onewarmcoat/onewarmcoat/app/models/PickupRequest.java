package org.onewarmcoat.onewarmcoat.app.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Data model for a post.
 */
@ParseClassName("PickupRequest")
public class PickupRequest extends ParseObject {
    public PickupRequest() {
        // A default constructor is required.
    }

    //Full constructor, not sure if it will ever actually be used.
    public PickupRequest(ParseGeoPoint location, Date pickupDate, String name, String address, String phoneNumber,
                         ParseUser donor, String donationType, double donationValue, Donation donation, ParseUser volunteer) {
        super();
        setLocation(location);
        setPickupDate(pickupDate);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
        setDonation(donation);
        setVolunteer(volunteer);
    }

    //Normal use case, the donation and volunteer shouldn't exist.
    public PickupRequest(ParseGeoPoint location, Date pickupDate, String name, String address, String phoneNumber,
                         ParseUser donor, String donationType, double donationValue) {
        super();
        setLocation(location);
        setPickupDate(pickupDate);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
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
        return getString("name");
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

    public Donation getDonation() {
        return (Donation) getParseObject("donation");
    }

    public void setDonation(Donation donation) {
        put("donation", donation);
    }

    public ParseUser getVolunteer() {
        return getParseUser("volunteer");
    }

    public void setVolunteer(ParseUser value) {
        put("volunteer", value);
    }

    public static ParseQuery<PickupRequest> getQuery() {
        return ParseQuery.getQuery(PickupRequest.class);
    }

    public static ParseQuery<PickupRequest> getAllActiveRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        return q.whereDoesNotExist("volunteer");
    }
}
