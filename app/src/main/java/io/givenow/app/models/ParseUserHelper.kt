package io.givenow.app.models

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.parse.ParseAnonymousUtils
import com.parse.ParseFile
import com.parse.ParseUser
import fj.data.Option
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable

object ParseUserHelper {

    val isRegistered: Boolean
        get() = isRegistered(ParseUser.getCurrentUser())

    fun isRegistered(user: ParseUser): Boolean = !ParseAnonymousUtils.isLinked(user) // isLinked() returns True if Anonymous.

    fun sendCode(phoneNumber: String, smsBody: String): Observable<Any> {
        val params = hashMapOf(
                "phoneNumber" to phoneNumber,
                "body" to smsBody)
        return ParseObservable.callFunction<Any>("sendCode", params).observeOn(AndroidSchedulers.mainThread())
    }

    fun logIn(phoneNumber: String, smsCode: Int): Observable<Any> {
        val params = hashMapOf(
                "phoneNumber" to phoneNumber,
                "codeEntry" to smsCode)
        return ParseObservable.callFunction<Any>("logIn", params).observeOn(AndroidSchedulers.mainThread())
    }

    val name: Option<String>
        get() = getName(ParseUser.getCurrentUser())

    fun setName(name: String) {
        val user = ParseUser.getCurrentUser()
        user.put("name", name)
    }

    fun getName(user: ParseUser): Option<String> {
        return Option.fromNull(user.getString("name"))
    }

    val firstName: Option<String>
        get() = getFirstName(ParseUser.getCurrentUser())

    fun getFirstName(user: ParseUser): Option<String> = getName(user).map { splitIntoFirstName(it) }

    fun splitIntoFirstName(name: String): String = name.split(" ", limit = 1).first()

    val phoneNumber: String
        get() = getPhoneNumber(ParseUser.getCurrentUser())

    fun getPhoneNumber(user: ParseUser): String {
        val username = user.username
        var phoneNumberString: String
        try {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val pn = phoneUtil.parse("+" + username, null)
            phoneNumberString = phoneUtil.format(pn, PhoneNumberUtil.PhoneNumberFormat.RFC3966).replace("tel:", "")
        } catch (e: NumberParseException) {
            phoneNumberString = if (ParseUserHelper.isRegistered(user)) username else ""
            e.printStackTrace()
        }

        return phoneNumberString
    }

    var profileImage: Option<ParseFile>
        get() = Option.fromNull(ParseUser.getCurrentUser().getParseFile("profileImage"))
        set(imageFile) {
            val user = ParseUser.getCurrentUser()
            user.put("profileImage", imageFile.some())
        }
}
