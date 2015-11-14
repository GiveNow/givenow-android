package io.givenow.app;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;
import io.givenow.app.models.Donation;
import io.givenow.app.models.DonationCategory;
import io.givenow.app.models.PickupRequest;

/**
 * Created by craigmartin on 4/29/14.
 */
public class OWCApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        //init Parse

        // Enable Crash Reporting
//        ParseCrashReporting.enable(this);
        Fabric.with(this, new Crashlytics());

        ParseObject.registerSubclass(Donation.class);
        ParseObject.registerSubclass(DonationCategory.class);
        ParseObject.registerSubclass(PickupRequest.class);
        Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");
        ParseUser.enableAutomaticUser();
//        PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_launcher);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
