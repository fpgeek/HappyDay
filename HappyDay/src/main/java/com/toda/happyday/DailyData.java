package com.toda.happyday;

import android.graphics.Bitmap;

/**
 * Created by fpgeek on 2013. 12. 8..
 */
public class DailyData {

    private String date;
    private String location;
//    private long imageId;
    private long thumbId;
    private String imagePath;

//    public long getImageId() {
//        return imageId;
//    }
//
//    public void setImageId(long imageId) {
//        this.imageId = imageId;
//    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getThumbId() {
        return thumbId;
    }

    public void setThumbId(long thumbId) {
        this.thumbId = thumbId;
    }


    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
}
