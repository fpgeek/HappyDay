package com.toda.happyday.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.views.PictureGroupActivity;
import com.toda.happyday.R;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.db.DailyInfoDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fpgeek on 2014. 2. 13..
 */
public class PictureGroupPresenter {

    private PictureGroupActivity mPictureGroupActivity;
    private DailyInfoDbHelper mDbHelper;
    private List<Picture> mPictureList;
    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간

    public PictureGroupPresenter(PictureGroupActivity pictureGroupActivity) {
        mPictureGroupActivity = pictureGroupActivity;
        mDbHelper = new DailyInfoDbHelper(mPictureGroupActivity);
    }

    private AsyncPostExecute<List<Picture>> mOnPostGetPictureList = new AsyncPostExecute<List<Picture>>() {
        @Override
        public void onPostExecute(List<Picture> pictureList) {
            mPictureList = pictureList;
            PictureGroup.all(mDbHelper, mOnPostGetPictureGroupList);
        }
    };

    private AsyncPostExecute<List<PictureGroup>> mOnPostGetPictureGroupList = new AsyncPostExecute<List<PictureGroup>>() {
        @Override
        public void onPostExecute(List<PictureGroup> pictureGroupList) {

            Map<Long, PictureGroup> pictureGroupHashMap = pictureGroupListToMap(pictureGroupList);
            List<List<Picture>> pictureGroupListGroupByTimes = pictureListToListGroupByTime(mPictureList);

            SharedPreferences sharedPreferences = mPictureGroupActivity.getSharedPreferences(mPictureGroupActivity.getString(R.string.preference_picture_info_key), Context.MODE_PRIVATE);

            for (List<Picture> picturesGroupByTime : pictureGroupListGroupByTimes) {
                final long pictureGroupId = getPictureGroupId(sharedPreferences, picturesGroupByTime);
                if (pictureGroupId == -1) {
                    PictureGroup pictureGroup = newCreatePictureGroup(sharedPreferences, picturesGroupByTime);
                    pictureGroup.addAll(picturesGroupByTime);
                    pictureGroupList.add(pictureGroup);
                } else {
                    PictureGroup pictureGroup = pictureGroupHashMap.get(pictureGroupId);
                    pictureGroup.addAll(picturesGroupByTime);
                }
            }

            mPictureGroupActivity.setPictureGroups(pictureGroupList);
        }
    };


    public void loadPictureGroups() {
        Picture.all(mPictureGroupActivity.getContentResolver(), mOnPostGetPictureList);
    }

    private PictureGroup newCreatePictureGroup(SharedPreferences sharedPreferences, List<Picture> picturesGroupByTime) {
        PictureGroup pictureGroup = PictureGroup.create(mDbHelper);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Picture picture : picturesGroupByTime) {
            editor.putLong(String.valueOf(picture.getId()), pictureGroup.getId());
        }
        editor.commit();
        return pictureGroup;
    }

    private List<List<Picture>> pictureListToListGroupByTime(List<Picture> pictureList) {

        List<Picture> pictureGroup = new ArrayList<Picture>();
        List<List<Picture>> pictureGroupList = new ArrayList<List<Picture>>();

        long prevTakenDateValue = 0;
        for(Picture picture : pictureList) {

            final long takenTime = picture.getDate().getTime();
            if (prevTakenDateValue == 0) {
                prevTakenDateValue = takenTime;
            }

            if ((prevTakenDateValue - takenTime) <= TAKEN_DATE_DIFF_MS) {
                prevTakenDateValue = takenTime;
                pictureGroup.add(picture);
            } else {
                pictureGroupList.add(pictureGroup);
                pictureGroup = new PictureGroup();
                pictureGroup.add(picture);
                prevTakenDateValue = 0;
            }
        }
        return pictureGroupList;
    }

    private Map<Long, PictureGroup> pictureGroupListToMap(List<PictureGroup> pictureGroupList) {
        Map<Long, PictureGroup> hashMap = new HashMap<Long, PictureGroup>(pictureGroupList.size());
        for (PictureGroup pictureGroup : pictureGroupList) {
            hashMap.put(pictureGroup.getId(), pictureGroup);
        }
        return hashMap;
    }

    private long getPictureGroupId(SharedPreferences sharedPreferences, List<Picture> pictureList) {
        for (Picture picture : pictureList) {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.remove(String.valueOf(pictureInfo.getId()));
//                editor.commit();
            long value = sharedPreferences.getLong(String.valueOf(picture.getId()), -1);
            if (value > -1) {
                return value;
            }
        }
        return -1;
    }
}
