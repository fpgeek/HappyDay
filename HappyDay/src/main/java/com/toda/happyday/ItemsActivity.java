package com.toda.happyday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.toda.happyday.db.CreateDailyInfoAsyncTask;
import com.toda.happyday.db.DailyInfoDbHelper;
import com.toda.happyday.model.PictureGroup;
import com.toda.happyday.model.PictureInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemsActivity extends Activity {

    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간

    private List<PictureGroup> pictureGroups;

    private ListView onTouchListView;
    private ListView listViewLeft;
    private ListView listViewRight;
    private ItemsAdapter leftAdapter;
    private ItemsAdapter rightAdapter;

    int[] leftViewsHeights;
    int[] rightViewsHeights;

    private DailyInfoDbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list);

        listViewLeft = (ListView) findViewById(R.id.list_view_left);
        listViewRight = (ListView) findViewById(R.id.list_view_right);

        loadItems();

        listViewLeft.setOnTouchListener(touchListener);
        listViewRight.setOnTouchListener(touchListener);
        listViewLeft.setOnScrollListener(scrollListener);
        listViewRight.setOnScrollListener(scrollListener);
        listViewLeft.setOnItemClickListener(itemClickListener);
        listViewRight.setOnItemClickListener(itemClickListener);
    }

    // Passing the touch event to the opposite list
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        boolean dispatched = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.equals(listViewLeft) && !dispatched) {
                dispatched = true;
                listViewRight.dispatchTouchEvent(event);
                onTouchListView = listViewLeft;
            } else if (v.equals(listViewRight) && !dispatched) {
                dispatched = true;
                listViewLeft.dispatchTouchEvent(event);
                onTouchListView = listViewRight;
            }

            dispatched = false;
            return false;
        }
    };

    /**
     * Synchronizing scrolling
     * Distance from the top of the first visible element opposite list:
     * sum_heights(opposite invisible screens) - sum_heights(invisible screens) + distance from top of the first visible child
     */
    AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView v, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (view.getChildAt(0) != null) {
                if (view.equals(listViewLeft) ){
                    leftViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < listViewRight.getFirstVisiblePosition(); i++) {
                        h += rightViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < listViewLeft.getFirstVisiblePosition(); i++) {
                        hi += leftViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    listViewRight.setSelectionFromTop(listViewRight.getFirstVisiblePosition(), top);
                } else if (view.equals(listViewRight)) {
                    rightViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < listViewLeft.getFirstVisiblePosition(); i++) {
                        h += leftViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < listViewRight.getFirstVisiblePosition(); i++) {
                        hi += rightViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    listViewLeft.setSelectionFromTop(listViewLeft.getFirstVisiblePosition(), top);
                }

            }

        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            if (adapterView == onTouchListView) {
                PictureGroup pictureGroup = null;
                if (adapterView == listViewLeft) {
                    pictureGroup = pictureGroups.get(i*2);
                } else if (adapterView == listViewRight) {
                    pictureGroup = pictureGroups.get(i*2 + 1);
                }

                if (pictureGroup != null) {
                    Intent intent = new Intent(view.getContext(), DailyActivity.class);
                    intent.putExtra(getString(R.string.extra_daily_data_array), (Parcelable)pictureGroup);
                    startActivity(intent);
                }
            }
        }
    };

    private void loadItems(){

        Cursor imagesCursor = getImagesCursor();

        dbHelper = new DailyInfoDbHelper(this);
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

        craetePictureGroupIds(pictureGroups);
        List<PictureGroup> leftPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);
        List<PictureGroup> rightPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);

        dividePictureGroups(pictureGroups, leftPictureGroups, rightPictureGroups);

        leftAdapter = new ItemsAdapter(this, leftPictureGroups);
        rightAdapter = new ItemsAdapter(this, rightPictureGroups);
        listViewLeft.setAdapter(leftAdapter);
        listViewRight.setAdapter(rightAdapter);

        leftViewsHeights = new int[leftPictureGroups.size()];
        rightViewsHeights = new int[rightPictureGroups.size()];
    }

    private void dividePictureGroups(List<PictureGroup> pictureGroups, List<PictureGroup> leftPictureGroups, List<PictureGroup> rightPictureGroups) {
        final int size = pictureGroups.size();
        for (int i=0; i<size; i++) {
            if (i % 2 == 0) {
                leftPictureGroups.add(pictureGroups.get(i));
            } else {
                rightPictureGroups.add(pictureGroups.get(i));
            }
        }
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

        return getContentResolver().query(
                imagesUri,
                projection,
                selection,
                selectionArgs,
                sortOreder
        );
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

        Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), pictureInfo.getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
        pictureInfo.setThumbnailBitmap(thumbnailBitmap);

        return pictureInfo;
    }

    private void craetePictureGroupIds(List<PictureGroup> pictureGroups) {
        for (PictureGroup pictureGroup : pictureGroups) {
            createPictureGroupId(pictureGroup);
        }
    }

    private void createPictureGroupId(PictureGroup pictureGroup) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_picture_info_key), Context.MODE_PRIVATE);
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

}
