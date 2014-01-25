package com.toda.happyday;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toda.happyday.model.PictureGroup;
import com.toda.happyday.model.PictureInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by fpgeek on 2014. 1. 25..
 */
public class ItemsAdapter extends ArrayAdapter<PictureGroup> {

    private Activity context;
    private List<PictureGroup> dailyDataGroup;

    private int windowWidth = 0;
    private int windowHeight = 0;
    private Random random = new Random();
    private Map<Integer, Integer> dailyDataIndexMap;

    private static Bitmap mLoadingBitmap;

    public ItemsAdapter(Activity context, List<PictureGroup> dailyDataGroup) {
        super(context, R.layout.daily_item, dailyDataGroup);
        this.context = context;

        this.context = context;
        this.dailyDataGroup = dailyDataGroup;
        this.dailyDataIndexMap = makeDailyDataIndexMap(dailyDataGroup);

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
            viewHolder.fullDateTextView = (TextView)convertView.findViewById(R.id.full_date_text);
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.day_text);
            viewHolder.timeTextView = (TextView)convertView.findViewById(R.id.time_text);
            viewHolder.pictureImageView = (ImageView)convertView.findViewById(R.id.picture);
            viewHolder.position = position;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PictureGroup pictureGroup = dailyDataGroup.get(position);
        PictureInfo pictureInfo = pictureGroup.get(dailyDataIndexMap.get(position));

        viewHolder.fullDateTextView.setText(pictureInfo.getFullDateText());
        viewHolder.dayTextView.setText(pictureInfo.getDayText());
        viewHolder.timeTextView.setText(pictureInfo.getTimeText());

        BitmapFactory.Options bitmapOptions = getBitmapOptions(pictureInfo.getImagePath());

        final int imageWidth = windowWidth / 2;
        final double imageWidthRate = ((double) windowWidth / 2.0) / (double)bitmapOptions.outWidth;
        final int imageHeight = (int)(imageWidthRate * (double)bitmapOptions.outHeight);

        viewHolder.pictureImageView.setImageBitmap(mLoadingBitmap);
        viewHolder.pictureImageView.getLayoutParams().width = imageWidth;
        viewHolder.pictureImageView.getLayoutParams().height = imageHeight;

        ListView listView = (ListView)parent;
//        new BitmapWorkerTask(viewHolder.pictureImageView, listView, position).execute(pictureInfo.getImagePath());
        loadBitmap(pictureInfo.getImagePath(), viewHolder.pictureImageView);

        return convertView;
    }

    private static BitmapFactory.Options getBitmapOptions( String fileName ){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);
        return options;
    }

    public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = getBitmapOptions(imagePath);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Map<Integer, Integer> makeDailyDataIndexMap(List<PictureGroup> dailyDataGroup) {
        Map<Integer, Integer> dailyDataIndexMap = new HashMap<Integer, Integer>(dailyDataGroup.size());
        final int dailyDataGroupSize = dailyDataGroup.size();
        for (int i=0; i<dailyDataGroupSize; i++) {
            final int index = selectRandomIndex(dailyDataGroup.get(i).size());
            dailyDataIndexMap.put(i, index);
        }
        return dailyDataIndexMap;
    }

    private int selectRandomIndex(int size) {
        if (size == 1) {
            return 0;
        }

        return random.nextInt(size - 1);
    }

    private static class ViewHolder {
        public TextView fullDateTextView;
        public TextView dayTextView;
        public TextView timeTextView;
        public ImageView pictureImageView;
        public int position;
    }

//    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
//
//        private ImageView mPictureView;
//        private ListView mListView;
//        private int mPosition;
//
//        public ImageLoadTask(ImageView pictureView, ListView listView, int position) {
//            mPictureView = pictureView;
//            mListView = listView;
//            mPosition = position;
//        }
//
//        @Override
//        protected Bitmap doInBackground(String... strings) {
//            final String imagePath = strings[0];
//
//            if (shouldDecodeBitmap()) {
//                return decodeSampledBitmapFromFile(imagePath, mPictureView.getLayoutParams().width, mPictureView.getLayoutParams().height);
//            } else {
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            if (result != null) {
//                if (shouldSetImageBitmap()) {
//                    mPictureView.setImageBitmap(result);
//                }
//            }
//        }
//
//        private boolean shouldDecodeBitmap() {
//            return mListView.getFirstVisiblePosition() - 1 <= mPosition && mPosition <= mListView.getLastVisiblePosition() + 1;
//        }
//
//        private boolean shouldSetImageBitmap() {
//            return mListView.getFirstVisiblePosition() <= mPosition && mPosition <= mListView.getLastVisiblePosition();
//        }
//    }

    public void loadBitmap(String imagePath, ImageView imageView) {
        if (cancelPotentialWork(imagePath, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getContext().getResources(), mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imagePath);
        }
    }

    public static boolean cancelPotentialWork(String bitmapImagePath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String imagePath = bitmapWorkerTask.imagePath;
            if (bitmapImagePath != imagePath) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        public String imagePath;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            imagePath = params[0];
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                return decodeSampledBitmapFromFile(imagePath, imageView.getLayoutParams().width, imageView.getLayoutParams().height);
            }
            return null;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
