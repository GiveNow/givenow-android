package io.givenow.app.fragments.main.common;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import fj.data.Option;
import io.givenow.app.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapHostingFragment extends Fragment
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected GoogleMap mGoogleMap;
    protected SupportMapFragment mapFragment;
    protected boolean mMapIsTouched;

    @Bind(R.id.flMapLayout)
    protected FrameLayout flMapLayout;

    private GoogleApiClient mGoogleApiClient;
    private boolean mZoomToLocation = true;
    private Geocoder mGeocoder;
    private CameraPosition mCameraPosition;

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(this.getClass().getSimpleName(), "onCreate.");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
    }

    protected void attachMapFragment() {
        if (isAdded()) {
            Log.w(this.getClass().getSimpleName(), "Attaching map fragment now.");
            if (mCameraPosition != null) {
                Log.w(this.getClass().getSimpleName(), "Initializing map with saved camera position " + mCameraPosition.toString());
                mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions()
                        .camera(mCameraPosition));
                mZoomToLocation = false;
            } else {
                Log.w(this.getClass().getSimpleName(), "Initializing map with default camera position.");
                mapFragment = SupportMapFragment.newInstance();
                mZoomToLocation = true;
            }

            getChildFragmentManager().beginTransaction()
                    .add(R.id.flMapContainer, mapFragment, "MAP")
                    .commit();
            mGoogleApiClient.connect();

            mapFragment.getMapAsync(this);
        } else {
            Log.e(this.getClass().getSimpleName(), "CANT ATTACH MAP FRAGMENT NOW BECAUSE WE'RE NOT ADDED.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapFragment != null) {
            outState.putParcelable("mCameraPosition", mapFragment.getMap().getCameraPosition());
//            pauseMapFragment();
//            getChildFragmentManager().putFragment(outState, "mapFragment", mapFragment);
        }
        Log.w(this.getClass().getSimpleName(), "onSaveInstanceState: Map saved");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w(this.getClass().getSimpleName(), "onActivityCreated called.");
        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable("mCameraPosition");
            Log.w(this.getClass().getSimpleName(), "onActivityCreated: Map restored");
        }

        Log.w(this.getClass().getSimpleName(), "onActivityCreated: Calling attachMapFragment.");
        attachMapFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void onMapReady(GoogleMap map) {
        flMapLayout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMapIsTouched = true;
                    break;
                case MotionEvent.ACTION_UP:
                    mMapIsTouched = false;
                    break;
            }
            return false; // determines whether the event is 'consumed' or not
        });

        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);

    }

    public boolean isMapTouched() {
        return mMapIsTouched;
    }

    @NonNull
    protected Observable<Address> getAddressFromMapTarget() {
        // capture pickup position
        return getAddressFromLatLng(getMapTarget());
    }

    public LatLng getMapTarget() {
        return mGoogleMap.getCameraPosition().target;
    }

    @NonNull
    public Observable<Address> getAddressFromLatLng(LatLng pos) {
        return Observable
                .defer(() -> {
                    List<Address> addresses = null;
                    try {
                        if (isOnline()) {
                            addresses = mGeocoder.getFromLocation(pos.latitude, pos.longitude, 1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses != null) {
                        if (addresses.size() > 0)
                            return Observable.just(addresses.get(0));
                    }
                    return Observable.empty();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public LatLngBounds convertCenterAndRadiusToBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (mZoomToLocation) {
            getLastLocation().foreachDoEffect(latLng -> {
//                Toast.makeText(getActivity(), "GPS location was found!", Toast.LENGTH_SHORT).show();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                mGoogleMap.animateCamera(cameraUpdate);
                mZoomToLocation = false;
            });
        }

    }

    public Option<LatLng> getLastLocation() {
        return Option.fromNull(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient))
                .map(location -> new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

}
