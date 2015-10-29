package org.onewarmcoat.onewarmcoat.app.fragments.main.common;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.GoogleMapFragment;
import org.onewarmcoat.onewarmcoat.app.helpers.MapFragmentCounter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fj.data.Option;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


//import com.google.android.gms.location.LocationClient;

public class MapHostingFragment extends Fragment
        implements
        GoogleMapFragment.OnGoogleMapFragmentListener,
        GoogleApiClient.ConnectionCallbacks,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    protected GoogleMap mGoogleMap;
    protected MapFragment mapFragment;
    protected boolean mMapIsTouched;
    @InjectView(R.id.flMapLayout)
    protected FrameLayout flMapLayout;

    private GoogleApiClient mGoogleApiClient;
    private boolean mZoomToLocation;
    private Geocoder mGeocoder;
    //    private boolean mShouldAttachMapFragmentOnStart = false;
//    private boolean isVisibleInViewPager = false;

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());

        if (savedInstanceState == null) {
            mZoomToLocation = true;
        } else {
            Log.w(((Object) this).getClass().getSimpleName(), "loading mapFragment");
            // Disabled map fragment reloading for now because of how fragile GPS 6.5.87 is.
//            mapFragment = (GoogleMapFragment) getChildFragmentManager().getFragment(savedInstanceState, "mapFragment");
//            mapFragment = (GoogleMapFragment) getFragmentManager().getFragment(savedInstanceState, ((Object) this).getClass().getSimpleName());
//            mapFragment = (GoogleMapFragment) getChildFragmentManager().findFragmentById(R.id.flMapContainer);
//            mapFragment = savedInstanceState.getParcelable("mapFragment");
            Log.w(((Object) this).getClass().getSimpleName(), "mapFragment loaded");
            mZoomToLocation = false;
        }
    }

    protected void attachMapFragment() {
        if (isAdded()) {
            Log.w(this.getClass().getSimpleName(), "Attaching map fragment now.");
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.flMapContainer, mapFragment, "MAP")
                    .commit();
            mZoomToLocation = true;
            mGoogleApiClient.connect();

            mapFragment.getMapAsync(this);
        } else {
            Log.e(this.getClass().getSimpleName(), "CANT ATTACH MAP FRAGMENT NOW BECAUSE WE'RE NOT ADDED.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (mapFragment != null) {
//            pauseMapFragment();
//            getChildFragmentManager().putFragment(outState, "mapFragment", mapFragment);
//        }
//        Log.w(this.getClass().getSimpleName(), "onSaveInstanceState: Fragments saved");
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        Log.w(this.getClass().getSimpleName(), "onActivityCreated called.");
//        if (inState != null) {
//            mapFragment = (MapFragment) getChildFragmentManager().getFragment(inState, "mapFragment");
//            if (mapFragment != null) {
////                attachMapFragment();
////                resumeMapFragment();
//            }
//            Log.w(((Object) this).getClass().getSimpleName(), "onActivityCreated: Fragments restored");
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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

    @Override
    public void onStart() {
        super.onStart();
//        if (isVisibleInViewPager) {
        attachMapFragment();
//        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(getClass().getSimpleName(), "OnPause.");

        // To work around "DirectByteBuffer.put Attempt to get length of null array" errors in the Google Maps 6.5.87 library,
        // we must not have more than one map fragment running at once.
        // When we want a map fragment to Resume, any other map fragments must Pause, or the fatal crash occurs.
        // By judicious use of the mapFragment's onPause and onResume calls, we can facilitate this.
        if (mapFragment != null) {
            if (mapFragment.isAdded()) {
                mapFragment.onPause();
                MapFragmentCounter.dec();
                Log.w(this.getClass().getSimpleName(), "mapFragment paused.");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(getClass().getSimpleName(), "OnResume.");

        if (mapFragment != null) {
            if (mapFragment.isAdded()) {
                mapFragment.onResume();
                MapFragmentCounter.inc();
                Log.w(this.getClass().getSimpleName(), "mapFragment resumed.");
            } else {
                // possible cause of crashes after long-term returns from background.
                // if this is hit, do we need to attach the map fragment?
                Log.e(this.getClass().getSimpleName(), "onResume: mapFragment is not null, but is not Added!.");
            }
        } else {
//            if (isVisibleInViewPager) { //gotta detect if we're visible and only then attach
            attachMapFragment();
//            }
        }
    }

//    @Override
//    public void onViewPagerShow() {
//        isVisibleInViewPager = true;
//        Log.w(this.getClass().getSimpleName(), "I've been marked as visible in the Viewpager.");
//    }
//
//    @Override
//    public void onViewPagerHide() {
//        isVisibleInViewPager = false;
//    }

    /*
     * Called when the Fragment is no longer visible.
     */
    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
//        disconnectMap();
        super.onStop();
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
                .map(location -> new LatLng(location.getLatitude(), location.getLongitude())
                );
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
