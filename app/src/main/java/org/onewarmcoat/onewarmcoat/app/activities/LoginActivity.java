package org.onewarmcoat.onewarmcoat.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;


public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ParseAnalytics.trackAppOpened(getIntent());

        //register exception handler for crazy google maps bug that seems to crash occasionally and doesn't appear to have a solution
        //StackOverflow link: http://stackoverflow.com/questions/19624437/random-nullpointerexception-on-google-maps-api-v2/19627149#19627149
//        registerExceptionHandler();

        //if current user is null, then let them login, otherwise go straight to MainActivity
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //auto-login user
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
        //else, user sees the login
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 1.  get phone number
     * 2.  see if phone no exists
     * a.  if it does, provide that info to profile
     * b.  if it doesn't, create it
     *
     * @param v
     */
    public void anonLogin(View v) {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d("MyApp", "Anonymous login failed.");
                } else {
                    //at this point we know we have a valid Parse User, so subscribe to your own Push Notif channel
                    //and this way we only subscribe once per user
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("user", ParseUser.getCurrentUser());
                    installation.saveInBackground();

                    Log.d("MyApp", "Anonymous user logged in.");
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        });


    }

}
