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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyListAdapter extends ArrayAdapter<List<DailyData>> {

    private Activity context;
    private List<List<DailyData>> dailyDataGroup;

    private int windowWidth = 0;
    private int windowHeight = 0;
    private Random random = new Random();
    private Map<Integer, Integer> dailyDataIndexMap;

    private static Bitmap mLoadingBitmap;

    public DailyListAdapter(Activity context, List<List<DailyData>> dailyDataGroup) {
        super(context, R.layout.daily_item, dailyDataGroup);

        this.context = context;
        this.dailyDataGroup = dailyDataGroup;
        this.dailyDataIndexMap = makeDailyDataIndexMap(dailyDataGroup);

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;

        mLoadingBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.img_loading);
    }

    private Map<Integer, Integer> makeDailyDataIndexMap(List<List<DailyData>> dailyDataGroup) {
        Map<Integer, Integer> dailyDataIndexMap = new HashMap<Integer, Integer>(dailyDataGroup.size());
        final int dailyDataGroupSize = dailyDataGroup.size();
        for (int i=0; i<dailyDataGroupSize; i++) {
            dailyDataIndexMap.put(i, random.nextInt(dailyDataGroup.get(i).size() - 1));
        }
        return dailyDataIndexMap;
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

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        List<DailyData> dailyDataList = dailyDataGroup.get(position);
        DailyData dailyData = dailyDataList.get(dailyDataIndexMap.get(position));

        viewHolder.fullDateTextView.setText(dailyData.getFullDateText());
        viewHolder.dayTextView.setText(dailyData.getDayText());
        viewHolder.timeTextView.setText(dailyData.getTimeText());

        final int[] imageOrgSize = getBitmapSize(dailyData.getImagePath());

        final int imageWidth = windowWidth;
        final double imageWidthRate = (double) windowWidth / (double)imageOrgSize[0];
        final int imageHeight = (int)(imageWidthRate * (double)imageOrgSize[1]);

        viewHolder.pictureImageView.setImageBitmap(mLoadingBitmap);
        viewHolder.pictureImageView.getLayoutParams().width = imageWidth;
        viewHolder.pictureImageView.getLayoutParams().height = imageHeight;

        ListView listView = (ListView)parent;
        new ImageLoadTask(viewHolder.pictureImageView, listView, position, isResizing(imageOrgSize[0], imageOrgSize[1])).execute(dailyData.getImagePath());

        return convertView;
    }

    private boolean isResizing(int width, int height) {
        return (width > 1000 && height > 1000);
    }

    private static class ViewHolder {
        public TextView fullDateTextView;
        public TextView dayTextView;
        public TextView timeTextView;
        public ImageView pictureImageView;
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
