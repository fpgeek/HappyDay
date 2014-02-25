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
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.async.BitmapWorkerTask;
import com.toda.happyday.async.PictureGroupBitmapWorkerTask;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.TextViewUtil;

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

//    private static Bitmap mLoadingBitmap;
    private static Bitmap mLoadingBitmap = null;
    private static ImageListLoader mImageListLoader;

    public PictureGroupAdapter(Activity activity, List<PictureGroup> pictureGroups, ImageListLoader imageListLoader) {
        super(activity, R.layout.picture_group_item, pictureGroups);
        this.mActivity = activity;

        this.mPictureGroups = pictureGroups;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;

//        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);

        mImageListLoader = imageListLoader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.picture_group_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.day_text);
            viewHolder.monthTextView = (TextView)convertView.findViewById(R.id.month_text);
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.stickerImageView = (ImageView)convertView.findViewById(R.id.sticker_thumb);
            viewHolder.dairyTextView = (TextView)convertView.findViewById(R.id.dairy_text);
            viewHolder.dateTextView = (TextView)convertView.findViewById(R.id.date_text);
            viewHolder.position = position;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PictureGroup pictureGroup = mPictureGroups.get(position);
        Picture picture = pictureGroup.getMainPicture();

        TextViewUtil.setText(viewHolder.dayTextView, picture.getDayText());
        TextViewUtil.setText(viewHolder.monthTextView, picture.getMonthText());
        viewHolder.stickerImageView.setImageResource(pictureGroup.getSticker());
        TextViewUtil.setText(viewHolder.dairyTextView, pictureGroup.getDairyText());
        TextViewUtil.setText(viewHolder.dateTextView, picture.getDateText());

        final int imageViewWidth = windowWidth / 2;

        {
            double ratio = 1;
            if (picture.getDegrees() == 0) {
                ratio = (double)picture.getHeight() / (double)picture.getWidth();
            } else if (picture.getDegrees() == 90) {
                ratio = (double)picture.getWidth() / (double)picture.getHeight();
            }
            viewHolder.pictureImageView.getLayoutParams().width = imageViewWidth;
            viewHolder.pictureImageView.getLayoutParams().height = (int)(ratio * (double)imageViewWidth);
        }

        ListView listView = (ListView)parent;

        BitmapWorkerTask bitmapWorkerTask = new PictureGroupBitmapWorkerTask(mActivity.getContentResolver(), picture, viewHolder.pictureImageView, listView, position);

        if (picture.getThumbnailBitmap() == null) {
            mImageListLoader.loadBitmap(picture, mLoadingBitmap, viewHolder.pictureImageView, bitmapWorkerTask);
        } else {
            mImageListLoader.loadBitmap(picture, picture.getThumbnailBitmap(), viewHolder.pictureImageView, bitmapWorkerTask);
        }


        return convertView;
    }

    private static class ViewHolder {
        public TextView dayTextView;
        public TextView monthTextView;
        public ImageView pictureImageView;
        public ImageView stickerImageView;
        public TextView dairyTextView;
        public int position;
        public TextView dateTextView;
    }
}
