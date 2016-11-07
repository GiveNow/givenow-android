package io.givenow.app.fragments.main.profile;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.givenow.app.R;
import io.givenow.app.adapters.ViewPagerAdapter;
import io.givenow.app.fragments.main.BaseFragment;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.ParseUserHelper;
import rx.parse.ParseObservable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


public class ProfileFragment extends BaseFragment {

    @BindView(R.id.silhouette)
    ImageView profileIV;
    @BindView(R.id.username)
    TextView usernameTV;
    @BindView(R.id.phoneno)
    TextView phonenoTV;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tablayout)
    TabLayout tabLayout;

    private ViewPagerAdapter mAdapter;

    private DonationHistoryFragment dhFragment;
    private PickupHistoryFragment phFragment;
    private Unbinder unbinder;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        unbinder = ButterKnife.bind(this, rootView);

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

        setupViewPager();
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    private void setupViewPager() {
        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        dhFragment = DonationHistoryFragment.newInstance();
        phFragment = PickupHistoryFragment.newInstance();
        mAdapter.addFrag(dhFragment, getString(R.string.tab_title_donation_history));
        mAdapter.addFrag(phFragment, getString(R.string.tab_title_volunteer_history));
        mViewPager.setAdapter(mAdapter);
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
//        dhFragment.refreshList(); //TODO crahses
//        phFragment.refreshList();
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
