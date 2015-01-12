package org.onewarmcoat.onewarmcoat.app.helpers;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Manages the app's connection to the GoogleAPIClient.
 */
public class GoogleApiClientManager implements
        GoogleApiClient.ConnectionCallbacks {

    private static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    public static Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public void build(Activity activity) {
        GoogleApiClient.OnConnectionFailedListener lActivity;
        try {
            lActivity = (GoogleApiClient.OnConnectionFailedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GoogleApiClient.OnConnectionFailedListener");
        }
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(lActivity)
                .addApi(LocationServices.API)
                .build();

    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        mGoogleMap.setMyLocationEnabled(true); // does this cause the crash? no
//        if (location != null) {
////            Toast.makeText(getActivity(), "GPS location was found!", Toast.LENGTH_SHORT).show();
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
////            CameraPosition startCameraPosition = new CameraPosition.Builder()
////                    .bearing(0.0f)
////                    .target(new LatLng(0, 0)).build();
//            if (mGoogleMap != null) {
//                if (mZoomToLocation) {
////                    mGoogleMap.setMyLocationEnabled(true);
////                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
////                    mGoogleMap.animateCamera(cameraUpdate);
//                    mZoomToLocation = false;
//                }
//            } else {
////                Toast.makeText(getActivity(), "map is null, can't move camera!", Toast.LENGTH_SHORT).show();
//            }
//        } else {
////            Toast.makeText(getActivity(), "Current location was null, enable GPS!", Toast.LENGTH_SHORT).show();
//        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
