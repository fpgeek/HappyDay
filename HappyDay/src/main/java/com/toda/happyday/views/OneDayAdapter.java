package com.toda.happyday.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.Picture;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class OneDayAdapter extends ArrayAdapter<Picture> {

    private Activity mActivity;
    private PictureGroup mPictureGroup;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;

    private static Bitmap mLoadingBitmap;
    private static ImageListLoader mImageListLoader;

    public OneDayAdapter(Activity activity, PictureGroup pictureGroup, ImageListLoader imageListLoader) {
        super(activity, R.layout.picture_group_item, pictureGroup);

        mActivity = activity;
        mPictureGroup = pictureGroup;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        mWindowWidth = metrics.widthPixels;
        mWindowHeight = metrics.heightPixels;

        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);
        mImageListLoader = imageListLoader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_item, null);

            viewHolder = new ViewHolder();
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Picture picture = mPictureGroup.get(position);

        final int imageWidth = mWindowWidth;
        final double imageWidthRate = (double) mWindowWidth / (double)picture.getWidth();
        final int imageHeight = (int)(imageWidthRate * (double)picture.getHeight());

        viewHolder.pictureImageView.getLayoutParams().width = imageWidth;
        viewHolder.pictureImageView.getLayoutParams().height = imageHeight;

        ListView listView = (ListView)parent;
        if (picture.getThumbnailBitmap() == null) {
            mImageListLoader.loadBitmap(picture.getImagePath(), mLoadingBitmap, viewHolder.pictureImageView, listView, position);
        } else {
            mImageListLoader.loadBitmap(picture.getImagePath(), picture.getThumbnailBitmap(), viewHolder.pictureImageView, listView, position);
        }

        return convertView;
    }

    private static class ViewHolder {
        public ImageView pictureImageView;
    }
}
