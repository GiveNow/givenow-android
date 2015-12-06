package io.givenow.app.fragments.main.volunteer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.parse.ParseQuery;

import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.fragments.main.common.MapHostingFragment;
import io.givenow.app.interfaces.ViewPagerChangeListener;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;

public class PickupRequestsFragment extends MapHostingFragment implements ClusterManager.OnClusterClickListener<PickupRequest>,
        ClusterManager.OnClusterInfoWindowClickListener<PickupRequest>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PickupRequest>,
        ViewPagerChangeListener {

    private ClusterManager<PickupRequest> mClusterManager;
    private PickupRequestDetailInteractionListener mListener;

    public PickupRequestsFragment() {
        // Required empty public constructor
    }

    public static PickupRequestsFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        PickupRequestsFragment f = new PickupRequestsFragment();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PickupRequestDetailInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PickupRequestDetailInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pickup_requests, container, false);
        ButterKnife.bind(this, v);
        setHasOptionsMenu(true);

        // TODO: add a loading indicator of some sort to show map loading?
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pickup_request_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                loadMarkers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        super.onMapReady(map);

        mClusterManager = new ClusterManager<>(getActivity(), map);
        mClusterManager.setRenderer(new PickupRequestRenderer(map));
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<>(new GridBasedAlgorithm<>()));
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
//        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        //onHiddenChanged doesn't get called first load, only when the state changes
        loadMarkers();
    }

    public void loadMarkers() {
        if (mClusterManager != null) {
            ParseQuery<PickupRequest> query = PickupRequest.getAllActiveRequests();

//            mPullToRefreshLayout.setRefreshing(true);
            query.findInBackground((list, e) -> {
                if (list == null) {
//                    mPullToRefreshLayout.setRefreshComplete();
                    return;
                }

                mClusterManager.clearItems();
                for (PickupRequest item : list) {
                    //default clustering setup
                    mClusterManager.addItem(item);
                    mClusterManager.cluster();
                }
//                mPullToRefreshLayout.setRefreshComplete();
            });
        }

    }

    @Override
    public boolean onClusterClick(Cluster<PickupRequest> pickupRequestCluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<PickupRequest> pickupRequestCluster) {

    }

    @Override
    public void onClusterItemInfoWindowClick(PickupRequest pickupRequest) {
        mListener.onLaunchPickupRequestDetail(pickupRequest);
    }

    public void removePickupRequestFromMap(PickupRequest pickupRequest){
        mClusterManager.removeItem(pickupRequest);
        //need to call cluster so the items are re-rendered (removing the item)
        mClusterManager.cluster();
    }

    @Override
    public void onViewPagerShow() {
//        super.onViewPagerShow();
        loadMarkers();
    }

    @Override
    public void onViewPagerHide() {
//        super.onViewPagerHide();
    }


//    @Override
//    public void onConfirmAcceptDialog() {
//        // donation gets saved.
//        // this should happen after volunteer says he picked up the donation
//        Double donationValue = selectedPickupReq.getDonationValue();
//        String donationType = selectedPickupReq.getDonationCategories();
//        ParseUser donor = selectedPickupReq.getDonor();
//        Donation newDonation = new Donation(donor, donationType, donationValue);
//        newDonation.saveInBackground();
//        selectedPickupReq.setDonation(newDonation);
//        selectedPickupReq.saveInBackground();
//    }

    // Container Activity must implement this interface
    public interface PickupRequestDetailInteractionListener {
        void onLaunchPickupRequestDetail(PickupRequest pickupRequest);
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
            Bitmap icon = mIconGenerator.makeIcon(""); //TODO: express categories in icon somehow?

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            //set the title to the users name, and snippet to be number of coats
            //TODO none name?
            markerOptions.title(ParseUserHelper.getName(pickupRequest.getDonor()).orSome(pickupRequest.getAddress()));
            markerOptions.snippet(getString(R.string.volunteer_accept_request_marker_cta));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // always be clustering
            return cluster.getSize() > 2;
        }
    }
}
