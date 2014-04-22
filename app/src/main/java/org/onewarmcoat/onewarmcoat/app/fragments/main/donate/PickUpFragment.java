package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.ErrorDialogFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.GoogleMapFragment;
import org.onewarmcoat.onewarmcoat.app.fragments.main.ConfirmPickupDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PickUpFragment extends Fragment
        implements
        GoogleMapFragment.OnGoogleMapFragmentListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        ConfirmPickupDialog.ConfirmPickupDialogListener {

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @InjectView(R.id.btnSetPickup)
    Button btnSetPickup;
    @InjectView(R.id.btnSubmitPickup)
    Button btnSubmitPickup;
    @InjectView(R.id.etAddress)
    EditText etAddress;
    @InjectView(R.id.flMapLayout)
    FrameLayout flMapLayout;
    @InjectView(R.id.rlPickupDetailContainer)
    RelativeLayout rlPickupDetailContainer;
    @InjectView(R.id.spinnerPickupDates)
    Spinner spinnerPickupDates;

    private GoogleMap mGoogleMap;
    private MapFragment mapFragment;
    private LocationClient mLocationClient;
    private boolean mMapIsTouched;


    public PickUpFragment() {
        // Required empty public constructor
    }

    public static PickUpFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        PickUpFragment f = new PickUpFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getActivity(), this, this);
        mapFragment = GoogleMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.flMapContainer, mapFragment, "MAP").commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pick_up, container, false);
        ButterKnife.inject(this, v);

        btnSetPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetPickup(v);
            }
        });

        btnSubmitPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitPickup(v);
            }
        });

        flMapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMapIsTouched = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mMapIsTouched = false;
                        break;
                }
                return false; // determines whether the event is 'consumed' or not
            }
        });

//        Button btnMarker = (Button) v.findViewById(R.id.btnMarker);
//        btnMarker.setBackground(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
        if (mapFragment != null) {
//            map = mapFragment.getMap();
            if (map != null) {
                Toast.makeText(getActivity(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
                map.setMyLocationEnabled(true);
            } else {
                Toast.makeText(getActivity(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();

        }

        mGoogleMap.getUiSettings().setCompassEnabled(false);

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (!mMapIsTouched) {
                    // save cpu cycles, only recalculate if we're not pressed, ie the user lifted their finger off
                    reverseGeocodeAddress();
                }
//                rlPickupDetailContainer.setVisibility(View.GONE);
            }
        });

//        MarkerOptions marker = new MarkerOptions();
//        marker.anchor(0.5f, 0.5f);
//        map.addMarker(marker);

    }

    private void reverseGeocodeAddress() {
        // capture pickup position
        LatLng pos = mGoogleMap.getCameraPosition().target;

        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(pos.latitude, pos.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            if (addresses.size() > 0)
                etAddress.setText(addresses.get(0).getAddressLine(0));
        }
    }


    public void onSetPickup(View view) {
        reverseGeocodeAddress();
        // zoom in map

        //show date layout
        //hacky way to make the map resize itself above the detail layout
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ABOVE, R.id.rlPickupDetailContainer);
        flMapLayout.setLayoutParams(lp);
        rlPickupDetailContainer.setVisibility(View.VISIBLE);

        // populate possible dates in spinner
        ArrayList<CharSequence> dates_list = new ArrayList<CharSequence>();
        dates_list.add("Example Date!");
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, dates_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerPickupDates.setAdapter(adapter);
    }

    private void onSubmitPickup(View v) {
        // grab the address and date

        //spawn a dialogfragment
        showConfirmPickupDialog();


    }

    private void showConfirmPickupDialog() {
        FragmentManager fm = getChildFragmentManager();
        ConfirmPickupDialog confirmPickupDialog = ConfirmPickupDialog.newInstance("Confirm Pickup");
        confirmPickupDialog.show(fm, "fragment_confirm_pickup_dialog");

    }


    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        LatLng pos = mGoogleMap.getCameraPosition().target;

        ParseObject pickupRequest = new ParseObject("PickupRequest"); //table name
        pickupRequest.put("location", new ParseGeoPoint(pos.latitude, pos.longitude)); //guessing it is relatively easy to get
        pickupRequest.put("pickupDate", new Date()); //obviously, this will just be now
        pickupRequest.put("name", name);
        pickupRequest.saveInBackground();

        Toast.makeText(getActivity(), "Pickup Confirmed! Saved " + name + " and " + phoneNumber + " to Parse!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*
     * Called when the Fragment becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        if (isGooglePlayServicesAvailable()) {
            mLocationClient.connect();
        }

    }

    /*
     * Called when the Fragment is no longer visible.
     */
    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }


    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Location location = mLocationClient.getLastLocation();
        if (location != null) {
            Toast.makeText(getActivity(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition startCameraPosition = new CameraPosition.Builder()
                    .bearing(0.0f)
                    .target(new LatLng(0, 0)).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            if (mGoogleMap != null) {
                mGoogleMap.animateCamera(cameraUpdate);
            } else {
                Toast.makeText(getActivity(), "map is null, can't move camera!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(getActivity(), "Disconnected from location services. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /* Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /* Thrown if Google Play services canceled the original
                 * PendingIntent */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

}
