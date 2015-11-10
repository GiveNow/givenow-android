package io.givenow.app.fragments.main.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import io.givenow.app.R;

import butterknife.ButterKnife;

public class DropOffLocationsFragment extends MapHostingFragment implements GoogleMap.OnInfoWindowClickListener {

    public DropOffLocationsFragment() {
        // Required empty public constructor
    }

    public static DropOffLocationsFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        DropOffLocationsFragment f = new DropOffLocationsFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_drop_off, container, false);
        ButterKnife.bind(this, v);

        Log.w(((Object) this).getClass().getSimpleName(), "onCreateView completed.");
        return v;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        super.onMapReady(map);
        map.setOnInfoWindowClickListener(this);

        mGoogleMap.setOnCameraChangeListener(cameraPosition -> {
            LatLng pos = mGoogleMap.getCameraPosition().target;
            ParseGeoPoint geoPoint = new ParseGeoPoint(pos.latitude, pos.longitude);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("DropOffAgency");
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
            // Restrict query to the 20 nearest locations so the phone doesn't explode
            query.whereNear("agencyGeoLocation", geoPoint);
            query.setLimit(20);

            query.findInBackground((list, e) -> {
                if (list == null) {
                    return;
                }

                for (Object item : list) {
                    ParseObject it = (ParseObject) item;
                    ParseGeoPoint geoPoint1 = it.getParseGeoPoint("agencyGeoLocation");
                    MarkerOptions marker = new MarkerOptions();
                    LatLng ll = new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude());
                    marker.position(ll);
                    marker.title(it.getString("agencyName"));
                    marker.snippet(it.getString("agencyAddress"));
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    //TODO: don't place the same markers on top of markers we already got?
                    map.addMarker(marker);
                }
            });
        });


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        LatLng ll = marker.getPosition();
        String uriBegin = "geo:" + ll.latitude + "," + ll.longitude;
        String query = marker.getTitle() + ", " + marker.getSnippet();
        String encodedQuery = Uri.encode( query  );
        String uriString = uriBegin + "?q=" + encodedQuery;
        Uri uri = Uri.parse( uriString );

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri );
        startActivity( intent );
    }
}
