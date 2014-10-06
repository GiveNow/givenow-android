package org.onewarmcoat.onewarmcoat.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.Donation;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DashboardItemAdapter extends ParseQueryAdapter {

    public DashboardItemAdapter(Context context) {
        //items are all items where the pending volunteer = current user.
        super(context, (QueryFactory<PickupRequest>) PickupRequest::getMyDashboardPickups);

    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v != null) {
            holder = (ViewHolder) v.getTag();
        } else {
            v = LayoutInflater.from(getContext()).inflate(R.layout.dashboard_card_item, parent, false);
//            v = View.inflate(getContext(), R.layout.dashboard_card_item, null);
            holder = new ViewHolder(v);
            v.setTag(holder);
        }
        super.getItemView(object, v, parent);

        final PickupRequest pickupRequest = (PickupRequest) object;

        //set this as default case
        holder.tvStatus.setText("Waiting for Donor");
        holder.readyLayout.setVisibility(View.GONE);
        holder.tvNumCoats.setText(String.valueOf(pickupRequest.getNumberOfCoats()));
        holder.tvName.setText(CharityUserHelper.getFirstName(pickupRequest.getName()));
        holder.tvAddress.setText(pickupRequest.getAddresss());

        ParseUser confirmedVolunteer = pickupRequest.getConfirmedVolunteer();
        //if there is a confirmed volunteer and it is me, then say it is ready for pickup
        if(confirmedVolunteer != null && confirmedVolunteer.hasSameId(ParseUser.getCurrentUser())) {
            holder.tvStatus.setText("Ready for Pickup");
            holder.readyLayout.setVisibility(View.VISIBLE);
            setupStaticMap(holder, pickupRequest);
            setupCardActionButtons(holder, pickupRequest);
        }
        return v;
    }


    private void setupStaticMap(ViewHolder holder, final PickupRequest pickupRequest) {
        ParseGeoPoint gp = pickupRequest.getLocation();
        final Double lat = gp.getLatitude();
        final Double lng = gp.getLongitude();
        final String label = pickupRequest.getName();
        //TODO: use our custom marker
        String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?" +
                "center=" + gp.getLatitude() + "," + gp.getLongitude() +
                "&zoom=15&size=512x512&scale=2" +
                "&markers=color:blue%7Clabel:" + pickupRequest.getNumberOfCoats() +
                "%7C" + gp.getLatitude() + "," + gp.getLongitude() +
                "&maptype=roadmap&key=AIzaSyAtfxdA2mU_Jk_l6BIFRmasWp4H9jrKTuc";
        Picasso.with(getContext()).load(mapUrl).into(holder.ivMapContainer);

        holder.ivMapContainer.setOnClickListener(v -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            String uriStr = "geo:<" + lat + ">,<" + lng + ">?q=" + Uri.encode(pickupRequest.getAddresss());

//                "geo:" + lat + "," + lng + "?q="+lat+","+ lng +"("")"
            mapIntent.setData(Uri.parse(uriStr));
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Mapping "+ uriStr, Toast.LENGTH_LONG).show();
            getContext().startActivity(mapIntent);
        });
    }


    private void setupCardActionButtons(ViewHolder holder, final PickupRequest pickupRequest) {
        holder.btnCall.setTag(pickupRequest.getPhoneNumber());
        holder.btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String donorPhoneNum = (String) v.getTag();
            donorPhoneNum = donorPhoneNum.replaceAll("[^0-9]", "");
            String uriStr = "tel:" + donorPhoneNum;
            callIntent.setData(Uri.parse(uriStr));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Calling "+ uriStr, Toast.LENGTH_LONG).show();
            getContext().startActivity(callIntent);
        });

//        ParseGeoPoint gp = object.getParseGeoPoint("location");
        // regular map intent:
        // String uriBegin = "geo:" + gp.getLatitude() + "," + gp.getLongitude();
        // navigation intent:
        String uriBegin = "google.navigation:"; // + gp.getLatitude() + "," + gp.getLongitude();
        String query = pickupRequest.getAddresss();
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "q=" + encodedQuery;
        holder.btnMap.setTag(uriString);
        holder.btnMap.setOnClickListener(v -> {
            Intent navIntent = new Intent(Intent.ACTION_VIEW);
            String uriStr = (String) v.getTag();
            navIntent.setData(Uri.parse(uriStr));
            navIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Mapping "+ uriStr, Toast.LENGTH_LONG).show();
            getContext().startActivity(navIntent);
        });

        holder.btnFinishPickup.setOnClickListener(v -> {
            // DONATION CREATION
            final Donation donation = new Donation(pickupRequest.getDonor(), pickupRequest.getDonationType(), pickupRequest.getDonationValue(), pickupRequest.getNumberOfCoats());
            donation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        //send push to donor
                        pickupRequest.generatePickupCompleteNotif(getContext());
                        //create donation, and set it in the PickupRequest
                        pickupRequest.setDonation(donation);
                        pickupRequest.saveInBackground();
                        loadObjects();
                        //TODO: would be nice to make this card slide away
                    }
                }
            });
        });
        //TODO: Make Report Problem do something
    }

    static class ViewHolder {
        @InjectView(R.id.tvName)
        TextView tvName;
        @InjectView(R.id.tvAddress)
        TextView tvAddress;
        @InjectView(R.id.tvStatus)
        TextView tvStatus;
        @InjectView(R.id.tvNumCoats)
        TextView tvNumCoats;
        @InjectView(R.id.btnCall)
        Button btnCall;
        @InjectView(R.id.btnMap)
        Button btnMap;
        @InjectView(R.id.btnProblem)
        Button btnProblem;
        @InjectView(R.id.btnFinishPickup)
        Button btnFinishPickup;
        @InjectView(R.id.readyLayout)
        LinearLayout readyLayout;
        @InjectView(R.id.map_container)
        ImageView ivMapContainer;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}