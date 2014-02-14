package com.toda.happyday.models;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.toda.happyday.async.AsyncPostExecute;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class Picture implements Parcelable {

    private long id;
    private Date date;
    private double longitude;
    private double latitude;
    private String imagePath;
    private Bitmap thumbnailBitmap;

    private final static Uri DB_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final static String[] DB_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.IS_PRIVATE
    };
    private final static String DB_DATE_ORDER_DESC = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    public Picture() {
    }

    public Picture(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static void all(ContentResolver contentResolver, AsyncPostExecute<List<Picture>> asyncPostExecute) {
        new GetAllPicturesTask(contentResolver, asyncPostExecute).execute();
    }

    private static class GetAllPicturesTask extends AsyncTask<Void, Void, List<Picture>> {

        private ContentResolver mContentResolver;
        private AsyncPostExecute<List<Picture>> mAsyncPostExecute;

        public GetAllPicturesTask(ContentResolver contentResolver, AsyncPostExecute<List<Picture>> asyncPostExecute) {
            mContentResolver = contentResolver;
            mAsyncPostExecute = asyncPostExecute;
        }

        @Override
        protected List<Picture> doInBackground(Void... voids) {
            Cursor pictureCursor = getAllPictureCursor(mContentResolver);

            List<Picture> pictureList = new ArrayList<Picture>(pictureCursor.getCount());
            while(pictureCursor.moveToNext()) {
                Picture picture = createPictureInfo(pictureCursor, mContentResolver);
                pictureList.add(picture);
            }
            pictureCursor.close();

            return pictureList;
        }

        @Override
        protected void onPostExecute(List<Picture> pictureList) {
            mAsyncPostExecute.onPostExecute(pictureList);
        }
    }

    public static Picture get(ContentResolver contentResolver, final long id) {
        Cursor pictureCursor = getPictureCursor(contentResolver, id);
        if (pictureCursor == null) { return null; }

        return createPictureInfo(pictureCursor, contentResolver);
    }

    private static Cursor getPictureCursor(ContentResolver contentResolver, final long id) {
        String[] selectionArgs = {String.valueOf(id)};
        return getCursor(contentResolver, MediaStore.Images.Media._ID + " = ?", selectionArgs, DB_DATE_ORDER_DESC);
    }

    private static Cursor getAllPictureCursor(ContentResolver contentResolver) {
        return getCursor(contentResolver, null, null, DB_DATE_ORDER_DESC);
    }

    private static Cursor getCursor(ContentResolver contentResolver, final String selection, final String[] selectionArgs, final String sortOrder) {
        return contentResolver.query(
                DB_IMAGE_URI,
                DB_PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    private static Picture createPictureInfo(Cursor pictureCursor, ContentResolver contentResolver) {
        Picture picture = new Picture();
        picture.setId(pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Images.Media._ID)));

        final long takenDateValue = pictureCursor.getLong(pictureCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
        picture.setDate(new Date(takenDateValue));

        String imagePath = pictureCursor.getString(pictureCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        picture.setImagePath(imagePath);

        final double longitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
        picture.setLongitude(longitudeValue);

        final double latitudeValue = pictureCursor.getDouble(pictureCursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
        picture.setLatitude(latitudeValue);

        Bitmap thumbnailBitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, picture.getId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
        picture.setThumbnailBitmap(thumbnailBitmap);

        return picture;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getFullDateText() {
        return DateFormat.getDateInstance(DateFormat.FULL).format(date);
    }

    public String getDateText() {
        return DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(date);
    }

    public String getDayText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getTimeText() {
        return DateFormat.getTimeInstance().format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Picture> CREATOR
            = new Parcelable.Creator<Picture>() {
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(date.getTime());
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(imagePath);
        parcel.writeParcelable(thumbnailBitmap, i);
    }

    private void readFromParcel(Parcel parcel) {
        this.date = new Date(parcel.readLong());
        this.longitude = parcel.readDouble();
        this.latitude = parcel.readDouble();
        this.imagePath = parcel.readString();
        this.thumbnailBitmap = parcel.readParcelable(Bitmap.class.getClassLoader());
    }


}
