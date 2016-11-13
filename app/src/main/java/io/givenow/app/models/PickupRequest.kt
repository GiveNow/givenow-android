package io.givenow.app.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.parse.*
import fj.data.Option
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable
import java.io.Serializable
import java.util.*

/**
 * Data model for a PickupRequest.
 */
@ParseClassName("PickupRequest")
class PickupRequest : ParseObject, ClusterItem, Serializable {

    constructor() { // A default constructor is required.
    }

    constructor(location: ParseGeoPoint, address: String, note: String,
                donor: ParseUser, donationCategories: Collection<DonationCategory>) : super() {
        this.location = location
        this.address = address
        this.note = note
        this.donor = donor
        this.donationCategories = donationCategories
        this.active = true
    }

    /**
     * Properties
     */

    var location: ParseGeoPoint
        get() = getParseGeoPoint("location")
        set(value) = put("location", value)

    var address: String
        get() = getString("address")
        set(value) = put("address", value)

    var note: String
        get() = getString("note")
        set(note) = put("note", note)

    var donor: ParseUser
        get() = getParseUser("donor")
        set(value) = put("donor", value)

    var donationCategories: Collection<DonationCategory>
        get() = getList("donationCategories")
        set(donationCategories) = put("donationCategories", ArrayList(donationCategories))

    val pendingVolunteer: Option<ParseUser>
        get() = Option.fromNull(getParseUser("pendingVolunteer"))

    fun cancelPendingVolunteer() {
        remove("pendingVolunteer")
    }

    val confirmedVolunteer: Option<ParseUser>
        get() = Option.fromNull(getParseUser("confirmedVolunteer"))

    val donation: Option<Donation>
        get() = Option.fromNull(getParseObject("donation") as Donation)

    var active: Boolean
        get() = getBoolean("isActive")
        set(isActive) = put("isActive", isActive)

    /* Cancel this pickup request.*/
    fun cancel() {
        active = false
    }

    fun claim(): Observable<Any> {
        val params = HashMap<String, Any>()
        params.put("pickupRequestId", objectId)
        return ParseObservable.callFunction<Any>("claimPickupRequest", params)
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun confirmVolunteer(): Observable<Any> {
        val params = HashMap<String, Any>()
        params.put("pickupRequestId", objectId)
        return ParseObservable.callFunction<Any>("confirmVolunteer", params)
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun pickUp(): Observable<Any> {
        val params = HashMap<String, Any>()
        params.put("pickupRequestId", objectId)
        return ParseObservable.callFunction<Any>("pickupDonation", params)
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun markComplete(): Observable<Any> {
        val params = HashMap<String, Any>()
        params.put("pickupRequestId", objectId)
        return ParseObservable.callFunction<Any>("markComplete", params)
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getPosition(): LatLng {
        val loc = location
        return LatLng(loc.latitude, loc.longitude)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as PickupRequest

        return objectId == that.objectId && updatedAt == that.updatedAt
        //                && getActive() == that.getActive()
        //                && getLocation().getLatitude() == that.getLocation().getLatitude()
        //                && getLocation().getLongitude() == that.getLocation().getLongitude()
        //                && getAddress().equals(that.getAddress())
        //                && getNote().equals(that.getNote())
        //                && getDonor().getObjectId().equals(that.getDonor().getObjectId())
        //                && getDonationCategories().equals(that.getDonationCategories());
    }

    override fun hashCode(): Int {
        return objectId.hashCode()
    }

    companion object {
        val VOLUNTEER_CONFIRMED = "volunteer_confirmed"
        val PICKUP_COMPLETE = "pickup_complete"
        val PROBLEM_REPORTED = "problem_reported"

        /**
         * Static Query Providers
         */

        fun queryAll(): ParseQuery<PickupRequest> {
            val q = ParseQuery.getQuery(PickupRequest::class.java)
            q.cachePolicy = ParseQuery.CachePolicy.IGNORE_CACHE
            q.include("donor")
            q.include("donationCategories")
            // may want to include other pointers, but this should do for now
            return q
        }

        fun queryAllActive(): ParseQuery<PickupRequest> {
            val q = queryAll()
            q.whereEqualTo("isActive", true)
            return q
        }

        /* All Pickup Requests which need volunteers to accept them. */
        fun queryAllOpenRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereDoesNotExist("pendingVolunteer")
            return q
        }


        /* All Pickup Requests that I have accepted but have not picked up. */
        fun queryMyDashboardPickups(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.cachePolicy = ParseQuery.CachePolicy.IGNORE_CACHE
            q.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser())
            //we actually don't want this because then it is too restrictive
            //        q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser());
            q.whereDoesNotExist("donation")
            q.orderByDescending("updatedAt") //latest accepted requests first
            return q
        }

        /* All Pickup Requests that I'm picking up and the donor has confirmed.
     * (This query is not currently used anywhere.) */
        fun queryMyConfirmedPickups(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser())
            q.orderByDescending("createdAt")
            return q
        }


        /* All Pickup Requests that I've picked up and successfully completed.
     * (Used in profile screen for volunteering history.) */
        fun queryMyCompletedPickups(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("confirmedVolunteer", ParseUser.getCurrentUser())
            q.whereExists("donation")
            q.orderByDescending("createdAt")
            return q
        }

        /*
    All Pickup Requests that I have made.
     */
        fun queryMyRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            return q
        }

        /*
    All Pickup Requests that I have made,
    which currently have a pending volunteer,
    but no confirmed volunteer
     */
        fun queryMyPendingRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            q.whereExists("pendingVolunteer")
            q.whereDoesNotExist("confirmedVolunteer")
            return q
        }

        /*
    All Pickup Requests that I have made,
    which currently have a volunteer confirmed to be completing the pickup,
    but not delivered
     */
        fun queryMyConfirmedRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            q.whereExists("confirmedVolunteer")
            q.whereDoesNotExist("donation")
            return q
        }

        /*
    All Pickup Requests that I have made,
    which currently have a volunteer confirmed to be completing the pickup
     */
        fun queryAllMyConfirmedRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            q.whereExists("confirmedVolunteer")
            return q
        }

        /*
    All Pickup Requests that I have made, which were successfully delivered to the charity as a donation
     */
        fun queryMyCompletedRequests(): ParseQuery<PickupRequest> {
            val q = queryAllActive()
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            q.whereExists("donation")
            return q
        }

        fun queryPickupRequestForDonation(donation: Donation): ParseQuery<PickupRequest> {
            val q = queryAll()
            q.whereEqualTo("donation", donation)
            q.orderByDescending("createdAt")
            return q
        }
    }
}
