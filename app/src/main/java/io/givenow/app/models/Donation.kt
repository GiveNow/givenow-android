package io.givenow.app.models

import com.parse.ParseClassName
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import java.util.*

@ParseClassName("Donation")
class Donation : ParseObject {

    constructor() : super() {
    }

    constructor(donor: ParseUser, donationCategories: Collection<DonationCategory>) : super() {
        this.donor = donor
        this.donationCategories = donationCategories
    }

    var donor: ParseUser?
        get() = getParseUser("donor")
        set(donor) = put("donor", donor)

    var donationCategories: Collection<DonationCategory>
        get() = getList("donationCategories")
        set(donationCategories) = put("donationCategories", ArrayList(donationCategories))

    companion object {

        fun queryAllMyDonations(): ParseQuery<Donation> {
            val q = ParseQuery.getQuery(Donation::class.java)
            q.cachePolicy = ParseQuery.CachePolicy.NETWORK_ELSE_CACHE
            q.whereEqualTo("donor", ParseUser.getCurrentUser())
            q.orderByDescending("createdAt")
            return q
        }
    }

}
