package com.toda.happyday.async;

import android.widget.ImageView;
import android.widget.ListView;
import android.graphics.Bitmap;

import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.BitmapUtils;

/**
 * Created by fpgeek on 2014. 2. 25..
 */
public class OneDayBitmapWorkerTask extends BitmapWorkerTask {

    public OneDayBitmapWorkerTask(Picture picture, ImageView imageView, int position) {
        super(picture, imageView, position);
    }

    @Override
    public Bitmap createBitmap(ImageView imageView) {
        if (shouldDecodeBitmap()) {
            return BitmapUtils.decodeSampledBitmapFromFile(imagePath, mPicture.getDegrees(), imageView.getLayoutParams().width / 2, imageView.getLayoutParams().height / 2);
        }
        return null;
    }

    private boolean shouldDecodeBitmap() {
        return true;
//        return listView.getFirstVisiblePosition() - 2 <= position && position <= listView.getLastVisiblePosition() + 2;
    }
}
