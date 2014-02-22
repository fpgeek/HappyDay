package com.toda.happyday.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.BitmapUtils;

import java.lang.ref.WeakReference;

/**
 * Created by fpgeek on 2014. 2. 15..
 */
public class ImageListLoader {

    private LruCache<String, Bitmap> memoryCache;
    private Context mContext;

    public ImageListLoader(Context context) {
        mContext = context;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 16;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void loadBitmap(Picture picture, Bitmap thumbnailImage, ImageView imageView, ListView listView, int position) {
        boolean isCancelWork = cancelPotentialWork(picture.getImagePath(), imageView);

        final Bitmap bitmap = getBitmapFromMemCache(picture.getImagePath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (isCancelWork) {
                final BitmapWorkerTask task = new BitmapWorkerTask(picture, imageView, listView, position);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(mContext.getResources(), thumbnailImage, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(picture.getImagePath());
            }
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private static boolean cancelPotentialWork(String bitmapImagePath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String imagePath = bitmapWorkerTask.imagePath;

            if (!bitmapImagePath.equals(imagePath)) {
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
        private Picture mPicture;
        private String imagePath;
        private ListView listView;
        private int position;

        public BitmapWorkerTask(Picture picture, ImageView imageView, ListView listView, int position) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.mPicture = picture;
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.listView = listView;
            this.position = position;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            imagePath = params[0];
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (shouldDecodeBitmap()) {
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(imagePath, mPicture.getDegrees(), imageView.getLayoutParams().width / 2, imageView.getLayoutParams().height / 2);
                    addBitmapToMemoryCache(imagePath, bitmap);
                    return bitmap;
                }
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

        private boolean shouldDecodeBitmap() {
            return listView.getFirstVisiblePosition() - 2 <= position && position <= listView.getLastVisiblePosition() + 2;
        }

        private boolean shouldSetImageBitmap() {
            return listView.getFirstVisiblePosition() <= position && position <= listView.getLastVisiblePosition();
        }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
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
