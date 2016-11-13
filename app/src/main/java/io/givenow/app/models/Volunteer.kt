package io.givenow.app.models

import com.parse.ParseClassName
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable

/**
 * Created by aphex on 11/27/15.
 */

@ParseClassName("Volunteer")
class Volunteer : ParseObject {

    constructor() : super() {
    }

    constructor(user: ParseUser, isApproved: Boolean) : super() {
        this.user = user
        this.isApproved = isApproved
    }

    var user: ParseUser
        get() = getParseUser("user")
        set(user) = put("user", user)

    var isApproved: Boolean
        get() = getBoolean("isApproved")
        set(isApproved) = put("isApproved", isApproved)

    companion object {

        fun findUser(user: ParseUser): Observable<Volunteer> {
            return ParseObservable.first(ParseQuery.getQuery(Volunteer::class.java)
                    .whereEqualTo("user", user))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
