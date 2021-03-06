package io.givenow.app.models;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fj.data.Option;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

/**
 * Data model for a PickupRequest.
 */
@ParseClassName("PickupRequest")
public class PickupRequest extends ParseObject implements ClusterItem, Serializable {
    public static final String VOLUNTEER_CONFIRMED = "volunteer_confirmed";
    public static final String PICKUP_COMPLETE = "pickup_complete";
    public static final String PROBLEM_REPORTED = "problem_reported";

    public PickupRequest() {
        // A default constructor is required.
    }

    public PickupRequest(ParseGeoPoint location, String address, String note,
                         ParseUser donor, Collection<DonationCategory> donationCategories) {
        super();
        setActive(true);
        setLocation(location);
        setAddress(address);
        setNote(note);
        setDonor(donor);
        setDonationCategories(donationCategories);
    }

    /**
     * Static Query Providers
     **/


    public static ParseQuery<PickupRequest> queryAll() {
        ParseQuery<PickupRequest> q = ParseQuery.getQuery(PickupRequest.class);
        q.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        q.include("donor");
        q.include("donationCategories");
        // may want to include other pointers, but this should do for now
        return q;
    }

    public static ParseQuery<PickupRequest> queryAllActive() {
        ParseQuery<PickupRequest> q = queryAll();
        q.whereEqualTo("isActive", true);
        return q;
    }

    /* All Pickup Requests which need volunteers to accept them. */
    public static ParseQuery<PickupRequest> queryAllOpenRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereDoesNotExist("pendingVolunteer");
        return q;
    }


    /* All Pickup Requests that I have accepted but have not picked up. */
    public static ParseQuery<PickupRequest> queryMyDashboardPickups() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);
        q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
        //we actually don't want this because then it is too restrictive
//        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereDoesNotExist("donation");
        q.orderByDescending("updatedAt"); //latest accepted requests first
        return q;
    }

    /* All Pickup Requests that I'm picking up and the donor has confirmed.
     * (This query is not currently used anywhere.) */
    public static ParseQuery<PickupRequest> queryMyConfirmedPickups() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        return q;
    }


    /* All Pickup Requests that I've picked up and successfully completed.
     * (Used in profile screen for volunteering history.) */
    public static ParseQuery<PickupRequest> queryMyCompletedPickups() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
        q.whereExists("donation");
        q.orderByDescending("createdAt");
        return q;
    }

    /*
    All Pickup Requests that I have made.
     */
    public static ParseQuery<PickupRequest> queryMyRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        return q;
    }

    /*
    All Pickup Requests that I have made,
    which currently have a pending volunteer,
    but no confirmed volunteer
     */
    public static ParseQuery<PickupRequest> queryMyPendingRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
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
    public static ParseQuery<PickupRequest> queryMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        q.whereDoesNotExist("donation");
        return q;
    }

    /*
    All Pickup Requests that I have made,
    which currently have a volunteer confirmed to be completing the pickup
     */
    public static ParseQuery<PickupRequest> queryAllMyConfirmedRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("confirmedVolunteer");
        return q;
    }

    /*
    All Pickup Requests that I have made, which were successfully delivered to the charity as a donation
     */
    public static ParseQuery<PickupRequest> queryMyCompletedRequests() {
        ParseQuery<PickupRequest> q = queryAllActive();
        q.whereEqualTo("donor", ParseUser.getCurrentUser());
        q.whereExists("donation");
        return q;
    }

    public static ParseQuery<PickupRequest> queryPickupRequestForDonation(Donation donation) {
        ParseQuery<PickupRequest> q = queryAll();
        q.whereEqualTo("donation", donation);
        q.orderByDescending("createdAt");
        return q;
    }

    /**
     * Properties
     **/

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
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

    @NonNull
    public Option<ParseUser> getPendingVolunteer() {
        return Option.fromNull(getParseUser("pendingVolunteer"));
    }

    public void setPendingVolunteer(ParseUser value) {
        put("pendingVolunteer", value);
    }

    public void cancelPendingVolunteer() {
        remove("pendingVolunteer");
    }

    @NonNull
    public Option<ParseUser> getConfirmedVolunteer() {
        return Option.fromNull(getParseUser("confirmedVolunteer"));
    }

    public void setConfirmedVolunteer(ParseUser value) {
        put("confirmedVolunteer", value);
    }

    @NonNull
    public Option<Donation> getDonation() {
        return Option.fromNull((Donation) getParseObject("donation"));
    }

    public void setDonation(Donation donation) {
        put("donation", donation);
    }

    public boolean getActive() {
        return getBoolean("isActive");
    }

    public void setActive(boolean b) {
        put("isActive", b);
    }

    /* Cancel this pickup request.*/
    public void cancel() {
        setActive(false);
    }

    public Observable<Object> claim() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pickupRequestId", getObjectId());
        return ParseObservable.callFunction("claimPickupRequest", params)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> confirmVolunteer() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pickupRequestId", getObjectId());
        return ParseObservable.callFunction("confirmVolunteer", params)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> pickUp() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pickupRequestId", getObjectId());
        return ParseObservable.callFunction("pickupDonation", params)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> markComplete() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pickupRequestId", getObjectId());
        return ParseObservable.callFunction("markComplete", params)
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public LatLng getPosition() {
        ParseGeoPoint loc = getLocation();
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PickupRequest that = (PickupRequest) o;

        return getObjectId().equals(that.getObjectId())
                && getUpdatedAt().equals(that.getUpdatedAt());
//                && getActive() == that.getActive()
//                && getLocation().getLatitude() == that.getLocation().getLatitude()
//                && getLocation().getLongitude() == that.getLocation().getLongitude()
//                && getAddress().equals(that.getAddress())
//                && getNote().equals(that.getNote())
//                && getDonor().getObjectId().equals(that.getDonor().getObjectId())
//                && getDonationCategories().equals(that.getDonationCategories());
    }

    @Override
    public int hashCode() {
        return getObjectId().hashCode();
    }
}
