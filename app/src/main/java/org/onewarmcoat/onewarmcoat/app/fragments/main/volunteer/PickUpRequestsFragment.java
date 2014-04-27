package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.MapHostingFragment;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import java.util.List;

import butterknife.ButterKnife;


public class PickUpRequestsFragment extends MapHostingFragment {

    private OnMarkerClickListener listener;

    public PickUpRequestsFragment() {
        // Required empty public constructor
    }

    public static PickUpRequestsFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        PickUpRequestsFragment f = new PickUpRequestsFragment();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnMarkerClickListener) {
            listener = (OnMarkerClickListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement MyListFragment.OnMarkerClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pick_up_requests, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        super.onMapReady(map);

        ParseQuery<PickupRequest> query = PickupRequest.getAllActiveRequests();

        query.findInBackground(new FindCallback<PickupRequest>() {
            @Override
            public void done(List<PickupRequest> list, ParseException e) {
                for (PickupRequest item : list) {
                    ParseGeoPoint geoPoint = item.getLocation();
                    MarkerOptions marker = new MarkerOptions();
                    LatLng ll = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    marker.position(ll);
                    marker.title(item.getName());
                    marker.snippet(item.getAddresss());
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    //**create a HashMap <marker.getId(), item> so we can lookup pickupRequest details
                    map.addMarker(marker);
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                 /* in this callback function, when a marker on the map is
                 clicked, use fragment mgr to replace current fragment
                  */
                 @Override
                 public boolean onMarkerClick(Marker marker) {
                     String selectedTitle = marker.getTitle();
                     String selectedAddr = marker.getSnippet();

                     //pass value & type
                     //pass phone number
                     //**this is not going to work, save a HashMap <marker.getId(), item>

                     listener.onMarkerClicked(selectedTitle, selectedAddr);
                     return true;
                 }
            }
        );

    }

    public interface OnMarkerClickListener {
        public void onMarkerClicked(String dTitle, String dAddr);
    }
}
