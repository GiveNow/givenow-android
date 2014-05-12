package org.onewarmcoat.onewarmcoat.app.fragments.main.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.PageSlidingTabStripFragment;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileFragment extends PageSlidingTabStripFragment {

    String readableUsername;
    @InjectView(R.id.silhouette)
    ImageView profileIV;
    @InjectView(R.id.username)
    TextView usernameTV;
    @InjectView(R.id.phoneno)
    TextView phonenoTV;

    private DonationHistoryFragment dhFragment;
    private PickupHistoryFragment phFragment;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        return f;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{"Donation History", "Volunteer History"};
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {
            //create fragments
            dhFragment = DonationHistoryFragment.newInstance();
            phFragment = PickupHistoryFragment.newInstance();
            Log.w("ProfileFragment", "onCreate: child fragments created");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.inject(this, rootView);

        ParseUser currentUser = ParseUser.getCurrentUser();
        String objId = currentUser.getObjectId();

        setReadableName("donor", currentUser);
        setReadableName("confirmedVolunteer", currentUser);

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("objectId", objId);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        String phoneNum = objects.get(0).getString("phone");
                        phonenoTV.setText(phoneNum);
                    } else {
//                        Toast.makeText(getActivity(), "NO RECORDS FOUND!", Toast.LENGTH_LONG);
                    }
                } else {
                    // Something went wrong.
                }
            }
        });
        return rootView;
    }


    public void setReadableName(String columnName, ParseUser currentUser) {
        ParseQuery<PickupRequest> query = ParseQuery.getQuery(PickupRequest.class);
        query.whereEqualTo(columnName, currentUser);

        // Execute the find asynchronously
        query.findInBackground(new FindCallback<PickupRequest>() {
            public void done(List<PickupRequest> itemList, ParseException e) {
                if (e == null) {
                    if (itemList.size() != 0) {
                        readableUsername = itemList.get(0).getString("name");
                        usernameTV.setText(readableUsername);
                    }

                } else {
                    Log.d("item", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void refreshProfile() {
        dhFragment.refreshList();
        phFragment.refreshList();
    }

    @Override
    protected Fragment getFragmentForPosition(int position) {
        Fragment frag = null;

        switch (position) {
            case 0: //PickUp Requests
                frag = dhFragment;
                break;
            case 1: //Drop Off Locations
                frag = phFragment;
                break;
        }
        return frag;
    }

}
