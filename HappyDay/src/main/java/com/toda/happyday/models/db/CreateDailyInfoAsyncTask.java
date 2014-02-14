package com.toda.happyday.models.db;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.models.Picture;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class CreateDailyInfoAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private DailyInfoDbHelper dbHelper;
    private PictureGroup pictureGroup;
    private SharedPreferences sharedPreferences;

    public CreateDailyInfoAsyncTask(DailyInfoDbHelper dbHelper, PictureGroup pictureGroup, SharedPreferences sharedPreferences) {
        this.dbHelper = dbHelper;
        this.pictureGroup = pictureGroup;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_DIARY_TEXT, "");
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_STICKER, 0);
        values.put(DailyInfo.DailyEntry.COLUMN_NAME_FAVORITE, 0);

        assert db != null;
        long newRowId = db.insert(
                DailyInfo.DailyEntry.TABLE_NAME,
                DailyInfo.DailyEntry.COLUMN_NAME_NULLABLE,
                values
        );
        pictureGroup.setId(newRowId);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Picture picture : pictureGroup) {
            editor.putLong(String.valueOf(picture.getId()), pictureGroup.getId());
        }
        editor.commit();

        return true;
    }


}
