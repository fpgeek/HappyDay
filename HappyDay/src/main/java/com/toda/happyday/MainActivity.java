package com.toda.happyday;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.toda.happyday.db.CreateDailyInfoAsyncTask;
import com.toda.happyday.db.DailyInfoDbHelper;
import com.toda.happyday.model.PictureGroup;
import com.toda.happyday.model.PictureInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간
    public static final String EXTRA_PICTURE_GROUP = "DailyDataArray";

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

        private List<PictureGroup> pictureGroups;
        private DailyInfoDbHelper dbHelper;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Cursor imagesCursor = getImagesCursor();

            dbHelper = new DailyInfoDbHelper(getActivity());
            pictureGroups = new ArrayList<PictureGroup>(imagesCursor.getCount());
            PictureGroup pictureInfoList = new PictureGroup();
            long prevTakenDateValue = 0;
            while(imagesCursor.moveToNext()) {
                PictureInfo pictureInfo = createPictureInfo(imagesCursor);

                final long takenTime = pictureInfo.getDate().getTime();
                if (prevTakenDateValue == 0) {
                    prevTakenDateValue = takenTime;
                }

                if ((prevTakenDateValue - takenTime) <= TAKEN_DATE_DIFF_MS) {
                    prevTakenDateValue = takenTime;
                    pictureInfoList.add(pictureInfo);
                } else {
                    pictureGroups.add(pictureInfoList);
                    pictureInfoList = new PictureGroup();
                    pictureInfoList.add(pictureInfo);
                    prevTakenDateValue = 0;
                }
            }
            imagesCursor.close();

            craetePictureGroupIds();

            DailyListAdapter listAdapter = new DailyListAdapter(getActivity(), pictureGroups);
            setListAdapter(listAdapter);

            return rootView;
        }

        private void craetePictureGroupIds() {
            for (PictureGroup pictureGroup : pictureGroups) {
                createPictureGroupId(pictureGroup);
            }
        }

        private void createPictureGroupId(PictureGroup pictureGroup) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.preference_picture_info_key), Context.MODE_PRIVATE);
            long pictureGroupId = getPictureGroupId(sharedPreferences, pictureGroup);
            if (pictureGroupId == -1) {
                new CreateDailyInfoAsyncTask(dbHelper, pictureGroup, sharedPreferences).execute();
            } else {
                pictureGroup.setId(pictureGroupId);
            }
        }

        private long getPictureGroupId(SharedPreferences sharedPreferences, PictureGroup pictureGroup) {
            for (PictureInfo pictureInfo : pictureGroup) {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.remove(String.valueOf(pictureInfo.getId()));
//                editor.commit();
                long value = sharedPreferences.getLong(String.valueOf(pictureInfo.getId()), -1);
                if (value > -1) {
                    return value;
                }
            }
            return -1;
        }

        private PictureInfo createPictureInfo(Cursor imagesCursor) {
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.setId(imagesCursor.getInt(imagesCursor.getColumnIndex(MediaStore.Images.Media._ID)));

            final long takenDateValue = imagesCursor.getLong(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
            pictureInfo.setDate(new Date(takenDateValue));

            String imagePath = imagesCursor.getString(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            pictureInfo.setImagePath(imagePath);

            final double longitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            pictureInfo.setLongitude(longitudeValue);

            final double latitudeValue = imagesCursor.getDouble(imagesCursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
            pictureInfo.setLatitude(latitudeValue);

            return pictureInfo;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            Intent intent = new Intent(getActivity(), DailyActivity.class);

            PictureGroup pictureGroup = pictureGroups.get(position);
            intent.putExtra(EXTRA_PICTURE_GROUP, (Parcelable)pictureGroup);
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
