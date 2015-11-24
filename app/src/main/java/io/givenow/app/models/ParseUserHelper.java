package io.givenow.app.models;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import fj.data.Option;

public class ParseUserHelper {

    public static boolean isSignedUpWithPhoneNumber() {
        return ParseUserHelper.getPhoneNumber(ParseUser.getCurrentUser()).isSome();
//        return ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser());// ParseUser.getCurrentUser().getUsername() == null;
    }

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

    public static String getName() {
        ParseUser user = ParseUser.getCurrentUser();
        String name = user.getString("name");

        if (name == null) {
            name = "Anonymous";
        }
        return name;
    }

    public static void setName(String name) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
        user.saveInBackground();
    }

    public static String getName(ParseUser user) {
        String name = null;
        try {
            user.fetchIfNeeded();
            name = user.getString("name");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(name == null || name.isEmpty()){
            name = "Anonymous";
        }
        return name;
    }

    public static String getPhoneNumber() {
        ParseUser user = ParseUser.getCurrentUser();
        return user.getString("phoneNumber");
    }

    public static void setPhoneNumber(String phoneNumber) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("phoneNumber", phoneNumber);
        user.saveInBackground();
    }

    public static Option<String> getPhoneNumber(ParseUser user) {
        String number = null;
        try {
            user.fetchIfNeeded();
            number = user.getString("phoneNumber");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Option.fromNull(number);
    }

    //this way we only do 1 network call to save user.
    public static void setNameAndNumber(String name, String phoneNumber){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
        user.put("phoneNumber", phoneNumber);
        user.saveInBackground();
    }

    public static ParseFile getProfileImage() {
        ParseUser user = ParseUser.getCurrentUser();
        return user.getParseFile("profileImage");
    }

    public static void setProfileImage(ParseFile imageFile) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profileImage", imageFile);
    }

    public static String getStripeCustomerId() {
        ParseUser user = ParseUser.getCurrentUser();
        String stripeCustomerId = user.getString("stripeCustomerId");

        if(stripeCustomerId == null){
            stripeCustomerId = "";
        }

        return stripeCustomerId;
    }

    public static void setStripeCustomerId(String value) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("stripeCustomerId", value);
        user.saveInBackground();
    }

    public static boolean hasStripeCustomerId() {
        if(getStripeCustomerId().isEmpty()){
            return false;
        }
        return true;
    }

    public static String getFirstName() {
        //TODO: don't be so ghetto . . . we only want to show first name, but this is a sucky way to do it
        return ParseUserHelper.getName().split(" ")[0];
    }

    public static String getFirstName(String name) {
        //TODO: don't be so ghetto . . . we only want to show first name, but this is a sucky way to do it
        return name.split(" ")[0];
    }
}
