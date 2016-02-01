package io.givenow.app.models;

import android.support.annotation.NonNull;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.HashMap;

import fj.data.Option;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

public class ParseUserHelper {

    public static boolean isRegistered() {
        return isRegistered(ParseUser.getCurrentUser());
    }

    public static boolean isRegistered(ParseUser user) {
        return !ParseAnonymousUtils.isLinked(user); // isLinked() returns True if Anonymous.
    }

    public static Observable<Object> sendCode(String phoneNumber, String smsBody) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("phoneNumber", phoneNumber);
        params.put("body", smsBody);
        return ParseObservable.callFunction("sendCode", params).observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Object> logIn(String phoneNumber, int smsCode) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("phoneNumber", phoneNumber);
        params.put("codeEntry", smsCode);
        return ParseObservable.callFunction("logIn", params).observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public static Option<String> getName() {
        return getName(ParseUser.getCurrentUser());
    }

    public static void setName(String name) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
    }

    @NonNull
    public static Option<String> getName(ParseUser user) {
        return Option.fromNull(user.getString("name"));
    }

    @NonNull
    public static Option<String> getFirstName() {
        return getFirstName(ParseUser.getCurrentUser());
    }

    @NonNull
    public static Option<String> getFirstName(ParseUser user) {
        return getName(user).map(ParseUserHelper::splitIntoFirstName);
    }

    public static String splitIntoFirstName(String name) {
        return name.split(" ")[0];
    }

    @NonNull
    public static String getPhoneNumber() {
        return getPhoneNumber(ParseUser.getCurrentUser());
    }

    @NonNull
    public static String getPhoneNumber(ParseUser user) {
        String username = user.getUsername();
        String phoneNumberString;
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber pn = phoneUtil.parse("+" + username, null);
            phoneNumberString = phoneUtil.format(pn, PhoneNumberUtil.PhoneNumberFormat.RFC3966).replace("tel:", "");
        } catch (NumberParseException e) {
            phoneNumberString = ParseUserHelper.isRegistered(user) ? username : "";
            e.printStackTrace();
        }
        return phoneNumberString;
    }

    @NonNull
    public static Option<ParseFile> getProfileImage() {
        return Option.fromNull(ParseUser.getCurrentUser().getParseFile("profileImage"));
    }

    public static void setProfileImage(ParseFile imageFile) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profileImage", imageFile);
    }
}
