package io.givenow.app.models;

import android.content.Context;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import fj.data.Option;
import io.givenow.app.R;
import io.givenow.app.helpers.ErrorDialogs;
import rx.parse.ParseObservable;

/**
 * Data model for a post.
 */
@ParseClassName("PickupRequest")
public class PickupRequest extends ParseObject implements ClusterItem, Serializable {
    public static final String VOLUNTEER_CONFIRMED = "volunteer_confirmed";
    public static final String PICKUP_COMPLETE = "pickup_complete";
    public static final String PROBLEM_REPORTED = "problem_reported";

    public PickupRequest() {
        // A default constructor is required.
    }

    //Full constructor, not sure if it will ever actually be used.
//    public PickupRequest(ParseGeoPoint location, Date pickupDate, String name, String address, String phoneNumber,
//                         ParseUser donor, String donationType, double donationValue, ParseUser pendingVolunteer, Donation donation, ParseUser confirmedVolunteer) {
//        super();
//        setLocation(location);
//        setPickupDate(pickupDate);
//        setName(name);
//        setAddress(address);
//        setPhoneNumber(phoneNumber);
//        setDonor(donor);
//        setDonationCategories(donationCategories);
//        setPendingVolunteer(pendingVolunteer);
//        setDonation(donation);
//        setConfirmedVolunteer(confirmedVolunteer);
//    }

    //Normal use case, the donation and volunteer shouldn't exist.
    public PickupRequest(ParseGeoPoint location, String address, String note,
                         ParseUser donor, Collection<DonationCategory> donationCategories) {
        super();
        setActive(true);
        setLocation(location);
//        setName(name); //deprecated
        setAddress(address);
        setNote(note);
//        setPhoneNumber(phoneNumber); //deprecated
        setDonor(donor);
        setDonationCategories(donationCategories);
    }

