package com.toda.happyday.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.views.PictureGroupActivity;
import com.toda.happyday.R;
import com.toda.happyday.models.Picture;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.db.DailyInfoDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fpgeek on 2014. 2. 13..
 */
public class PictureGroupPresenter {

    private Activity mActivity;
    private DailyInfoDbHelper mDbHelper;
    private List<Picture> mPictureList;
    private final static long TAKEN_DATE_DIFF_MS = 1000 * 60 * 60; // 사진이 묶이는 시간 차이 - 1시간

    public PictureGroupPresenter(Activity activity) {
        mActivity = activity;
        mDbHelper = new DailyInfoDbHelper(mActivity);
    }

    private AsyncPostExecute<List<Picture>> mOnPostGetPictureList = new AsyncPostExecute<List<Picture>>() {
        @Override
        public void onPostExecute(List<Picture> pictureList) {
            mPictureList = pictureList;
            Collections.sort(mPictureList, new PictureCompare());
            PictureGroup.all(mDbHelper, mOnPostGetPictureGroupList);
        }
    };

    private static class PictureCompare implements Comparator<Picture> {

        @Override
        public int compare(Picture picture, Picture picture2) {
            return picture.getDate().compareTo(picture2.getDate());
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

            // TODO - 삭제된 이미지들을 가지고 있던 picture group 제거하기
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
            }

            Collections.sort(pictureGroupList, new PictureGroupCompare());

            Intent intent = new Intent(mActivity, PictureGroupActivity.class);
            intent.putParcelableArrayListExtra(mActivity.getString(R.string.EXTRA_PICTURE_GROUP_LIST), new ArrayList<PictureGroup>(pictureGroupList));
            mActivity.startActivity(intent);

//            mPictureGroupActivity.setPictureGroups(pictureGroupList);
        }
    };


    public void loadPictureGroups() {
        Picture.all(mActivity, mOnPostGetPictureList);
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
            if ( (takenTime - prevPictTakenTime) <= TAKEN_DATE_DIFF_MS ) {
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
