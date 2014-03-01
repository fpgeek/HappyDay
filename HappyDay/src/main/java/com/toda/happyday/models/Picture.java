package com.toda.happyday.models;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.toda.happyday.R;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.utils.BitmapUtils;

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
    private String location = null;
    private String imagePath;
    private Bitmap thumbnailBitmap = null; // 다른 Activity에 전달할 때는 의도적으로 제외했음.
    private int width;
    private int height;
    private int mDegrees;

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
    private final static String DB_DATE_ORDER = MediaStore.Images.Media.DATE_TAKEN + " ASC";

    private final static String MONTH_ENG_LIST[] = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public Picture() {
    }

    public Picture(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static void all(Context context, AsyncPostExecute<List<Picture>> asyncPostExecute) {
        new GetAllPicturesTask(context, asyncPostExecute).execute();
    }

    public static void updateLocation(Context context, long pictureId, String location) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_picture_to_location_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(pictureId), location);
        editor.commit();
    }

    private static String getLocationFromDb(Context context, long pictureId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_picture_to_location_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString(String.valueOf(pictureId), null);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getMonthText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return MONTH_ENG_LIST[calendar.get(Calendar.MONTH)];
    }

    public String getLocation() {
        return location;
    }

    public boolean hasValidLocationInfo() {
        return longitude != 0 && latitude != 0;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private static class GetAllPicturesTask extends AsyncTask<Void, Void, List<Picture>> {

        private Context mContext;

        private AsyncPostExecute<List<Picture>> mAsyncPostExecute;

        public GetAllPicturesTask(Context context, AsyncPostExecute<List<Picture>> asyncPostExecute) {
            mContext = context;
            mAsyncPostExecute = asyncPostExecute;
        }

        @Override
        protected List<Picture> doInBackground(Void... voids) {
            Cursor pictureCursor = getAllPictureCursor(mContext.getContentResolver());

            List<Picture> pictureList = new ArrayList<Picture>(pictureCursor.getCount());
            while(pictureCursor.moveToNext()) {
                Picture picture = createPictureInfo(mContext, pictureCursor);
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

//    public static Picture get(ContentResolver contentResolver, final long id) {
//        Cursor pictureCursor = getPictureCursor(contentResolver, id);
//        if (pictureCursor == null) { return null; }
//
//        return createPictureInfo(pictureCursor, contentResolver);
//    }

//    private static Cursor getPictureCursor(ContentResolver contentResolver, final long id) {
//        String[] selectionArgs = {String.valueOf(id)};
//        return getCursor(contentResolver, MediaStore.Images.Media._ID + " = ?", selectionArgs, DB_DATE_ORDER);
//    }

    private static Cursor getAllPictureCursor(ContentResolver contentResolver) {
        return getCursor(contentResolver, null, null, DB_DATE_ORDER);
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

    private static Picture createPictureInfo(Context context, Cursor pictureCursor) {
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

        final String location = getLocationFromDb(context, picture.getId());
        if (location != null) {
            picture.setLocation(location);
        }

        final int orientation = pictureCursor.getInt(pictureCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        picture.setDegrees(orientation);

        BitmapFactory.Options bitmapOptions = BitmapUtils.getBitmapOptions(picture.getImagePath());
        picture.setWidth(bitmapOptions.outWidth);
        picture.setHeight(bitmapOptions.outHeight);

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
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    public String getDayText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getTimeText() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
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

    public int getDegrees() {
        return mDegrees;
    }

    public void setDegrees(int degrees) {
        this.mDegrees = degrees;
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
        parcel.writeLong(id);
        parcel.writeLong(date.getTime());
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(location);
        parcel.writeString(imagePath);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeInt(mDegrees);
//        parcel.writeParcelable(thumbnailBitmap, i);
    }

    private void readFromParcel(Parcel parcel) {
        this.id = parcel.readLong();
        this.date = new Date(parcel.readLong());
        this.longitude = parcel.readDouble();
        this.latitude = parcel.readDouble();
        this.location = parcel.readString();
        this.imagePath = parcel.readString();
        this.width = parcel.readInt();
        this.height = parcel.readInt();
        this.mDegrees = parcel.readInt();
//        this.thumbnailBitmap = parcel.readParcelable(getClass().getClassLoader());
    }


}
