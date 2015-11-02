package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.GridAdapter;
import org.onewarmcoat.onewarmcoat.app.adapters.PlaceAutocompleteAdapter;
import org.onewarmcoat.onewarmcoat.app.customviews.AdaptableGradientRectView;
import org.onewarmcoat.onewarmcoat.app.customviews.SlidingRelativeLayout;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.ConfirmRequestDialogFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.MapHostingFragment;
import org.onewarmcoat.onewarmcoat.app.models.DonationCategory;
import org.onewarmcoat.onewarmcoat.app.models.ParseUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RequestPickupFragment extends MapHostingFragment
        implements ResultCallback<PlaceBuffer>, AdapterView.OnItemClickListener,
        ConfirmRequestDialogFragment.ConfirmPickupDialogListener {

    private static final double AUTOCOMPLETE_BIAS_RADIUS_METERS = 10000;

    @Bind(R.id.btnSetPickup)
    Button btnSetPickup;

    @Bind(R.id.actvAddress)
    AutoCompleteTextView actvAddress;

    @Bind(R.id.btnClearAddress)
    ImageButton btnClearAddress;

    @Bind(R.id.llAddressInfoContainer)
    LinearLayout llAddressInfoContainer;

    @Bind(R.id.llInfo)
    LinearLayout llInfo;

    @Bind(R.id.agrv)
    AdaptableGradientRectView adaptableGradientRectView;

    @Bind(R.id.rvDonationCategories)
    RecyclerView rvDonationCategories;

    @Bind(R.id.slidingRLContainer)
    SlidingRelativeLayout slidingRLContainer;

    @Bind(R.id.tvInfo)
    TextView tvInfo;

    private PickUpDetailInteractionListener mListener;
    private PlaceAutocompleteAdapter mAdapter;
    private Animator fade_in;
    private Animator inflate_to_height;
    private boolean mConfirmAddressShowing = false;
    private Animator fade_out;
    private boolean mKeyCodeBackEventHandled = false;
    private GridAdapter mGridAdapter;
    private boolean mCategoryLayoutShowing = false;
    private GridLayoutManager mGridLayoutManager;
    private boolean mInputValidated = false;
    private PickupRequest mPickupRequest;
    private boolean mRequestSubmitted = false;

    public RequestPickupFragment() {
        // Required empty public constructor
    }

    public static RequestPickupFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        RequestPickupFragment f = new RequestPickupFragment();
        return f;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @OnClick(R.id.btnClearAddress)
    public void onClearAddress(ImageButton imageButton) {
        actvAddress.setText("", false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewRoot = inflater.inflate(R.layout.fragment_request_pickup, container, false);
        ButterKnife.bind(this, viewRoot);


        btnClearAddress.setVisibility(View.INVISIBLE);

        // Register a listener that receives callbacks when a suggestion has been selected
        actvAddress.setOnItemClickListener(this);

        //Clear address button is only visible if address field has focus.
        actvAddress.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus)
                btnClearAddress.setVisibility(View.VISIBLE);
            else
                btnClearAddress.setVisibility(View.INVISIBLE);
        });

        //TODO: deselect address field/hide keyboard if map is touched
