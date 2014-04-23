package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.FragmentManager;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.MapHostingFragment;

import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PickUpFragment extends MapHostingFragment implements
        ConfirmPickupDialog.ConfirmPickupDialogListener {

    @InjectView(R.id.etAddress)
    EditText etAddress;
    @InjectView(R.id.rlPickupDetailContainer)
    RelativeLayout rlPickupDetailContainer;
    @InjectView(R.id.spinnerPickupDates)
    Spinner spinnerPickupDates;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pick_up, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        //TODO: should we disable zoom controls? i only disabled them because its not nice to have them partially obscured by the pickup details overlay.
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (!mMapIsTouched) {
                    // save cpu cycles, only recalculate if we're not pressed, ie the user lifted their finger off
                    Address address = reverseGeocodeAddress();
                    if (address != null) {
                        etAddress.setText(address.getAddressLine(0));
                    }
                } else {
                    rlPickupDetailContainer.setVisibility(View.GONE);
                }
                //hacky way to make the map resize itself above the button
//                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT);
//                lp.addRule(RelativeLayout.ABOVE, R.id.btnSetPickup);
//                flMapLayout.setLayoutParams(lp);
            }
        });

    }

    @OnClick(R.id.btnSetPickup)
    protected void onSetPickup(View view) {
        Address address = reverseGeocodeAddress();
        if (address != null) {
            etAddress.setText(address.getAddressLine(0));
        }

        // zoom in map
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));

        //show date layout
        //hacky way to make the map resize itself above the detail layout
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
//        lp.addRule(RelativeLayout.ABOVE, R.id.rlPickupDetailContainer);
//        flMapLayout.setLayoutParams(lp);
        rlPickupDetailContainer.setVisibility(View.VISIBLE);

        // populate possible dates in spinner
        ArrayList<CharSequence> dates_list = new ArrayList<CharSequence>();
        dates_list.add("Wednesday, 4/30/2014");
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, dates_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerPickupDates.setAdapter(adapter);
    }

    @OnClick(R.id.btnSubmitPickup)
    protected void onSubmitPickup(View v) {
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

        String addrString = etAddress.getText().toString();

//        String addrString = "";
//        if (address != null){
//            for (int i = 0; i < address.getMaxAddressLineIndex(); i++ ){
//                addrString += ", " + address.getAddressLine(i);
//            }
////            Parcel parcel = new Parcel();
////            address.writeToParcel(parcel, 0);
////            addrString = ""
//
//        }
        ParseObject pickupRequest = new ParseObject("PickupRequest"); //table name
        pickupRequest.put("location", new ParseGeoPoint(pos.latitude, pos.longitude)); //guessing it is relatively easy to get
        pickupRequest.put("pickupDate", new Date()); //obviously, this will just be now
        pickupRequest.put("name", name);
        pickupRequest.put("address", addrString);
        pickupRequest.saveInBackground();

        Toast.makeText(getActivity(), "Pickup Confirmed! Saved " + name + " and " + phoneNumber + " to Parse!", Toast.LENGTH_LONG).show();

    }

}
