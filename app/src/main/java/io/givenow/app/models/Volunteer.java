package io.givenow.app.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

/**
 * Created by aphex on 11/27/15.
 */

@ParseClassName("Volunteer")
public class Volunteer extends ParseObject {

    public Volunteer() {
        super();
    }

//    public Donation(ParseUser donor, String donationType, double donationValue) {
//        super();
//        setDonor(donor);
//        setDonationCategories(donationType);
//    }

    public Volunteer(ParseUser user, boolean isApproved) {
        super();
        setUser(user);
        setApproved(isApproved);
    }

    public static Observable<Volunteer> findUser(ParseUser user) {
        return ParseObservable.first(ParseQuery.getQuery(Volunteer.class)
                .whereEqualTo("user", user))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public boolean isApproved() {
        return getBoolean("isApproved");
    }

//    public static ParseQuery<Volunteer> isAVolunteer() {
//        ParseQuery<Volunteer> q = ParseQuery.getQuery(Volunteer.class);
//        q.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
//        q.whereEqualTo("user", ParseUser.getCurrentUser());
//        return q;
//    }

    public void setApproved(boolean isApproved) {
        put("isApproved", isApproved);
    }
}
