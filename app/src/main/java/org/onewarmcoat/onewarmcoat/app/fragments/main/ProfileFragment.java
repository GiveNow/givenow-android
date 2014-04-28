package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.DonationsAdapter;
import org.onewarmcoat.onewarmcoat.app.adapters.PickupsAdapter;

import java.util.List;

public class ProfileFragment extends Fragment {

    public final String TAG = this.getClass().getSimpleName();

    private TextView usernameTV;
    private TextView phoneTV;
    private ListView historyLV;
    private ListView pickupHistoryLV;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        return f;
    }


    /**
     * If is volunteer: show parse username(id) and pickup history
     * If is donor: show parse username(id), donation name, number, and email
     * <p/>
     * 1.  silhouette
     * 2.  parse username to right
     * 3.  list of donations(donation name, phone, email)
     * 4.  list of pickups(pickup date, address, donationValue)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile, container,
                false);

        ParseUser currentUser = ParseUser.getCurrentUser();
        String objId = currentUser.getObjectId();
        String currUsername = currentUser.getUsername();
        // FOR DEBUGGING PURPOSES
        usernameTV = (TextView) rootView.findViewById(R.id.username);
        usernameTV.setText(currUsername);

        /*String email = currentUser.getEmail();
        emailTV.setText(email);*/
/*
        phoneTV = (TextView)rootView.findViewById(R.id.phoneno);
        Object phoneNumObj = currentUser.getString("phone");
        String phoneNum = currentUser.get("phone").toString();
        phoneTV.setText(phoneNum);*/

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", objId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    String phoneNum = objects.get(0).getString("phone");
                    phoneTV = (TextView) rootView.findViewById(R.id.phoneno);
                    phoneTV.setText(phoneNum);
                } else {
                    // Something went wrong.
                }
            }
        });

        DonationsAdapter adapter = new DonationsAdapter(getActivity());
        historyLV = (ListView) rootView.findViewById(R.id.donations);
        historyLV.setAdapter(adapter);

        PickupsAdapter pickupAdapter = new PickupsAdapter(getActivity());
        pickupHistoryLV = (ListView) rootView.findViewById(R.id.pickups);
        pickupHistoryLV.setAdapter(pickupAdapter);

        return rootView;
    }
}
