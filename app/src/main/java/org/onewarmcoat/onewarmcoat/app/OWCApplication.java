package org.onewarmcoat.onewarmcoat.app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;

import org.onewarmcoat.onewarmcoat.app.activities.MainActivity;
import org.onewarmcoat.onewarmcoat.app.models.Donation;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

/**
 * Created by craigmartin on 4/29/14.
 */
public class OWCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //init Parse
        ParseObject.registerSubclass(Donation.class);
        ParseObject.registerSubclass(PickupRequest.class);
        Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");
        PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_launcher);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
