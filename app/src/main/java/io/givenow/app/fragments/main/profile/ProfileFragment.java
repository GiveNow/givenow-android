package io.givenow.app.fragments.main.profile;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.fragments.PageSlidingTabStripFragment;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;


public class ProfileFragment extends PageSlidingTabStripFragment {

    String readableUsername;
    @Bind(R.id.silhouette)
    ImageView profileIV;
    @Bind(R.id.username)
    TextView usernameTV;
    @Bind(R.id.phoneno)
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

        ButterKnife.bind(this, rootView);

        phonenoTV.setText(ParseUserHelper.getPhoneNumber());
        usernameTV.setText(ParseUserHelper.getName().orSome("Anonymous"));

        return rootView;
    }


    public void setReadableName(String columnName, ParseUser currentUser) {
        ParseQuery<PickupRequest> query = ParseQuery.getQuery(PickupRequest.class);
        query.whereEqualTo(columnName, currentUser);

        // Execute the find asynchronously
        query.findInBackground((itemList, e) -> {
            if (e == null) {
                if (itemList.size() != 0) {
                    readableUsername = itemList.get(0).getString("name");
                    usernameTV.setText(readableUsername);
                }

            } else {
                Log.d("item", "Error: " + e.getMessage());
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
