package com.android.swipesort;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;

public class CardStackCallback extends DiffUtil.Callback {

    private List<ItemModel> old, fresh;

    public CardStackCallback(List<ItemModel> old, List<ItemModel> fresh) {
        this.old = old;
        this.fresh = fresh;
    }

    @Override
    public int getOldListSize() {
        return old.size();
    }

    @Override
    public int getNewListSize() {
        return fresh.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition).getImage() == fresh.get(newItemPosition).getImage();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition) == fresh.get(newItemPosition);
    }
}
