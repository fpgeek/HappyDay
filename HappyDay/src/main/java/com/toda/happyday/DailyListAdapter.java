package com.toda.happyday;

import android.app.Activity;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyListAdapter extends ArrayAdapter<DailyData> {

    private Activity context;
    private List<DailyData> dailyDatas;

    public DailyListAdapter(Activity context, List<DailyData> dailyDatas) {
        super(context, R.layout.daily_item, dailyDatas);

        this.context = context;
        this.dailyDatas = dailyDatas;
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
//        Bitmap thumbBitmap = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), dailyData.getImageId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
        viewHolder.mPictureImageView.setImageBitmap(dailyData.getThumbBitmap());
//        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, dailyData.getImageId());
//        viewHolder.mPictureImageView.setImageURI(imageUri);

        return convertView;
    }

    private static class ViewHolder {

        public TextView mDateTextView;
        public TextView mLocationTextView;
        public ImageView mPictureImageView;
    }
}
