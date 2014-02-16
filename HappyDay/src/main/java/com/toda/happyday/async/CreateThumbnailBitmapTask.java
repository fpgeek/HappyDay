package com.toda.happyday.async;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 2. 16..
 */
public class CreateThumbnailBitmapTask extends AsyncTask<Void, Void, Bitmap> {

    private ContentResolver mContentResolver;
    private Picture mPicture;

    public CreateThumbnailBitmapTask(ContentResolver contentResolver, Picture picture) {
        mContentResolver = contentResolver;
        mPicture = picture;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mPicture.getId(), MediaStore.Images.Thumbnails.MINI_KIND, options);
        return thumbnailBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mPicture.setThumbnailBitmap(bitmap);
    }
}
