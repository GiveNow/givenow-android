package io.givenow.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.Donation;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;


public class DashboardItemAdapter extends RecyclerView.Adapter<DashboardItemAdapter.ViewHolder> {
    ArrayList<PickupRequest> mItems = new ArrayList<>();
    private Context mContext;

    //TODO might have to go back to parsequeryadapter to get pagination and other goodies?
    //    public DashboardItemAdapter(Context context) {
//        //items are all items where the pending volunteer = current user.
//        super(context, (QueryFactory<PickupRequest>) PickupRequest::queryMyDashboardPickups);
//
//    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_dashboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final PickupRequest pickupRequest = mItems.get(position);

        //set this as default case
        ParseUserHelper.getProfileImage().foreachDoEffect(parseFile ->
                Picasso.with(mContext).load(parseFile.getUrl()).into(vh.ivProfile));
        vh.tvStatus.setText(R.string.volunteer_dashboard_status_waiting);
        vh.readyLayout.setVisibility(View.GONE);
//        vh.tvNumCoats.setText(""); //String.valueOf(pickupRequest.getNumberOfCoats()));
//        vh.tvName.setText(ParseUserHelper.getName(pickupRequest.getDonor()).orSome(mContext.getResources().getString(R.string.donor_default_name)));
        vh.tvAddress.setText(pickupRequest.getAddress());

        String note = pickupRequest.getNote();
        vh.tvNote.setText(note);
        vh.tvNote.setVisibility(View.GONE);

        pickupRequest.getConfirmedVolunteer().foreachDoEffect(confirmedVolunteer -> {
            //if there is a confirmed volunteer and it is me, then say it is ready for pickup
            if (confirmedVolunteer.hasSameId(ParseUser.getCurrentUser())) {
                vh.tvStatus.setText(R.string.volunteer_dashboard_status_ready);
                vh.tvNote.setVisibility(note.isEmpty() ? View.GONE : View.VISIBLE);
                vh.readyLayout.setVisibility(View.VISIBLE);
                setupStaticMap(vh, pickupRequest);
                setupCardActionButtons(vh, pickupRequest);
            }
        });
    }

    private void setupStaticMap(ViewHolder vh, final PickupRequest pickupRequest) {
        //TODO: use lite mode maps instead of static maps
        ParseGeoPoint gp = pickupRequest.getLocation();
        final Double lat = gp.getLatitude();
        final Double lng = gp.getLongitude();
        final String label = pickupRequest.getAddress();
        //TODO: use our custom marker
        String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?" +
                "center=" + gp.getLatitude() + "," + gp.getLongitude() +
                "&zoom=15&size=512x512&scale=2" +
                "&markers=color:blue%7Clabel:" + //pickupRequest.getNumberOfCoats() +
                "%7C" + lat + "," + lng +
                "&maptype=roadmap&key=AIzaSyAtfxdA2mU_Jk_l6BIFRmasWp4H9jrKTuc";
        Picasso.with(mContext).load(mapUrl).into(vh.ivMapContainer);

        vh.ivMapContainer.setOnClickListener(v -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            String uriStr = "geo:<" + lat + ">,<" + lng + ">?q=" + Uri.encode(pickupRequest.getAddress());
            mapIntent.setData(Uri.parse(uriStr));
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mapIntent);
        });
    }


    private void setupCardActionButtons(ViewHolder vh, final PickupRequest pickupRequest) {
        ParseObservable.fetchIfNeeded(pickupRequest.getDonor())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                donor -> {
                    vh.btnCall.setOnClickListener(v -> {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        String donorPhoneNum = ParseUserHelper.getPhoneNumber(pickupRequest.getDonor());
                        donorPhoneNum = donorPhoneNum.replaceAll("[^0-9]", "");
                        String uriStr = "tel:" + donorPhoneNum;
                        callIntent.setData(Uri.parse(uriStr));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // Toast.makeText(getContext(), "Intent: Calling "+ uriStr, Toast.LENGTH_LONG).show();
                        mContext.startActivity(callIntent);
                    });

                    vh.btnText.setOnClickListener(v -> { //TODO send text intent
                        Intent textMsgIntent = new Intent(Intent.ACTION_VIEW);
                        String donorPhoneNum = ParseUserHelper.getPhoneNumber(pickupRequest.getDonor());
                        donorPhoneNum = donorPhoneNum.replaceAll("[^0-9]", "");
                        String uriStr = "sms:" + donorPhoneNum;
                        textMsgIntent.setData(Uri.parse(uriStr));
                        textMsgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // Toast.makeText(getContext(), "Intent: Calling "+ uriStr, Toast.LENGTH_LONG).show();
                        mContext.startActivity(textMsgIntent);
                    });
                },
                error -> ErrorDialogs.connectionFailure(mContext, error));

        // regular map intent:
        // String uriBegin = "geo:" + gp.getLatitude() + "," + gp.getLongitude();
        // navigation intent:
        String uriBegin = "google.navigation:"; // + gp.getLatitude() + "," + gp.getLongitude();
        String uriString = uriBegin + "q=" + Uri.encode(pickupRequest.getAddress());
        vh.btnMap.setOnClickListener(v -> {
            Intent navIntent = new Intent(Intent.ACTION_VIEW);
            navIntent.setData(Uri.parse(uriString));
            navIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Toast.makeText(getContext(), "Intent: Mapping "+ uriStr, Toast.LENGTH_LONG).show();
            mContext.startActivity(navIntent);
        });

        vh.btnFinishPickup.setOnClickListener(v -> {
            //TODO: change card view to donation picked up view
            // could change status to Picked Up

//            new AlertDialog.Builder(mContext).setTitle("Finish dropoff")
            // DONATION CREATION
            final Donation newDonation = new Donation(pickupRequest.getDonor(), pickupRequest.getDonationCategories());
            ParseObservable.save(newDonation).subscribe(
                    donation -> {
                        //send push to donor
                        pickupRequest.generatePickupCompleteNotif(mContext);
                        //create donation, and set it in the PickupRequest
                        pickupRequest.setDonation(newDonation);
                        // loadObjects();
                        ParseObservable.save(pickupRequest)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        r -> {
                                            remove(vh.getAdapterPosition());
                                        },
                                        error -> ErrorDialogs.connectionFailure(mContext, error));
                    },
                    error -> ErrorDialogs.connectionFailure(mContext, error));
        });
        //TODO: Make Report Problem do something
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(Collection<PickupRequest> items) {
        Log.i("DashboardItemAdapter", items.size() + " dashboard items received.");
        if (items.equals(mItems)) { //TODO still doesnt work, prob due to pr.getDonor being different
            Log.i("DashboardItemAdapter", "New list is the same as the current list.");
            return;
        }
        // Attempt to fix crash:
        // java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position 3(offset:5).state:4
        // https://code.google.com/p/android/issues/detail?can=1&q=77846&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars&id=77846
//        mItems.clear();
//        notifyItemRangeRemoved(0, items.size()); <--- lol i was using items here, not mItems. probably the cause
        clearItems();
        fj.data.List.list(items)
                .foreachDoEffect(this::addItem);
        Log.i("DashboardItemAdapter", items.size() + " dashboard items added.");
    }

    //    public void addOrUpdateItem(PickupRequest item) {
