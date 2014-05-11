package org.onewarmcoat.onewarmcoat.app.models;

import com.parse.ParseException;
import com.parse.ParseUser;

public class CharityUserHelper {

    public static String getName() {
        ParseUser user = ParseUser.getCurrentUser();
        String name = user.getString("name");

        if(name == null || name.isEmpty()){
            name = "Anonymous";
        }
        return name;
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

    public static void setName(String name) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
        user.saveInBackground();
    }

    public static String getPhoneNumber() {
        ParseUser user = ParseUser.getCurrentUser();
        return user.getString("phoneNumber");
    }

    public static String getPhoneNumber(ParseUser user) {
        String number = "";
        try {
            user.fetchIfNeeded();
            number = user.getString("phoneNumber");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return number;
    }

    public static void setPhoneNumber(String phoneNumber) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("phoneNumber", phoneNumber);
        user.saveInBackground();
    }

    //this way we only do 1 network call to save user.
    public static void setNameAndNumber(String name, String phoneNumber){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("name", name);
        user.put("phoneNumber", phoneNumber);
        user.saveInBackground();
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
        return CharityUserHelper.getName().split(" ")[0];
    }

    public static String getFirstName(String name) {
        //TODO: don't be so ghetto . . . we only want to show first name, but this is a sucky way to do it
        return name.split(" ")[0];
    }
}
