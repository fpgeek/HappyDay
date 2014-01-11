package com.toda.happyday;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Cursor imagesCursor = getImagesCursor();

            List<List<DailyData>> dailyDataGroup = new ArrayList<List<DailyData>>(imagesCursor.getCount());
            List<DailyData> dailyDataList = null;
            long prevTakenDateValue = 0;
            while(imagesCursor.moveToNext()) {
                DailyData dailyData = new DailyData();

                final long takenDateValue = imagesCursor.getLong(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                Date takenDate = new Date(takenDateValue);
                dailyData.setDate(takenDate);

                final long takenTime = takenDate.getTime();

                String imagePath = imagesCursor.getString(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                dailyData.setImagePath(imagePath);

                final double longitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
                dailyData.setLongitude(longitudeValue);

                final double latitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                dailyData.setLatitude(latitudeValue);

                if (dailyDataList == null) { dailyDataList = new ArrayList<DailyData>(); }
                dailyDataList.add(dailyData);

                if ( !(prevTakenDateValue > 0 && ((takenTime - prevTakenDateValue) <= TAKEN_DATE_DIFF_MS)) ) {
                    dailyDataGroup.add(dailyDataList);
                    dailyDataList = null;
                }

                prevTakenDateValue = takenDateValue;

            }
            dailyDataGroup.add(dailyDataList);
            imagesCursor.close();

            DailyListAdapter listAdapter = new DailyListAdapter(getActivity(), dailyDataGroup);
            setListAdapter(listAdapter);

            return rootView;
        }

        private Cursor getImagesCursor() {
            final Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            final String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.IS_PRIVATE
            };
            final String selection = "";
            String[] selectionArgs = null;
            final String sortOreder = MediaStore.Images.Media.DATE_TAKEN;

            return getActivity().getContentResolver().query(
                    imagesUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOreder
            );
        }
    }

}
