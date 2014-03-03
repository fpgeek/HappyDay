package com.toda.happyday.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.internal.ac;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.views.PictureGroupActivity;
import com.toda.happyday.R;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.db.DailyInfoDbHelper;
import com.toda.happyday.views.PictureGroupAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fpgeek on 2014. 2. 13..
 */
public class PictureGroupPresenter {

    private static Activity mActivity = null;
    private List<Picture> mPictureList = new ArrayList<Picture>();
    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간
    private final static int LIMIT_COUNT_PER_LOAD = 200;

    public long getLastLoadDateValue() {
        return mLastLoadDateValue;
    }

    public void setLastLoadDateValue(long lastLoadDateValue) {
        this.mLastLoadDateValue = lastLoadDateValue;
    }

    private long mLastLoadDateValue;
    private boolean mInitLoad = false;

    public boolean isPictureEmpty() {
        return mPictureEmpty;
    }

    private boolean mPictureEmpty = false;
    private PictureGroupAdapter mAdapter;

    public PictureGroupPresenter(Activity activity) {
        mActivity = activity;
        mLastLoadDateValue = new Date().getTime();
    }

    private AsyncPostExecute<List<Picture>> mOnPostGetPictureList = new AsyncPostExecute<List<Picture>>() {

        @Override
        public void onPostExecute(List<Picture> newPictureList) {
            if (newPictureList.isEmpty()) {
                mPictureEmpty = true;
                return;
            }

            Collections.sort(newPictureList, new PictureCompare());
            mPictureList = newPictureList;
            DailyInfoDbHelper dbHelper = new DailyInfoDbHelper(mActivity);
            PictureGroup.all(dbHelper, mOnPostGetPictureGroupList);
        }
    };

    private static class PictureCompare implements Comparator<Picture> {

        @Override
        public int compare(Picture picture, Picture picture2) {
            return picture2.getDate().compareTo(picture.getDate());
        }
    };

    private static class PictureGroupCompare implements Comparator<PictureGroup> {

        @Override
        public int compare(PictureGroup pictureGroup, PictureGroup pictureGroup2) {
            return pictureGroup2.getMainPicture().getDate().compareTo(pictureGroup.getMainPicture().getDate());
        }
    };

