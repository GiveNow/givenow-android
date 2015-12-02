package io.givenow.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;
import io.givenow.app.models.Donation;
import io.givenow.app.models.DonationCategory;
import io.givenow.app.models.PickupRequest;
import io.givenow.app.models.Volunteer;

/**
 * Created by craigmartin on 4/29/14.
 */
public class GiveNowApplication extends Application {
    /* Google Analytics */
    private Tracker mTracker;
    /* MixPanel */
    private String mMixPanelProjectToken;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.d("Application", "BuildConfig.DEBUG is true. Running in debug mode.");
            //disable crashlytics
            Fabric.with(this, new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder()
                            .disabled(BuildConfig.DEBUG)
                            .build())
                    .build());
            //disable Google Analytics
            GoogleAnalytics.getInstance(this).setDryRun(true);
            //disable MixPanel
            setMixPanelProjectToken("none");
            //use dev parse db
        } else {
            Log.d("Application", "BuildConfig.DEBUG is false. Running in release mode.");
            //enable crashlytics
            Fabric.with(this, new Crashlytics());
            //enable MixPanel
            setMixPanelProjectToken("f9cec240b1fd9edf74c9cbf578481ad0");
            //use prod parse db
        }

        registerParseClasses();
        initializeParse();
    }

    private void registerParseClasses() {
        ParseObject.registerSubclass(Donation.class);
        ParseObject.registerSubclass(DonationCategory.class);
        ParseObject.registerSubclass(PickupRequest.class);
        ParseObject.registerSubclass(Volunteer.class);
    }

    private void initializeParse() {
        Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");
        ParseUser.enableAutomaticUser();
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public String getMixPanelProjectToken() {
        return mMixPanelProjectToken;
    }

    public void setMixPanelProjectToken(String mixPanelProjectToken) {
        this.mMixPanelProjectToken = mixPanelProjectToken;
    }
}
