package com.toda.happyday.async;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ListView;

import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 2. 25..
 */
public class PictureGroupBitmapWorkerTask extends BitmapWorkerTask {

    private ContentResolver mContentResolver;

    public PictureGroupBitmapWorkerTask(ContentResolver contentResolver, Picture picture, ImageView imageView, ListView listView, int position) {
        super(picture, imageView, listView, position);
        mContentResolver = contentResolver;
    }

    @Override
    public Bitmap createBitmap(ImageView imageView) {
        return MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
    }
}
