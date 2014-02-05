package com.toda.happyday.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class PictureInfo implements Parcelable {

    private long id;
    private Date date;
    private double longitude;
    private double latitude;
    private String imagePath;

    private Bitmap thumbnailBitmap;

    public PictureInfo() {

    }

    public PictureInfo(Parcel parcel) {
        readFromParcel(parcel);
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

    public static final Parcelable.Creator<PictureInfo> CREATOR
            = new Parcelable.Creator<PictureInfo>() {
        public PictureInfo createFromParcel(Parcel in) {
            return new PictureInfo(in);
        }

        public PictureInfo[] newArray(int size) {
            return new PictureInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(date.getTime());
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(imagePath);
    }

    private void readFromParcel(Parcel parcel) {
        this.date = new Date(parcel.readLong());
        this.longitude = parcel.readDouble();
        this.latitude = parcel.readDouble();
        this.imagePath = parcel.readString();
    }
}
