package com.toda.happyday;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간
    public static final String EXTRA_DAILY_DATA_ARRAY = "DailyDataArray";

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

        private List<List<DailyData>> dailyDataGroup;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Cursor imagesCursor = getImagesCursor();

            dailyDataGroup = new ArrayList<List<DailyData>>(imagesCursor.getCount());
            List<DailyData> dailyDataList = new ArrayList<DailyData>();;
            long prevTakenDateValue = 0;
            while(imagesCursor.moveToNext()) {
                DailyData dailyData = createDailyData(imagesCursor);

                final long takenTime = dailyData.getDate().getTime();
                if (prevTakenDateValue == 0) {
                    prevTakenDateValue = takenTime;
                }

                if ((prevTakenDateValue - takenTime) <= TAKEN_DATE_DIFF_MS) {
                    prevTakenDateValue = takenTime;
                    dailyDataList.add(dailyData);
                } else {
                    dailyDataGroup.add(dailyDataList);
                    dailyDataList = new ArrayList<DailyData>();
                    dailyDataList.add(dailyData);
                    prevTakenDateValue = 0;
                }
            }

            imagesCursor.close();

            DailyListAdapter listAdapter = new DailyListAdapter(getActivity(), dailyDataGroup);
            setListAdapter(listAdapter);

            return rootView;
        }

        private DailyData createDailyData(Cursor imagesCursor) {
            DailyData dailyData = new DailyData();

            final long takenDateValue = imagesCursor.getLong(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
            dailyData.setDate(new Date(takenDateValue));

            String imagePath = imagesCursor.getString(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            dailyData.setImagePath(imagePath);

            final double longitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            dailyData.setLongitude(longitudeValue);

            final double latitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
            dailyData.setLatitude(latitudeValue);

            return dailyData;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            Intent intent = new Intent(getActivity(), DailyActivity.class);

            List<DailyData> dailyDataList = dailyDataGroup.get(position);
            DailyData[] dailyDataArray = new DailyData[dailyDataList.size()];
            dailyDataList.toArray(dailyDataArray);
            intent.putExtra(EXTRA_DAILY_DATA_ARRAY, dailyDataArray);
            startActivity(intent);
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
            final String sortOreder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

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