    private AsyncPostExecute<List<PictureGroup>> mOnPostGetPictureGroupList = new AsyncPostExecute<List<PictureGroup>>() {
        @Override
        public void onPostExecute(List<PictureGroup> pictureGroupList) {

            Map<Long, PictureGroup> pictureGroupHashMap = pictureGroupListToMap(pictureGroupList);
            List<List<Picture>> pictureGroupListGroupByTimes = pictureListToListGroupByTime(mPictureList);

            SharedPreferences sharedPreferences = mActivity.getSharedPreferences(mActivity.getString(R.string.preference_picture_info_key), Context.MODE_PRIVATE);

            List<PictureGroup> newCreatedPictureGroups = new ArrayList<PictureGroup>();
            for (List<Picture> picturesGroupByTime : pictureGroupListGroupByTimes) {
                final long pictureGroupId = getPictureGroupId(sharedPreferences, picturesGroupByTime);
                if (pictureGroupId == -1) {
                    DailyInfoDbHelper dbHelper = new DailyInfoDbHelper(mActivity);
                    PictureGroup pictureGroup = PictureGroup.create(dbHelper);
                    newCreatedPictureGroups.add(pictureGroup);
                    pictureGroup.addAll(picturesGroupByTime);
                    pictureGroupList.add(pictureGroup);
                } else {
                    PictureGroup pictureGroup = pictureGroupHashMap.get(pictureGroupId);
                    if (pictureGroup != null) {
                        pictureGroup.addAll(picturesGroupByTime);
                    } else {
                        assert false;
                    }
                }
            }

            insertPictureGroupToPictureInfo(sharedPreferences, newCreatedPictureGroups);
            removeEmptyPictureGroups(pictureGroupList);

            Collections.sort(pictureGroupList, new PictureGroupCompare());

            if (!pictureGroupList.isEmpty()) {
                PictureGroup lastPictureGroup = pictureGroupList.get(pictureGroupList.size() - 1);
                mLastLoadDateValue = lastPictureGroup.get(0).getDate().getTime();
                pictureGroupList.remove(pictureGroupList.size() - 1);

                if (mInitLoad) {
                    Intent intent = new Intent(mActivity, PictureGroupActivity.class);
                    intent.putParcelableArrayListExtra(mActivity.getString(R.string.EXTRA_PICTURE_GROUP_LIST), new ArrayList<PictureGroup>(pictureGroupList));
                    intent.putExtra(mActivity.getString(R.string.EXTRA_LAST_LOAD_DATE_TIME), mLastLoadDateValue);
                    mActivity.startActivity(intent);
                } else {
                    mAdapter.addAll(pictureGroupList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public void initLoadPictureGroups() {
        mInitLoad = true;
        Picture.getList(mActivity, mOnPostGetPictureList, mLastLoadDateValue, LIMIT_COUNT_PER_LOAD);
    }

    public void continueLoadPictureGroups(PictureGroupAdapter adapter) {
        mInitLoad = false;
        mAdapter = adapter;
        Picture.getList(mActivity, mOnPostGetPictureList, mLastLoadDateValue, LIMIT_COUNT_PER_LOAD);
    }

    private void insertPictureGroupToPictureInfo(SharedPreferences sharedPreferences, List<PictureGroup> newCreatedPictureGroups) {

        if (newCreatedPictureGroups.isEmpty()) { return; }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (PictureGroup pictureGroup : newCreatedPictureGroups) {
            for (Picture picture : pictureGroup) {
                editor.putLong(String.valueOf(picture.getId()), pictureGroup.getId());
            }
        }
        editor.commit();
    }

    private void removeEmptyPictureGroups(List<PictureGroup> pictureGroupList) {

        List<PictureGroup> emptyPictureGroup = new ArrayList<PictureGroup>();
        for (PictureGroup pictureGroup : pictureGroupList) {
            if (pictureGroup.isEmpty()) {
                emptyPictureGroup.add(pictureGroup);
            } else {
                pictureGroup.selectMainPicture();
            }
        }

        for (PictureGroup pictureGroup : emptyPictureGroup) {
            pictureGroupList.remove(pictureGroup);
            DailyInfoDbHelper dbHelper = new DailyInfoDbHelper(mActivity);
            PictureGroup.remove(dbHelper, pictureGroup.getId());
        }
    }

    private List<List<Picture>> pictureListToListGroupByTime(List<Picture> pictureList) {
        List<List<Picture>> pictureGroupList = new ArrayList<List<Picture>>();
        List<Picture> pictureGroup = new ArrayList<Picture>();

        long prevPictTakenTime = 0;
        for (Picture picture : pictureList) {
            if (prevPictTakenTime == 0) {
                pictureGroup.add(picture);
                prevPictTakenTime = picture.getDate().getTime();
                continue;
            }

            final long takenTime = picture.getDate().getTime();
            if ( (prevPictTakenTime - takenTime) <= TAKEN_DATE_DIFF_MS ) {
                pictureGroup.add(picture);
            } else {
                pictureGroupList.add(pictureGroup);
                pictureGroup = new ArrayList<Picture>();
                pictureGroup.add(picture);
            }

            prevPictTakenTime = takenTime;
        }

        if (!pictureGroupList.contains(pictureGroup)) {
            pictureGroupList.add(pictureGroup);
        }
        return pictureGroupList;
    }

    private Map<Long, PictureGroup> pictureGroupListToMap(List<PictureGroup> pictureGroupList) {
        Map<Long, PictureGroup> hashMap = new HashMap<Long, PictureGroup>();
        for (PictureGroup pictureGroup : pictureGroupList) {
            hashMap.put(pictureGroup.getId(), pictureGroup);
        }
        return hashMap;
    }

    private long getPictureGroupId(SharedPreferences sharedPreferences, List<Picture> pictureList) {
        for (Picture picture : pictureList) {
            long value = sharedPreferences.getLong(String.valueOf(picture.getId()), -1);
            if (value > -1) {
                return value;
            }
        }
        return -1;
    }
}
