package com.toda.happyday.model;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.db.DailyInfo;
import com.toda.happyday.db.DailyInfoDbHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class PictureGroup extends ArrayList<PictureInfo> implements Parcelable {

    private long id;
    private String dairyText;
    private int sticker;
    private boolean isFavorite;

    public PictureGroup() {
    }

    public PictureGroup(String dairyText, int sticker, boolean isFavorite) {
        this.dairyText = dairyText;
        this.sticker = sticker;
        this.isFavorite = isFavorite;
    }

    public PictureGroup(Parcel parcel) {
        readFromParcel(parcel);
    }

    public PictureGroup(int size) {
        super(size);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDairyText() {
        return dairyText;
    }

    public void setDairyText(String dairyText) {
        this.dairyText = dairyText;
    }

    public int getSticker() {
        return sticker;
    }

    public void setSticker(int sticker) {
        this.sticker = sticker;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PictureGroup> CREATOR
            = new Parcelable.Creator<PictureGroup>() {
        public PictureGroup createFromParcel(Parcel in) {
            return new PictureGroup(in);
        }

        public PictureGroup[] newArray(int size) {
            return new PictureGroup[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeList(this);
    }

    private void readFromParcel(Parcel parcel) {
        this.id = parcel.readLong();
        parcel.readList(this, getClass().getClassLoader());
    }

    public void loadFromDb(Activity activity) {
        new GetDairyTask(activity, this).execute(this.id);
    }

    private class GetDairyTask extends AsyncTask<Long, Void, PictureGroup> {

        private Activity activity;
        private DailyInfoDbHelper dbHelper = null;
        private PictureGroup pictureGroup;

        public GetDairyTask(Activity activity, PictureGroup pictureGroup) {
            this.activity = activity;
            this.dbHelper = new DailyInfoDbHelper(activity);
            this.pictureGroup = pictureGroup;
        }

        @Override
        protected PictureGroup doInBackground(Long... longs) {
            long rowId = longs[0];
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] projection = {
                    DailyInfo.DailyEntry._ID,
                    DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT,
                    DailyInfo.DailyEntry.COLUMN_NAME_STICKER,
                    DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE
            };

            String selection = DailyInfo.DailyEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(rowId) };

            String sortOrder = DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE + " DESC";

            assert db != null;
            Cursor cursor = db.query(
                    DailyInfo.DailyEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );

            while(cursor.moveToNext()) {
                String diaryText = cursor.getString(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT));
                pictureGroup.setDairyText(diaryText);
                int sticker = cursor.getInt(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_STICKER));
                pictureGroup.setSticker(sticker);
                int favorite = cursor.getInt(cursor.getColumnIndex(DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE));
                pictureGroup.setFavorite(favorite == 1);
            }
            cursor.close();
            return pictureGroup;
        }

        @Override
        protected void onPostExecute(PictureGroup pictureGroup) {
            super.onPostExecute(pictureGroup);

            if (pictureGroup != null) {
                TextView diaryText = (TextView)this.activity.findViewById(R.id.dairy_text);
                diaryText.setText(pictureGroup.getDairyText());
            }
        }
    }
}
