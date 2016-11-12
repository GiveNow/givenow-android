package io.givenow.app

import android.app.Application
import android.os.StrictMode
import android.support.multidex.MultiDexApplication
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.parse.Parse
import com.parse.ParseObject
import com.parse.ParseUser
import io.fabric.sdk.android.Fabric
import io.givenow.app.models.*

/**
 * Created by craigmartin on 4/29/14.

 */
class GiveNowApplication : MultiDexApplication() {
    /* Google Analytics */
    private var mTracker: Tracker? = null
    /* MixPanel */
    lateinit var mixPanelProjectToken: String

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Log.d("Application", "BuildConfig.DEBUG is true. Running in debug mode.")
            //disable crashlytics
            Fabric.with(this, Crashlytics.Builder()
                    .core(CrashlyticsCore.Builder()
                            .disabled(BuildConfig.DEBUG)
                            .build())
                    .build())
            //disable Google Analytics
            GoogleAnalytics.getInstance(this).setDryRun(true)
            //disable MixPanel
            mixPanelProjectToken = "none"

            //enable strict mode
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectAll() // for all detectable problems
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build())
        } else {
            Log.d("Application", "BuildConfig.DEBUG is false. Running in release mode.")
            //enable crashlytics
            Fabric.with(this, Crashlytics())
            //enable MixPanel
            mixPanelProjectToken = "f9cec240b1fd9edf74c9cbf578481ad0"
        }

        registerParseClasses()
        initializeParse()
        logUserForCrashlytics(ParseUser.getCurrentUser())
    }

    private fun registerParseClasses() {
        ParseObject.registerSubclass(Donation::class.java)
        ParseObject.registerSubclass(DonationCategory::class.java)
        ParseObject.registerSubclass(PickupRequest::class.java)
        ParseObject.registerSubclass(Volunteer::class.java)
    }

    private fun initializeParse() {
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key))
        ParseUser.enableAutomaticUser()
        ParseUser.enableRevocableSessionInBackground()
        ParseInstallationHelper.updateInstallation()
    }

    private fun logUserForCrashlytics(parseUser: ParseUser) {
        Crashlytics.setUserIdentifier(parseUser.objectId)
        Crashlytics.setUserName(parseUser.username)
    }

    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    val defaultTracker: Tracker
        @Synchronized get() {
            if (mTracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                mTracker = analytics.newTracker(R.xml.global_tracker)
            }
            return mTracker!!
        }
}