//        FrameLayout flMapContainer = ButterKnife.findById(v, R.id.flMapContainer);
//        flMapContainer..setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                deselectAddressField();
//                return false;
//            }
//        });

        //Catch back button and animate away relevant views
        viewRoot.setOnKeyListener((v, keyCode, event) -> {
            //This event is raised twice for a back button press. Not sure why, but
            // here's a hack to only handle the first event.
            if (mKeyCodeBackEventHandled) {
                Log.d(logTag(), "Ignoring extra Back event.");
                mKeyCodeBackEventHandled = false;
                return true;
            } else {
                mKeyCodeBackEventHandled = true;
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mCategoryLayoutShowing) {
                        hideCategoryLayout();
                        return true;
                    } else if (mConfirmAddressShowing) {
                        hideConfirmAddress();
                        return true;
                    }
                }
                return false;
            }
        });

        // Calling the RecyclerView
        rvDonationCategories.setHasFixedSize(true);

        mGridAdapter = new GridAdapter();
        rvDonationCategories.setAdapter(mGridAdapter);

        // The number of Columns
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
//        rvDonationCategories.setItemAnimator(new DefaultItemAnimator());
//        mGridLayoutManager.
//        rvDonationCategories.setRecycledViewPool();
        rvDonationCategories.setLayoutManager(mGridLayoutManager);

        Log.w(logTag(), "onCreateView completed.");
        return viewRoot;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        map.getUiSettings().setCompassEnabled(false);

        map.setOnCameraChangeListener(cameraPosition -> {
            if (!isMapTouched()) {
                // save cpu cycles, only recalculate if we're not pressed, ie the user lifted their finger off
                getAddressFromMapTarget().subscribe(address -> {
                    Log.i(logTag(), "Map OnCameraChanged: Setting address in input field: " + address.getAddressLine(0));
                    setAddressFieldText(address.getAddressLine(0));
//                    mListener.updateAddress(address);
                });
            } else {
                //can remove the detail fragment here, but per uber UX we keep it displayed
            }

        });

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(getActivity(),
                getmGoogleApiClient(),
                null,
                null //AutocompleteFilter.create(Collections.singletonList(Place.TYPE_STREET_ADDRESS)) //Ugh, this doesnt work because google doesn't actually support the `address` filter on android.
        );
        actvAddress.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /* Retrieve the place ID of the selected item from the Adapter.
         The adapter stores each Place suggestion in a AutocompletePrediction from which we
         read the place ID and title.*/
        final AutocompletePrediction item = mAdapter.getItem(position);
        final String placeId = item.getPlaceId();
        final CharSequence primaryText = item.getPrimaryText(null);

        Log.i(logTag(), "Autocomplete item selected: " + primaryText);

        // Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
        Places.GeoDataApi
                .getPlaceById(RequestPickupFragment.this.getmGoogleApiClient(), placeId)
                .setResultCallback(RequestPickupFragment.this);

        //Toast.makeText(getActivity(), "Clicked: " + primaryText, Toast.LENGTH_SHORT).show();
        setAddressFieldText(primaryText.toString());
        deselectAddressField();
        Log.i(logTag(), "Called getPlaceById to get Place details for " + placeId);
    }

    public void setAddressFieldText(String text) {
        actvAddress.setText(text, false); // setText, and disable autocompletion.
        actvAddress.setSelection(text.length());
    }

    public void deselectAddressField() {
        flMapLayout.requestFocus();
        hideKeyboardFrom(getActivity(), flMapLayout);
    }

    @OnClick(R.id.btnSetPickup)
    protected void onSetPickup(Button button) {
        if (!mConfirmAddressShowing) {
            showConfirmAddress();
        } else {
            if (!mCategoryLayoutShowing) {
                showCategoryLayout();
            } else {
                confirmPickupRequest();
            }
        }
    }

    private void confirmPickupRequest() {
        Collection<DonationCategory> items = mGridAdapter.getSelectedItems();
        if (items.size() < 1) {
            //TODO: Highlight rlNumberCoats background to hint user to enter number of coats
            tvInfo.setText(R.string.error_insufficient_categories_selected);
        } else {
            ParseUser currUser = ParseUser.getCurrentUser();
            String myPhoneNumber = currUser.getString("phoneNumber");
            if (myPhoneNumber == null) {
                //user hasn't entered their phone before
                showConfirmPickupDialog("", "");
            } else {
                //they have entered their phone before, let's pre-populate it and their name
                String myName = currUser.getString("name");
//                showConfirmPickupDialog(myName, myPhoneNumber);
                onFinishConfirmPickupDialog(myName, myPhoneNumber);
            }

        }
    }

    private void showConfirmPickupDialog(String name, String phoneNumber) {
        FragmentManager fm = getChildFragmentManager();
        ConfirmRequestDialogFragment confirmRequestDialogFragment =
                ConfirmRequestDialogFragment.newInstance(getString(R.string.confirm_pickup_dialog_title), name, phoneNumber,
                        getResources().getText(R.string.donor_dialog_disclaimer));
        confirmRequestDialogFragment.show(fm, "fragment_confirm_request_dialog");
    }

    // after donor enters name and number and hits Confirm
    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        //update the current user's name and phone
        ParseUserHelper.setName(name);
        ParseUserHelper.setPhoneNumber(phoneNumber);

        // Associate the device with a user //TODO: maybe don't do this every time, only at the first time
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();

        //grab donation details
//        double donationValue;
//        String estimatedValueString = etEstimatedValue.getText().toString();
//        if (!estimatedValueString.equals("")) {
//            donationValue = Double.parseDouble(estimatedValueString);
//        } else {
//            donationValue = 0.0;
//        }
//        int numcoats = Integer.parseInt(etNumCoatsValue.getText().toString());
//\

        //get selected categories
        LatLng target = getMapTarget();