    public static ParseQuery<PickupRequest> getQuery() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        q.whereEqualTo("isActive", true);
        q.include("donor");
        q.include("donationCategories");
        // may want to include other pointers, but this should do for now
        return q;
    }

    public static ParseQuery<PickupRequest> getAllActiveRequests() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereDoesNotExist("pendingVolunteer");
        return q;
    }

    public static ParseQuery<PickupRequest> getMyPendingPickups() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
        return q;
    }

    public static ParseQuery<PickupRequest> getMyDashboardPickups() {
        ParseQuery<PickupRequest> q = getQuery();
        q.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
        //we actually don't want this because then it is too restrictive
//        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereDoesNotExist("donation");
        q.orderByDescending("updatedAt"); //latest accepted requests first
        return q;
    }

    public static ParseQuery<PickupRequest> getMyConfirmedPickups() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        return q;
    }

    public static ParseQuery<PickupRequest> getMyCompletedPickups() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereExists("donation");
        q.orderByDescending("createdAt");
        return q;
    }

    /*
    All Pickup Requests that I have made.
     */
    public static ParseQuery<PickupRequest> getMyRequests() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        return q;
    }

    /*
    All Pickup Requests that I have made,
    which currently have a pending volunteer,
    but no confirmed volunteer
     */
    public static ParseQuery<PickupRequest> getMyPendingRequests() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("pendingVolunteer");
        q.whereDoesNotExist("confirmedVolunteer");
        return q;
    }

    /*
    All Pickup Requests that I have made,
    which currently have a volunteer confirmed to be completing the pickup,
    but not delivered
     */
    public static ParseQuery<PickupRequest> getMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        q.whereDoesNotExist("donation");
        return q;
    }

    /*
    All Pickup Requests that I have made,
    which currently have a volunteer confirmed to be completing the pickup
     */
    public static ParseQuery<PickupRequest> getAllMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = getQuery();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        return q;
    }

    /*
    All Pickup Requests that I have made, which were successfully delivered to the charity as a donation
     */
    public static ParseQuery<PickupRequest> getMyCompletedRequests() {
        ParseQuery<PickupRequest> q = getQuery();
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

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String value) {
        put("address", value);
    }

    public String getNote() {
        return getString("note");
    }

    public void setNote(String note) {
        put("note", note);
    }

    public ParseUser getDonor() {
        return getParseUser("donor");
    }

    public void setDonor(ParseUser value) {
        put("donor", value);
    }

    public List<DonationCategory> getDonationCategories() {
        return getList("donationCategories");

    }

    public void setDonationCategories(Collection<DonationCategory> donationCategories) {
        put("donationCategories", new ArrayList<>(donationCategories));
    }

    public Option<ParseUser> getPendingVolunteer() {
        return Option.fromNull(getParseUser("pendingVolunteer"));
    }

    public void setPendingVolunteer(ParseUser value) {
        put("pendingVolunteer", value);
    }

    public Option<Donation> getDonation() {
        return Option.fromNull((Donation) getParseObject("donation"));
    }

    public void setDonation(Donation donation) {
        put("donation", donation);
    }

    public Option<ParseUser> getConfirmedVolunteer() {
        return Option.fromNull(getParseUser("confirmedVolunteer"));
    }

    public void setConfirmedVolunteer(ParseUser value) {
        put("confirmedVolunteer", value);
    }

    public boolean getActive() {
        return getBoolean("isActive");
    }

    public void setActive(boolean b) {
        put("isActive", b);
    }

    @Override
    public LatLng getPosition() {
        ParseGeoPoint loc = getLocation();
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    private void generatePushNotif(ParseUser target_user, String title, String message, String type) {
        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("user", target_user);

        //create Parse Data
        JSONObject data = new JSONObject();
        try {
            data.put("title", title);
            data.put("alert", message);
            data.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setData(data);
        push.sendInBackground();
    }

    public void generatePendingVolunteerAssignedNotif(Context context) {
        //send pickup response back to donor
        generatePushNotif(this.getDonor(),
                context.getString(R.string.notif_pending_volunteer_assigned_title),
                context.getString(R.string.notif_pending_volunteer_assigned_msg,
                        ParseUserHelper.getFirstName().orSome("A volunteer")),
                "");
    }

    public void generateVolunteerConfirmedNotif(Context context) {
        //send pickup confirmed notif to volunteer
        this.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).subscribe(
                        volunteer -> generatePushNotif(volunteer,
                                context.getString(R.string.notif_volunteer_confirmed_title),
                                context.getString(R.string.notif_volunteer_confirmed_msg,
                                        ParseUserHelper.getFirstName(volunteer).orSome("A donor")),
                                VOLUNTEER_CONFIRMED),
                        error -> ErrorDialogs.connectionFailure(context, error)));
    }

    public void generatePickupCompleteNotif(Context context) {
        //send pickup complete notif back to donor
        this.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).subscribe(
                        volunteer -> generatePushNotif(volunteer,
                                context.getString(R.string.notif_pickup_complete_title),
                                context.getString(R.string.notif_pickup_complete_msg,
                                        ParseUserHelper.getFirstName(volunteer).orSome("A volunteer")),
                                PICKUP_COMPLETE),
                        error -> ErrorDialogs.connectionFailure(context, error)));
    }

    public void reportProblem(Context context) {
        //there's a problem. send a notif to the donor with the problem description.
        this.getPendingVolunteer().foreachDoEffect(pendingVolunteer ->
                ParseObservable.fetchIfNeeded(pendingVolunteer).subscribe(
                        volunteer -> generatePushNotif(volunteer,
                                context.getResources().getString(R.string.notif_problem_reported_title),
                                context.getResources().getString(R.string.notif_problem_reported_msg,
                                        ParseUserHelper.getFirstName(volunteer).orSome("A volunteer")),
                                PROBLEM_REPORTED),
                        error -> ErrorDialogs.connectionFailure(context, error)));
    }

    /* Cancel this pickup request.
     */
    public void cancel() {
        setActive(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PickupRequest that = (PickupRequest) o;

        return getObjectId().equals(that.getObjectId())
                && getActive() == that.getActive()
                && getLocation().equals(that.getLocation())
                && getAddress().equals(that.getAddress())
                && getNote().equals(that.getNote())
                && getDonor().equals(that.getDonor())
                && getDonationCategories().equals(that.getDonationCategories());
    }

    @Override
    public int hashCode() {
        return getObjectId().hashCode();
    }
}
