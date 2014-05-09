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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DashboardItemAdapter extends ParseQueryAdapter {

    public DashboardItemAdapter(Context context) {
        //items are all items where the pending volunteer = current user.
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery<ParseObject> query = new ParseQuery("PickupRequest");
                query.whereEqualTo("pendingVolunteer", ParseUser.getCurrentUser());
                return query;
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

        //TODO: Add logic to choose the appropriate status to display.
        holder.tvStatus.setText("Waiting for donor to confirm");
        holder.tvStatus.setText("Ready for Pickup");

        holder.tvNumCoats.setText(String.valueOf(object.getNumber("numberOfCoats")));

        holder.tvName.setText(object.getString("name"));
        holder.tvAddress.setText(object.getString("address"));

        holder.btnCall.setTag(object.getString("phoneNumber"));
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
        String query = object.getString("address");
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