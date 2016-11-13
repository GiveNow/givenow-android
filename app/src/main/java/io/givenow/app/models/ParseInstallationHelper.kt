package io.givenow.app.models

import android.util.Log

import com.parse.ParseInstallation
import com.parse.ParseUser

/**
 * Created by aphex on 1/26/16.
 *
 */
object ParseInstallationHelper {

    fun associateUserWithDevice(user: ParseUser) {
        // Associate the device with a user
        val installation = ParseInstallation.getCurrentInstallation()
        installation.put("user", user)
        installation.saveInBackground()
    }

    fun updateInstallation() {
        Log.d("ParseInstallation", "Updating installation record " + ParseInstallation.getCurrentInstallation().installationId)
        ParseInstallation.getCurrentInstallation().saveInBackground()
    }
}
