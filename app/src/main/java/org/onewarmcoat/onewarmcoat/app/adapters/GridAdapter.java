package org.onewarmcoat.onewarmcoat.app.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.DonationCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private final DonationCategory mDummyCategory;
    ArrayList<DonationCategory> mItems;
    private Context mContext;

    public GridAdapter() {
        super();
        mDummyCategory = new DonationCategory();
        mItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            mItems.add(mDummyCategory);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_category, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        DonationCategory donationCategory = mItems.get(i);
        if (donationCategory.equals(mDummyCategory)) {
            return;
        }
        if (viewHolder.tvName != null && viewHolder.tvDescription != null) {
            if (Locale.getDefault() == Locale.GERMAN) { //TODO locale doesnt work correctly
                viewHolder.tvName.setText(donationCategory.getNameDE());
                viewHolder.tvDescription.setText(donationCategory.getDescriptionDE());
            } else {
                viewHolder.tvName.setText(donationCategory.getNameEN());
                viewHolder.tvDescription.setText(donationCategory.getDescriptionEN());
            }
        }

        Picasso.with(mContext).load(donationCategory.getImage().getUrl()).into(viewHolder.imageView, new Callback() {
            @Override
            public void onSuccess() {
                viewHolder.progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                // set a default or 'error' image
                // imageView.setImageDrawable(R.id.);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItem(DonationCategory item) {
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);
    }

    public void setItems(Collection<DonationCategory> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void setItemSelected(int position, boolean selected) {
        mItems.get(position).setSelected(selected);
    }

    public ArrayList<DonationCategory> getSelectedItems() {
        ArrayList<DonationCategory> selectedItems = new ArrayList<>();
        for (DonationCategory item : mItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image)
        public ImageView imageView;

        @Bind(R.id.progressBar)
        public ProgressBar progressBar;

        @Bind(R.id.tvName)
        public TextView tvName;

        @Bind(R.id.tvDescription)
        public TextView tvDescription;

        @Bind(R.id.vPalette)
        View vPalette;

        @BindColor(R.color.white)
        int white;

        @BindColor(R.color.colorPrimary)
        int colorPrimary;
        @BindDimen(R.dimen.card_elevation)
        int card_elevation;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.cvRoot)
        public void onCardClick(CardView cv) {
            int pos = getAdapterPosition();
            DonationCategory donationCategory = mItems.get(pos);

            if (donationCategory.isSelected()) {
                donationCategory.setSelected(false);
                tvName.setTextColor(colorPrimary);
                tvName.setTypeface(null, Typeface.NORMAL);
                cv.setElevation(card_elevation);
                cv.setSelected(false);
            } else {
                donationCategory.setSelected(true);
                tvName.setTextColor(white);
                tvName.setTypeface(null, Typeface.BOLD);
                cv.setElevation(1);
                cv.setSelected(true);
//                vPalette.setBackgroundResource(R.color.colorPrimary);
//            cv.setStateListAnimator(p);
            }
        }
    }
}