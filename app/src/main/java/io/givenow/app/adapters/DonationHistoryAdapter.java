package io.givenow.app.adapters;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.givenow.app.R;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.models.Donation;
import io.givenow.app.models.DonationCategory;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import rx.parse.ParseObservable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {
    ArrayList<Donation> mItems = new ArrayList<>();
    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_donation_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final Donation donation = mItems.get(position);

        ParseUserHelper.getProfileImage().foreachDoEffect(parseFile ->
                Picasso.with(mContext).load(parseFile.getUrl()).into(vh.ivProfile));

        Date d = donation.getCreatedAt();
        //TODO use SimpleDateFormat.getDateInstance(style) to get localized formatting
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        String dateStr = format.format(d);
        vh.tvDonationDate.setText(dateStr);

        vh.mCategoriesAdapter.clearItems();
        //TODO: messy and inefficient to run a query on every ViewHolder bind!
        ParseObservable.first(PickupRequest.Companion.queryPickupRequestForDonation(donation)).observeOn(mainThread()).subscribe(
                pickupRequest -> {
                    vh.tvAddress.setText(pickupRequest.getAddress());

                    for (DonationCategory item : pickupRequest.getDonationCategories()) {
                        item.setSelected(true);
                        item.setClickable(false);
                        vh.mCategoriesAdapter.addItem(item);
                    }
                },
                error -> {
                    ErrorDialogs.connectionFailure(mContext, error);
                }
        );

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(Collection<Donation> items) {
        Log.i("DashboardItemAdapter", items.size() + " dashboard items received.");
        if (items.equals(mItems)) { //TODO still doesnt work, prob due to pr.getDonor being different
            Log.i("DashboardItemAdapter", "New list is the same as the current list.");
            return;
        }
        mItems.clear();
        notifyItemRangeRemoved(0, items.size());
        fj.data.List.list(items).foreachDoEffect(this::addItem);
        Log.i("DashboardItemAdapter", items.size() + " dashboard items added.");
    }

    public void addItem(Donation item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public void removeItem(Donation item) {
        remove(mItems.indexOf(item));
    }

    public void remove(int index) {
        mItems.remove(index);
        notifyItemRemoved(index);
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final DonationCategoryAdapter mCategoriesAdapter;
        @BindView(R.id.ivProfile)
        ImageView ivProfile;
        @BindView(R.id.tvDonationDate)
        TextView tvDonationDate;
        @BindView(R.id.tvAddress)
        TextView tvAddress;
        @BindView(R.id.rvCategories)
        RecyclerView rvCategories;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            Context context = itemView.getContext();

            mCategoriesAdapter = new DonationCategoryAdapter();
            mCategoriesAdapter.setCardWidth(context.getResources().getDimensionPixelSize(R.dimen.card_horizontal_column_width));
            rvCategories.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setItemAnimator(new SlideInRightAnimator(new DecelerateInterpolator()));
            rvCategories.setAdapter(mCategoriesAdapter);
        }
    }
}
