package com.toda.happyday;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fpgeek on 2013. 12. 16..
 */
public class ImageScrollView extends ScrollView {

    private Set<Integer> mOnShowIndexSet = new HashSet<Integer>();
    private static Bitmap loadingBitmap = null;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private static Bitmap mLoadingBitmap;
    private static Bitmap mUserBitmap;

    public ImageScrollView(Context context) {
        super(context);
        init();
    }

    public ImageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        mWindowWidth = metrics.widthPixels;
        mWindowHeight = metrics.heightPixels;

        mUserBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apink_img);

        Boolean se = isSmoothScrollingEnabled();
        Log.i("ImageScrollView", "se : " + se);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        Log.i("ImageScrollView", "top : " + t);
        Log.i("ImageScrollView", "oldTop : " + oldt);
        if (((Math.abs(oldt - t)) > 5)) {
            return;
        }

        final int b = (t + mWindowHeight);

        LinearLayout imageContainer = (LinearLayout)this.getChildAt(0);
        final int childCount = imageContainer.getChildCount();

        int[] location = {0, 0};
        for(int i=0; i<childCount; i++) {
            final View childView = imageContainer.getChildAt(i);


            childView.getLocationInWindow(location);
            final int viewYPosition = location[1];

            if ( (viewYPosition + 200 <= b) && (0 < viewYPosition) ) {
                if (mOnShowIndexSet.size() > 5) {
                    continue;
                }
                if (!mOnShowIndexSet.contains(i)) {
                    mOnShowIndexSet.add(i);
                    onShowView(childView, i);
                }
            } else {
                if (mOnShowIndexSet.contains(i)) {
                    mOnShowIndexSet.remove(i);
                    onHiddenView(childView, i);
                } else if (viewYPosition > b) {
//                    break;
                }
            }
        }
    }

    protected void onShowView(View view, final int index) {
        ImageView pictureView = (ImageView)view.findViewById(R.id.picture);
        new ImageLoadTask(pictureView).execute("");
        Log.i("ImageScrollView", "onShowView : " + index);
    }

    protected void onHiddenView(View view, final int index) {
        ImageView pictureView = (ImageView)view.findViewById(R.id.picture);
        new ImageFreeTask(pictureView).execute("");
        Log.i("ImageScrollView", "onHiddenView : " + index);
    }

    public void setLoadingBitmap(Bitmap loadingBitmap) {
        mLoadingBitmap = loadingBitmap;
    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mPictureView;

        public ImageLoadTask(ImageView pictureView) {
            mPictureView = pictureView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.apink_img);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mPictureView.setImageBitmap(bitmap);
        }
    }

    private class ImageFreeTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mPictureView;

        ImageFreeTask(ImageView pictureView) {
            mPictureView = pictureView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return ((BitmapDrawable)mPictureView.getDrawable()).getBitmap();
//            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mPictureView.setImageBitmap(mLoadingBitmap);
        }
    }
}


