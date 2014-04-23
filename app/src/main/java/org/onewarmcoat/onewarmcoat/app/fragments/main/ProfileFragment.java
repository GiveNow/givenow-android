package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.DonationsAdapter;
import org.onewarmcoat.onewarmcoat.app.customModels.Donation;

/**
 * Created by atam on 4/22/2014.
 */
public class ProfileFragment extends Fragment {

    private TextView usernameTV;
    private TextView phonenoTV;
    private ListView historyLV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container,
                false);

        /*usernameTV = (TextView) rootView.findViewById(R.id.username);
        ParseUser currentUser = ParseUser.getCurrentUser();
        String currentUsername = currentUser.getUsername();
        usernameTV.setText(currentUsername);*/

        ParseObject.registerSubclass(Donation.class);
        /*ParseQuery<Donation> query = ParseQuery.getQuery(Donation.class);
        query.whereEqualTo("donorId", "Alex");
        query.findInBackground(new FindCallback<Donation>() {
            public void done(List<Donation> itemList, ParseException e) {
                if (e == null) {
                    // Access the array of results here
                    //String firstItemId = itemList.get(0).getObjectId();
                    //Toast.makeText(TodoItemsActivity.this, firstItemId, Toast.LENGTH_SHORT).show();
                    String currentUser = itemList.get(0).getDonorId();
                    usernameTV.setText(currentUser);
                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });*/

        ParseUser currentUser = ParseUser.getCurrentUser();
        phonenoTV = (TextView) rootView.findViewById(R.id.phoneno);
        String currentPhoneNo = (String) currentUser.get("phoneno");
        phonenoTV.setText(currentPhoneNo);
        DonationsAdapter adapter = new DonationsAdapter(getActivity());
        adapter.setTextKey("name");


        historyLV = (ListView) rootView.findViewById(R.id.donations);
        historyLV.setAdapter(adapter);

        return rootView;
    }
}
