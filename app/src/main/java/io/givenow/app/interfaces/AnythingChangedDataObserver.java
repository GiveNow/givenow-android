package io.givenow.app.interfaces;

import android.support.v7.widget.RecyclerView;

/**
 * Created by aphex on 2/2/16.
 */
public abstract class AnythingChangedDataObserver extends RecyclerView.AdapterDataObserver {
    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        super.onItemRangeRemoved(positionStart, itemCount);
        onAnythingChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        super.onItemRangeChanged(positionStart, itemCount);
        onAnythingChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        super.onItemRangeChanged(positionStart, itemCount, payload);
        onAnythingChanged();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        onAnythingChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        onAnythingChanged();
    }

    @Override
    public void onChanged() {
        super.onChanged();
        onAnythingChanged();
    }

    public abstract void onAnythingChanged();
}