//        //ship it off to parse
        Collection<DonationCategory> selectedItems = mGridAdapter.getSelectedItems();
        mPickupRequest = new PickupRequest(
                new ParseGeoPoint(target.latitude, target.longitude),
                name,
                actvAddress.getText().toString(),
                phoneNumber,
                ParseUser.getCurrentUser(),
                selectedItems
        );
        savePickupRequest();
    }

    private void savePickupRequest() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        mPickupRequest.saveInBackground(this::shouldWeRetrySave);
    }


    public void shouldWeRetrySave(ParseException e) {
        if (e == null) {
            // saved successfully
            mRequestSubmitted = true;
            // detach this detail fragment, we're done here
            hideCategoryLayout();
            hideConfirmAddress();
        } else {
            // save did not succeed
            getActivity().setProgressBarIndeterminateVisibility(false);
            // show error notification dialog with retry or cancel
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pickupRequest_retryDialog_title)
                    .setMessage(R.string.pickupRequest_retryDialog_message)
                    .setPositiveButton(R.string.pickupRequest_retryDialog_retryLabel, (dialog, which) -> savePickupRequest())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // do nothing
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // entering animations
        fade_in = AnimatorInflater.loadAnimator(activity, R.animator.fade_in);
        inflate_to_height = AnimatorInflater.loadAnimator(activity, R.animator.inflate_to_height);

        fade_out = AnimatorInflater.loadAnimator(activity, R.animator.fade_out);

        try {
            mListener = (PickUpDetailInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLaunchPickUpDetailListener");
        }

    }
//    @butterknife.Bind(R.attr.actionBarSize)
//    int actionBarSize;

    public void showConfirmAddress() {
        mConfirmAddressShowing = true;

        tvInfo.setText(R.string.request_pickup_info_confirm_address);
        btnSetPickup.setText(getString(R.string.continue_label));

        // zoom in map
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true);
        TypedArray typedArray = getActivity().obtainStyledAttributes(typedValue.data, new int[]{R.attr.actionBarSize});
        int targetHeight = typedArray.getDimensionPixelSize(0, -1);
        typedArray.recycle();

        ValueAnimator anim = ValueAnimator.ofInt(llInfo.getMeasuredHeight(), targetHeight);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = llInfo.getLayoutParams();
            layoutParams.height = val;
            llInfo.setLayoutParams(layoutParams);
        });
        anim.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));

        llInfo.setAlpha(0);
        fade_in.setTarget(llInfo);
        Animator fade_in_clone = fade_in.clone();
        fade_in_clone.setTarget(adaptableGradientRectView);

        AnimatorSet set = new AnimatorSet();
        set.play(anim).with(fade_in).with(fade_in_clone);
        set.start();
    }

    private void hideConfirmAddress() {
        //Animate out.
        ValueAnimator anim = ValueAnimator.ofInt(llInfo.getMeasuredHeight(), 0);
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = llInfo.getLayoutParams();
            layoutParams.height = val;
            llInfo.setLayoutParams(layoutParams);
        });
        anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

        fade_out.setTarget(llInfo);
        fade_out.setTarget(adaptableGradientRectView);

        AnimatorSet set = new AnimatorSet();
        set.play(fade_out).with(anim);
        set.start();

        tvInfo.setText(R.string.request_pickup_info_confirm_address);
        btnSetPickup.setText(getString(R.string.button_set_pickup_location_label));
        mConfirmAddressShowing = false;
    }


    private void showCategoryLayout() {
        mCategoryLayoutShowing = true;

        tvInfo.setText(R.string.request_pickup_info_select_categories);
        btnSetPickup.setText(getString(R.string.button_confirm_donation_label));
//        btnSetPickup.setBackgroundResource(R.color.disabled);

//        rvDonationCategories.
        slidingRLContainer.setVisibility(View.VISIBLE);
        Animator slide_down_from_top = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_down_from_top);
        slide_down_from_top.setTarget(slidingRLContainer);
        slide_down_from_top.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                buildCategoryGrid();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        slide_down_from_top.start();
    }

    private void buildCategoryGrid() {
        //get categories from parse
//        mGridAdapter.clearItems();
        DonationCategory.getTop9().findInBackground((categoryList, error) -> {
            if (error == null) {
                mGridAdapter.setItems(categoryList);
            } else {
                Log.d("RPDF", "Error fetching categories: " + error.getMessage());
            }
        });
    }

    private void hideCategoryLayout() {
        //Animate down
        Animator slide_up_to_top = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_up_to_top);
        slide_up_to_top.setTarget(slidingRLContainer);
        slide_up_to_top.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                slidingRLContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        slide_up_to_top.start();
        tvInfo.setText(R.string.request_pickup_info_confirm_address);
        btnSetPickup.setText(getString(R.string.continue_label));
//        btnSetPickup.setBackgroundResource(R.color.colorAccent);
        mCategoryLayoutShowing = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResult(PlaceBuffer places) {
        if (!places.getStatus().isSuccess()) {
            // Request did not complete successfully
            Log.e(logTag(), "Place query did not complete. Error: " + places.getStatus().toString());
            places.release();
            return;
        }
        // Get the Place object from the buffer.
        Place place = places.get(0);

        // Animate map to place
        // perhaps maintain zoom level?
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13);
        mGoogleMap.animateCamera(cameraUpdate);

        Log.i(logTag(), "Place details received: " + place.getName());
        places.release();
    }

    public final String logTag() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        super.onConnected(dataBundle);
        mAdapter.setBounds(convertCenterAndRadiusToBounds(getLastLocation().orSome(getMapTarget()), AUTOCOMPLETE_BIAS_RADIUS_METERS));
    }

    // Container Activity must implement this interface
    public interface PickUpDetailInteractionListener {
        void onLaunchRequestPickUpDetail(String addr, double lat, double lng);

        void updateAddress(Address address);
    }
}
