package org.onewarmcoat.onewarmcoat.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DashboardItemAdapter extends ParseQueryAdapter {

    public DashboardItemAdapter(Context context) {
        //items are all items where the pending volunteer = current user.
        super(context, new ParseQueryAdapter.QueryFactory<PickupRequest>() {
            public ParseQuery create() {
                return PickupRequest.getMyDashboardPickups();
            }
        });
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v != null) {
            holder = (ViewHolder) v.getTag();
        } else {
            v = View.inflate(getContext(), R.layout.dashboard_item, null);
            holder = new ViewHolder(v);
            v.setTag(holder);
        }
        super.getItemView(object, v, parent);

        PickupRequest pickupRequest = (PickupRequest) object;

        //set this as default case
        holder.tvStatus.setText("Waiting for donor to confirm");

        ParseUser confirmedVolunteer = pickupRequest.getConfirmedVolunteer();

        //if there is a confirmed volunteer and it is me, then say it is ready for pickup
        if(confirmedVolunteer != null && confirmedVolunteer.hasSameId(ParseUser.getCurrentUser())) {
            holder.tvStatus.setText("Ready for Pickup");
        }

        holder.tvNumCoats.setText(String.valueOf(pickupRequest.getNumberOfCoats()));

        holder.tvName.setText(CharityUserHelper.getFirstName(pickupRequest.getName()));
        holder.tvAddress.setText(pickupRequest.getAddresss());

        holder.btnCall.setTag(pickupRequest.getPhoneNumber());
        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String donorPhoneNum = (String) v.getTag();
                donorPhoneNum = donorPhoneNum.replaceAll("[^0-9]", "");
                String uriStr = "tel:" + donorPhoneNum;
                callIntent.setData(Uri.parse(uriStr));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Calling "+ uriStr, Toast.LENGTH_LONG).show();
                getContext().startActivity(callIntent);
            }
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
        holder.btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                String uriStr = (String) v.getTag();
                mapIntent.setData(Uri.parse(uriStr));
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Mapping "+ uriStr, Toast.LENGTH_LONG).show();
                getContext().startActivity(mapIntent);
            }
        });

        //TODO: Add Finish Pickup button
        //TODO: Add Problem with Pickup button
        //TODO: That's a lot of buttons. Perhaps a Card UI would be more suitable here. (+Swiping gestures to dismiss and ask user if the pickup was successful?) ... Point of debate.
        return v;
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
        ImageView btnCall;
        @InjectView(R.id.btnMap)
        ImageView btnMap;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}