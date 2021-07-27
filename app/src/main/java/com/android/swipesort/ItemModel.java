package com.android.swipesort;

import android.net.Uri;

public class ItemModel {
    private Uri image;
    private int imgRes = 0;


    public ItemModel() {
    }

    public ItemModel(Uri image) {
        this.image = image;
    }

    public ItemModel(int imgRes) {
        this.imgRes = imgRes;
    }

    public Uri getImage() {
        return image;
    }

    public int getImgRes() {
        return imgRes;
    }
}
