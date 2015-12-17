package io.givenow.app.fragments.main.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.fragments.PageSlidingTabStripFragment;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.ParseUserHelper;
import rx.parse.ParseObservable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


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
        usernameTV.setText(ParseUserHelper.getName().orSome(""));

        usernameTV.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveName();
            }
            return false;
        });

        usernameTV.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveName();
            }
        });
        return rootView;
    }

    private void saveName() {
        ParseUserHelper.setName(usernameTV.getText().toString());
        ParseObservable.save(ParseUser.getCurrentUser()).observeOn(mainThread()).subscribe(
                parseUser -> {
                    Log.d("ProfileFragment", "Name saved");
                },
                error -> ErrorDialogs.connectionFailure(getContext(), error));
    }

    public void refreshProfile() {
        dhFragment.refreshList(); //TODO crahses
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
