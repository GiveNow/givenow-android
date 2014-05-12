package org.onewarmcoat.onewarmcoat.app.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Data model for a post.
 */
@ParseClassName("PickupRequest")
public class PickupRequest extends ParseObject implements ClusterItem, Serializable {
    public static final String VOLUNTEER_CONFIRMED = "volunteer_confirmed";
    public static final String PICKUP_COMPLETE = "pickup_complete";

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
                         ParseUser donor, String donationType, double donationValue, int numberOfCoats) {
        super();
        setLocation(location);
        setName(name);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setDonor(donor);
        setDonationType(donationType);
        setDonationValue(donationValue);
        setNumberOfCoats(numberOfCoats);
    }

    public static ParseQuery<PickupRequest> getQuery() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        return q;
    }

    public static ParseQuery<PickupRequest> getAllActiveRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereDoesNotExist("pendingVolunteer");
        return q;
    }

    public static ParseQuery<PickupRequest> getMyPendingPickups() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
        return q;
    }

    public static ParseQuery<PickupRequest> getMyDashboardPickups() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
        //we actually don't want this because then it is too restrictive
//        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereDoesNotExist("donation");
        return q;
    }

    public static ParseQuery<PickupRequest> getMyConfirmedPickups() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        return q;
    }

    public static ParseQuery<PickupRequest> getMyCompletedPickups() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereExists("donation");
        q.orderByDescending("createdAt");
        return q;
    }

    /*
    All Pickup Requests that I have made, which currently have a pending volunteer, but no confirmed volunteer
     */
    public static ParseQuery<PickupRequest> getMyPendingRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("pendingVolunteer");
        q.whereDoesNotExist("confirmedVolunteer");
        return q;
    }

    /*
    All Pickup Requests that I have made, which currently have a volunteer confirmed to be completing the pickup, but not delivered
     */
    public static ParseQuery<PickupRequest> getMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        q.whereDoesNotExist("donation");
        return q;
    }

    /*
    All Pickup Requests that I have made, which currently have a volunteer confirmed to be completing the pickup
     */
    public static ParseQuery<PickupRequest> getAllMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        return q;
    }

    /*
    All Pickup Requests that I have made, which were successfully delivered to the charity as a donation
     */
    public static ParseQuery<PickupRequest> getMyCompletedRequests() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("donation");
        return q;
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

    public int getNumberOfCoats() {
        int coats = getInt("numberOfCoats");

        //TODO: do we really want this? UI should probably limit you from ever entering 0
        if (coats < 1) {
            coats = 1;
        }
        return coats;
    }

    public void setNumberOfCoats(int numCoats) {
        put("numberOfCoats", numCoats);
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
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public void generatePendingVolunteerAssignedNotif() {
        //send pickup response back to donor
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", this.getDonor());

        //create Parse Data
        JSONObject data = new JSONObject();
        try {
            data.put("title", "Pickup Request Accepted");
//            data.put("alert", CharityUserHelper.getFirstName() + " is available to pickup your donation within the next hour.");
            data.put("alert", CharityUserHelper.getFirstName() + " is available to pickup your donation!");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setData(data);
//      push.setMessage("Pickup Request Confirmed: " + CharityUserHelper.getFirstName() + " is available to pickup your donation within the next hour.");
        push.sendInBackground();
    }

    public void generateVolunteerConfirmedNotif() {
        //send pickup response back to volunteer
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", this.getPendingVolunteer());

        //create Parse Data
        JSONObject data = new JSONObject();
        try {
            data.put("title", "Pickup Request Ready");
            data.put("alert", CharityUserHelper.getFirstName() + " is available to have their donation picked up.");
            data.put("type", VOLUNTEER_CONFIRMED);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setData(data);
//      push.setMessage("Pickup Request Confirmed: " + CharityUserHelper.getFirstName() + " is available to pickup your donation within the next hour.");
        push.sendInBackground();
    }

    public void generatePickupCompleteNotif() {
        //send pickup response back to donor
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", this.getDonor());

        //create Parse Data
        JSONObject data = new JSONObject();
        try {
            data.put("title", "Donation Successful");
            data.put("alert", CharityUserHelper.getFirstName() + " has successfully picked up your donation.");
            data.put("type", PICKUP_COMPLETE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setData(data);
//      push.setMessage("Pickup Request Confirmed: " + CharityUserHelper.getFirstName() + " is available to pickup your donation within the next hour.");
        push.sendInBackground();
    }
}
