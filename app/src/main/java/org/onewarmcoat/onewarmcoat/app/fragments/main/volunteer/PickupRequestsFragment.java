package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

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

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.common.MapHostingFragment;
import org.onewarmcoat.onewarmcoat.app.models.ParseUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;


public class PickupRequestsFragment extends MapHostingFragment implements ClusterManager.OnClusterClickListener<PickupRequest>,
        ClusterManager.OnClusterInfoWindowClickListener<PickupRequest>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PickupRequest> {

    private ClusterManager<PickupRequest> mClusterManager;
    private PickupRequestDetailInteractionListener mListener;
    private PullToRefreshLayout mPullToRefreshLayout;

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

        // add a PullToRefreshLayout, just so we can use its progress bar, heh.
        // or i could use the indeterminate actionbar spinner instead. point of debate.
        // Convert view to ViewGroup
        ViewGroup viewGroup = (ViewGroup) v;
        // Create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        // Setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)
                        // Commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

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

        mClusterManager = new ClusterManager<PickupRequest>(getActivity(), map);
        mClusterManager.setRenderer(new PickupRequestRenderer(map));
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<PickupRequest>(new GridBasedAlgorithm<PickupRequest>()));
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

            mPullToRefreshLayout.setRefreshing(true);
            query.findInBackground((list, e) -> {
                if (list == null) {
                    mPullToRefreshLayout.setRefreshComplete();
                    return;
                }

                mClusterManager.clearItems();
                for (PickupRequest item : list) {
                    //default clustering setup
                    mClusterManager.addItem(item);
                    mClusterManager.cluster();
                }
                mPullToRefreshLayout.setRefreshComplete();
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

//    @Override
//    public void onViewPagerShow() {
//        super.onViewPagerShow();
//        loadMarkers();
//    }

//    @Override
//    public void onViewPagerHide() {
//        super.onViewPagerHide();
//    }


//    @Override
//    public void onConfirmAcceptDialog() {
//        // donation gets saved.
//        // this should happen after volunteer says he picked up the donation
//        Double donationValue = selectedPickupReq.getDonationValue();
//        String donationType = selectedPickupReq.getDonationType();
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
            Bitmap icon = mIconGenerator.makeIcon(String.valueOf(pickupRequest.getNumberOfCoats()));

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            //set the title to the users name, and snippet to be number of coats
            markerOptions.title(ParseUserHelper.getFirstName(pickupRequest.getName()));
            markerOptions.snippet("Tap to accept this request!");
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // always be clustering
            return cluster.getSize() > 2;
        }
    }
}
