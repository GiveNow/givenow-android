package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.MapHostingFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PickUpFragment extends MapHostingFragment {

    @InjectView(R.id.etAddress)
    EditText etAddress;

    private PickUpDetailInteractionListener mListener;

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
        Log.w(((Object) this).getClass().getSimpleName(), "onCreate!!!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pick_up, container, false);
        ButterKnife.inject(this, v);

        Log.w(((Object) this).getClass().getSimpleName(), "onCreateView completed.");
        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        mGoogleMap.getUiSettings().setCompassEnabled(false);

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (!mMapIsTouched) {
                    // save cpu cycles, only recalculate if we're not pressed, ie the user lifted their finger off
                    Address address = reverseGeocodeAddress();
                    if (address != null) {
                        setAddressFieldText(address.getAddressLine(0));
                        mListener.updateAddress(address);
                    }
                } else {
                    //can remove the detail fragment here, but per uber UX we keep it displayed
                }
            }
        });

        //TODO: Add a listener to etAddress to go to inputted addresses

    }

    public void setAddressFieldText(String text) {
        etAddress.setText(text);
    }

    @OnClick(R.id.btnSetPickup)
    protected void onSetPickup(View view) {
        Address address = reverseGeocodeAddress();

        String addrString = "";
        if (address != null) {
            addrString = address.getAddressLine(0);
            setAddressFieldText(addrString);
        }
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
//        etAddress.setVisibility(View.INVISIBLE);

        //show detail layout
        mListener.onLaunchPickUpDetail(addrString, pos.latitude, pos.longitude);
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

    // Container Activity must implement this interface
    public interface PickUpDetailInteractionListener {
        public void onLaunchPickUpDetail(String addr, double lat, double lng);

        public void updateAddress(Address address);
    }

}
