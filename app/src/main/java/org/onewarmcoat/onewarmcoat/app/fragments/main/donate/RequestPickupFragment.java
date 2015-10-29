package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.PlaceAutocompleteAdapter;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.MapHostingFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class RequestPickupFragment extends MapHostingFragment implements ResultCallback<PlaceBuffer>, AdapterView.OnItemClickListener {

    private static final double AUTOCOMPLETE_BIAS_RADIUS_METERS = 10000;

    @InjectView(R.id.actvAddress)
    AutoCompleteTextView actvAddress;

    @InjectView(R.id.btnClearAddress)
    ImageButton btnClearAddress;

    private PickUpDetailInteractionListener mListener;
    private PlaceAutocompleteAdapter mAdapter;

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
        View v = inflater.inflate(R.layout.fragment_request_pickup, container, false);
        ButterKnife.inject(this, v);

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
        Log.w(logTag(), "onCreateView completed.");
        return v;
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
    protected void onSetPickup(View view) {
        getAddressFromMapTarget().subscribe(address ->
                setAddressFieldText(address.getAddressLine(0)));

        LatLng pos = mGoogleMap.getCameraPosition().target;

        // zoom in map
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });

        //hide on-map address field
//        actvAddress.setVisibility(View.INVISIBLE);

        //show detail layout
        mListener.onLaunchRequestPickUpDetail(actvAddress.getText().toString(), pos.latitude, pos.longitude);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PickUpDetailInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLaunchPickUpDetailListener");
        }

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
        final Place place = places.get(0);

        // Format details of the place for display and show it in a TextView.
//        mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                place.getId(), place.getAddress(), place.getPhoneNumber(),
//                place.getWebsiteUri()));
//
//        // Display the third party attributions if set.
//        final CharSequence thirdPartyAttribution = places.getAttributions();
//        if (thirdPartyAttribution == null) {
//            mPlaceDetailsAttribution.setVisibility(View.GONE);
//        } else {
//            mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//            mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
//        }

//        getAddressFromLatLng(place.getLatLng()).foreachDoEffect(address ->
//                actvAddress.setText(address.getAddressLine(0)));

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


    // Container Activity must implement this interface
    public interface PickUpDetailInteractionListener {
        void onLaunchRequestPickUpDetail(String addr, double lat, double lng);

        void updateAddress(Address address);
    }
}
