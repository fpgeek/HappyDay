package com.toda.happyday;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyData implements Parcelable {

    private Date date;
    private double longitude;
    private double latitude;
    private String imagePath;

    public DailyData() {

    }

    public DailyData(Parcel parcel) {
        readFromParcel(parcel);
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

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DailyData> CREATOR
            = new Parcelable.Creator<DailyData>() {
        public DailyData createFromParcel(Parcel in) {
            return new DailyData(in);
        }

        public DailyData[] newArray(int size) {
            return new DailyData[size];
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
