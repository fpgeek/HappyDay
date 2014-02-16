package com.toda.happyday.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.LruCache;
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

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by fpgeek on 2014. 1. 25..
 */
public class PictureGroupAdapter extends ArrayAdapter<PictureGroup> {

    private Activity mActivity;
    private List<PictureGroup> mPictureGroups;

    private int windowWidth = 0;
    private int windowHeight = 0;

    private static Bitmap mLoadingBitmap;
    private static ImageListLoader mImageListLoader;

    public PictureGroupAdapter(Activity activity, List<PictureGroup> pictureGroups, ImageListLoader imageListLoader) {
        super(activity, R.layout.picture_group_item, pictureGroups);
        this.mActivity = activity;

        this.mPictureGroups = pictureGroups;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;

        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);

        mImageListLoader = imageListLoader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_group_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.day_text);
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.stickerImageView = (ImageView)convertView.findViewById(R.id.sticker_thumb);
            viewHolder.dairyTextView = (TextView)convertView.findViewById(R.id.dairy_text);
            viewHolder.position = position;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PictureGroup pictureGroup = mPictureGroups.get(position);
        Picture picture = pictureGroup.getMainPicture();

        viewHolder.dayTextView.setText(picture.getDayText());
        viewHolder.stickerImageView.setImageResource(pictureGroup.getSticker());
        viewHolder.dairyTextView.setText(pictureGroup.getDairyText());

        final int imageWidth = windowWidth / 2;
        final double imageWidthRate = ((double) windowWidth / 2.0) / (double)picture.getWidth();
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
        public TextView dayTextView;
        public ImageView pictureImageView;
        public ImageView stickerImageView;
        public TextView dairyTextView;
        public int position;
    }
}
