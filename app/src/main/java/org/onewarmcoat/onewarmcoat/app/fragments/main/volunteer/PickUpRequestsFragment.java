package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.MapHostingFragment;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import java.util.List;

import butterknife.ButterKnife;


public class PickUpRequestsFragment extends MapHostingFragment implements ClusterManager.OnClusterClickListener<PickupRequest>,
        ClusterManager.OnClusterInfoWindowClickListener<PickupRequest>, ClusterManager.OnClusterItemClickListener<PickupRequest>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PickupRequest> {

    //    private OnMarkerClickListener listener;
    private ClusterManager<PickupRequest> mClusterManager;
    private ConfirmPickupInteractionListener mListener;

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
        try {
            mListener = (ConfirmPickupInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmPickupInteractionListener");
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

        mClusterManager = new ClusterManager<PickupRequest>(getActivity(), map);
        mClusterManager.setRenderer(new PickupRequestRenderer(map));
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        ParseQuery<PickupRequest> query = PickupRequest.getAllActiveRequests();

        query.findInBackground(new FindCallback<PickupRequest>() {
            @Override
            public void done(List<PickupRequest> list, ParseException e) {
                if (list == null) {
                    return;
                }

                for (PickupRequest item : list) {
                    //default clustering setup
                    mClusterManager.addItem(item);
                    mClusterManager.cluster();
                }
            }
        });

    }

    @Override
    public boolean onClusterClick(Cluster<PickupRequest> pickupRequestCluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<PickupRequest> pickupRequestCluster) {

    }

    @Override
    public boolean onClusterItemClick(PickupRequest pickupRequest) {
        Toast.makeText(getActivity(), "clicked on a marker, need to launch child fragment here", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(PickupRequest pickupRequest) {
        mListener.onLaunchConfirmPickup(pickupRequest);

//        FragmentManager fm = getChildFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
//
//        //this should just pass the pickupRequest
//        ConfirmPickupLocationFragment confirmPickupLocationFragment = ConfirmPickupLocationFragment.newInstance(pickupRequest);
//
//        ft.add(R.id.flMapContainer, confirmPickupLocationFragment);
//        ft.addToBackStack("pickupConfirmation");
//        ft.commit();
    }

    private class PickupRequestRenderer extends DefaultClusterRenderer<PickupRequest> {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());

        public PickupRequestRenderer(GoogleMap map) {
            super(getActivity(), map, mClusterManager);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View customMarker = inflater.inflate(R.layout.custom_marker, null);
            mIconGenerator.setContentView(customMarker);
        }

        @Override
        protected void onBeforeClusterItemRendered(PickupRequest pickupRequest, MarkerOptions markerOptions) {
            //draw marker with OneWarmCoat icon, and number of coats
            Bitmap icon = mIconGenerator.makeIcon(String.valueOf(pickupRequest.getNumberOfCoats()));

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            //set the title to the users name, and snippet to be number of coats
            markerOptions.title(pickupRequest.getName());
            markerOptions.snippet("10 coats!");
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // always be clustering
            return cluster.getSize() > 2;
        }
    }

    // Container Activity must implement this interface
    public interface ConfirmPickupInteractionListener {
        public void onLaunchConfirmPickup(PickupRequest pickupRequest);
    }
}
