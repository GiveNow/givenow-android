package io.givenow.app.models;

import android.util.Log;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by aphex on 1/26/16.
 */
public class ParseInstallationHelper {

    public static void associateUserWithDevice(ParseUser user) {
        // Associate the device with a user
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", user);
        installation.saveInBackground();
    }

    public static void updateInstallation() {
        Log.d("ParseInstallation", "Updating installation record " + ParseInstallation.getCurrentInstallation().getInstallationId());
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
