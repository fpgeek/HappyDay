package com.toda.happyday.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

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

    private static class PictureDateCompare implements Comparator<PictureGroup> {

        @Override
        public int compare(PictureGroup pictureGroup, PictureGroup pictureGroup2) {
            return pictureGroup2.getMainPicture().getDate().compareTo(pictureGroup.getMainPicture().getDate());
        }
    };

    private static class AppStartExecute implements AsyncPostExecute<Bitmap> {

        private PictureGroupActivity mPictureGroupActivity;
        private List<PictureGroup> mPictureGroupList;

        public AppStartExecute(PictureGroupActivity pictureGroupActivity, List<PictureGroup> pictureGroupList) {
            mPictureGroupActivity = pictureGroupActivity;
            mPictureGroupList = pictureGroupList;
        }

        @Override
        public void onPostExecute(Bitmap t) {
            mPictureGroupActivity.setPictureGroups(mPictureGroupList);
        }
    }

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

            for (PictureGroup pictureGroup : pictureGroupList) {
                pictureGroup.selectMainPicture();
            }

            Collections.sort(pictureGroupList, new PictureDateCompare());

//            int pictureCount=0;
//            for (PictureGroup pictureGroup : pictureGroupList) {
//                if (pictureCount == 6) {
//                    AppStartExecute appStartExecuteHandler = new AppStartExecute(mPictureGroupActivity, pictureGroupList);
//                    new CreateThumbnailBitmapTask(mPictureGroupActivity.getContentResolver(), pictureGroup.getMainPicture(), appStartExecuteHandler).execute();
//                } else {
//                    new CreateThumbnailBitmapTask(mPictureGroupActivity.getContentResolver(), pictureGroup.getMainPicture(), null).execute();
//                }
//                ++pictureCount;
//            }

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
