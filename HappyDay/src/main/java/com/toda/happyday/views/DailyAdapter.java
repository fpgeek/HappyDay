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

import java.util.List;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyAdapter extends ArrayAdapter<PictureGroup> {

    private Activity context;
    private List<PictureGroup> dailyDataGroup;

    private int windowWidth = 0;
    private int windowHeight = 0;

    private static Bitmap mLoadingBitmap;

    public DailyAdapter(Activity context, List<PictureGroup> dailyDataGroup) {
        super(context, R.layout.daily_item, dailyDataGroup);

        this.context = context;
        this.dailyDataGroup = dailyDataGroup;
//        this.dailyDataIndexMap = makeDailyDataIndexMap(dailyDataGroup);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;

        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.daily_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.day_text);
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.position = position;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PictureGroup pictureGroup = dailyDataGroup.get(position);
        Picture picture =  pictureGroup.getMainPicture();

        viewHolder.dayTextView.setText(picture.getDayText());

        final int[] imageOrgSize = getBitmapSize(picture.getImagePath());

        final int imageWidth = windowWidth;
        final double imageWidthRate = (double) windowWidth / (double)imageOrgSize[0];
        final int imageHeight = (int)(imageWidthRate * (double)imageOrgSize[1]);

        viewHolder.pictureImageView.setImageBitmap(mLoadingBitmap);
        viewHolder.pictureImageView.getLayoutParams().width = imageWidth;
        viewHolder.pictureImageView.getLayoutParams().height = imageHeight;

        ListView listView = (ListView)parent;
        new ImageLoadTask(viewHolder.pictureImageView, listView, position, isResizing(imageOrgSize[0], imageOrgSize[1])).execute(picture.getImagePath());

        return convertView;
    }

    private boolean isResizing(int width, int height) {
        return (width > 1000 && height > 1000);
    }

    private static class ViewHolder {
        public TextView dayTextView;
        public ImageView pictureImageView;
        public int position;
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

            if (shouldDecodeBitmap()) {
                return BitmapFactory.decodeFile(imagePath, opt);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                if (shouldSetImageBitmap()) {
                    mPictureView.setImageBitmap(result);
                }
            }
        }

        private boolean shouldDecodeBitmap() {
            return mListView.getFirstVisiblePosition() - 1 <= mPosition && mPosition <= mListView.getLastVisiblePosition() + 1;
        }

        private boolean shouldSetImageBitmap() {
            return mListView.getFirstVisiblePosition() <= mPosition && mPosition <= mListView.getLastVisiblePosition();
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
