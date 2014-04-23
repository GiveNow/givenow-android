package org.onewarmcoat.onewarmcoat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends Activity {

    public String username;
    public String userphone;
    //private EditText usernameET;
    private EditText phoneET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        /*usernameET = (EditText) v.findViewById(R.id.usernameET);
        username = usernameET.getText().toString();*/

        phoneET = (EditText) findViewById(R.id.phoneET);
        Editable ed = phoneET.getText();
        if (ed != null)
            userphone = ed.toString();
        Parse.initialize(this, "c8IKIZkRcbkiMkDqdxkM4fKrBymrX7p7glVQ6u8d", "EFY5RxFnVEKzNOMKGKa3JqLR6zJlS4P6z0OPF3Mt");
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d("MyApp", "Anonymous login failed.");
                } else {
                    Log.d("MyApp", "Anonymous user logged in.");
                    user.put("phoneno", userphone);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        });


    }


}
