package com.toda.happyday;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyListAdapter extends ArrayAdapter<DailyData> {

    private Activity context;
    private List<DailyData> dailyDatas;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;

    private static Bitmap mLoadingBitmap;

    public DailyListAdapter(Activity context, List<DailyData> dailyDatas) {
        super(context, R.layout.daily_item, dailyDatas);

        this.context = context;
        this.dailyDatas = dailyDatas;

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        mWindowWidth = metrics.widthPixels;
        mWindowHeight = metrics.heightPixels;

        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.daily_item, null);

            viewHolder = new ViewHolder();
            viewHolder.mDateTextView = (TextView)convertView.findViewById(R.id.date_text);
            viewHolder.mLocationTextView = (TextView)convertView.findViewById(R.id.location_text);
            viewHolder.mPictureImageView = (ImageView)convertView.findViewById(R.id.picture);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        DailyData dailyData = dailyDatas.get(position);
        viewHolder.mDateTextView.setText(dailyData.getDate());
        viewHolder.mLocationTextView.setText(dailyData.getLocation());

        final int[] imageOrgSize = getBitmapSize(dailyData.getImagePath());

        final int imageWidth = mWindowWidth;
        final double imageWidthRate = (double)mWindowWidth / (double)imageOrgSize[0];
        final int imageHeight = (int)(imageWidthRate * (double)imageOrgSize[1]);

//        Log.i("HappyDay", "imageWidthRate : " + imageWidthRate);
//        Log.i("HappyDay", "image imageOrgSize[0] : " + imageOrgSize[0]);
//        Log.i("HappyDay", "image imageOrgSize[1] : " + imageOrgSize[1]);
//        Log.i("HappyDay", "image imageWidth : " + imageWidth);
//        Log.i("HappyDay", "image imageHeight : " + imageHeight);

        viewHolder.mPictureImageView.setImageBitmap(mLoadingBitmap);
        viewHolder.mPictureImageView.getLayoutParams().width = imageWidth;
        viewHolder.mPictureImageView.getLayoutParams().height = imageHeight;

        ListView listView = (ListView)parent;
        new ImageLoadTask(viewHolder.mPictureImageView, listView, position, isResizing(imageOrgSize[0], imageOrgSize[1])).execute(dailyData.getImagePath());

        return convertView;
    }

    private boolean isResizing(int width, int height) {
        return (width > 1000 && height > 1000);
    }

    private static class ViewHolder {

        public TextView mDateTextView;
        public TextView mLocationTextView;
        public ImageView mPictureImageView;
    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mPictureView;
        private ListView mListView;
        private int mPosition;
        private boolean mIsResizing;

        public ImageLoadTask(ImageView pictureView, ListView listView, int position, boolean isResizing) {
            mPictureView = pictureView;
            mListView = listView;
            mPosition = position;
            mIsResizing = isResizing;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            final String imagePath = strings[0];

            BitmapFactory.Options opt = new BitmapFactory.Options();
            if (mIsResizing) {
                opt.inSampleSize = 4;
            }
            opt.inJustDecodeBounds = false;

            if (mListView.getFirstVisiblePosition() <= mPosition && mPosition <= mListView.getLastVisiblePosition()) {
                return BitmapFactory.decodeFile(imagePath, opt);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                if (mListView.getFirstVisiblePosition() <= mPosition && mPosition <= mListView.getLastVisiblePosition()) {
                    mPictureView.setImageBitmap(result);
                }
            } else {
                Log.i("DailyListAdapter", "bitmap == null");
            }
        }
    }

    public int[] getBitmapSize( String fileName ){
        int[] size = {0,0};

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;

    }
}
