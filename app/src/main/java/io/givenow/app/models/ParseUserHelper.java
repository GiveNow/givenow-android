package io.givenow.app.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.HashMap;

import fj.data.Option;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.parse.ParseObservable;

public class ParseUserHelper {

    public static boolean isRegistered() {
        return isRegistered(ParseUser.getCurrentUser());
    }

    public static boolean isRegistered(ParseUser user) {
        //        return ParseUserHelper.getPhoneNumber(ParseUser.getCurrentUser()).isSome();
        return !ParseAnonymousUtils.isLinked(user);// ParseUser.getCurrentUser().getUsername() == null;
    }

//    public static boolean
//    public static void registerUser(String phoneNumber) {
//        ParseUser currentUser = ParseUser.getCurrentUser();
//        currentUser.put("phoneNumber", phoneNumber);
//        currentUser.saveInBackground();
//    }

    public static void registerUserWithDevice(String phoneNumber) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("phoneNumber", phoneNumber);
//        currentUser.put("AuthData", "signedup!"); //clear out Anonymous status
        currentUser.saveInBackground(e -> associateWithDevice(currentUser));
//        ParseUser.getCurrentUser().signUpInBackground();

    }

    public static void associateWithDevice(ParseUser user) {
        // Associate the device with a user
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", user);
        installation.saveInBackground();
    }

    public static void signUpOrLogin(String phoneNumber, Action0 loginComplete) {
        //TODO excise
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
//        phoneNumberUtil.parse(phoneNumber)
        ParseObservable.first(ParseUser.getQuery().whereEqualTo("phoneNumber", phoneNumber))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            //user does already exist, let's "log them in"
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("phoneNumber", phoneNumber);
                            ParseObservable.callFunction("getUserSessionToken", params).subscribe(sessionToken -> {
                                ParseObservable.become(sessionToken.toString()).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(becameUser -> {
                                            Log.d("Onboarding", "Became user " + becameUser.get("phoneNumber"));
                                            ParseUserHelper.associateWithDevice(becameUser);
                                            loginComplete.call();
                                        });
                            });
                        },
                        error -> {
                            //user doesn't exist, let's sign them up
                            Log.d("Onboarding", "Existing user query error result: " + error.getMessage());
                            Log.d("Onboarding", "User with # " + phoneNumber + " doesn't exist, registering.");
                            ParseUserHelper.registerUserWithDevice(phoneNumber);
                            loginComplete.call();
                        });
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
        user.saveInBackground();
    }

    @NonNull
    public static Option<String> getName(ParseUser user) {
        String name = null;
        try {
            user.fetchIfNeeded();
            name = user.getString("name");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Option.fromNull(name);
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
        //TODO: don't be so ghetto . . . we only want to show first name, but this is a sucky way to do it
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

    public static ParseFile getProfileImage() {
        ParseUser user = ParseUser.getCurrentUser();
        return user.getParseFile("profileImage");
    }

    public static void setProfileImage(ParseFile imageFile) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profileImage", imageFile);
    }
}