//        int itemIdx = mItems.indexOf(item);
//
//        if (itemIdx != -1) {
//            mItems.set(itemIdx, item);
//            notifyItemChanged(itemIdx);
//        } else {
//            addItem(item);
//        }
//    }
//
    public void addItem(PickupRequest item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public void removeItem(PickupRequest item) {
        remove(mItems.indexOf(item));
    }

    public void remove(int index) {
        mItems.remove(index);
        notifyItemRemoved(index);
    }

    public void clearItems() {
        mItems.clear();
//        notifyDataSetChanged();
        notifyItemRangeRemoved(0, mItems.size());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivProfile)
        ImageView ivProfile;
        @Bind(R.id.tvStatus)
        TextView tvStatus;
        @Bind(R.id.btnMenu)
        ImageButton btnMenu;
        //        @Bind(R.id.tvName)
//        TextView tvName;
        @Bind(R.id.tvAddress)
        TextView tvAddress;
        @Bind(R.id.tvNote)
        TextView tvNote;
        @Bind(R.id.readyLayout)
        LinearLayout readyLayout;
        @Bind(R.id.map_container)
        ImageView ivMapContainer;
        @Bind(R.id.btnCall)
        Button btnCall;
        @Bind(R.id.btnText)
        Button btnText;
        @Bind(R.id.btnMap)
        Button btnMap;
        //        @Bind(R.id.btnProblem)
//        Button btnProblem;
        @Bind(R.id.btnFinishPickup)
        Button btnFinishPickup;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}