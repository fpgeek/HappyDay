package com.toda.happyday.model;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.db.DailyInfo;
import com.toda.happyday.db.DailyInfoDbHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class PictureGroup extends ArrayList<PictureInfo> implements Parcelable {

    private long id;
    private String dairyText = null;
    private int sticker;
    private boolean hasSticker = false;
    private boolean isFavorite;
    private String locationText = null;

    public PictureGroup() {
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
        this.hasSticker = true;
        this.sticker = sticker;
    }

    public boolean hasSticker() {
        return hasSticker;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
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

        Location location = new Location("");
        location.setLatitude(this.get(0).getLatitude());
        location.setLongitude(this.get(0).getLongitude());
        new GetAddressTask(activity, this).execute(location);
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
                TextView diaryTextView = (TextView)this.activity.findViewById(R.id.dairy_text);
                if (pictureGroup.getDairyText() != null) {
                    diaryTextView.setText(pictureGroup.getDairyText());
                }

                TextView dateTextView = (TextView)this.activity.findViewById(R.id.date_text);
                dateTextView.setText(pictureGroup.get(0).getDateText());

                ImageView stickerImage = (ImageView)this.activity.findViewById(R.id.sticker_image);
                if (pictureGroup.hasSticker()) {
                    stickerImage.setImageResource(pictureGroup.getSticker());
                }
            }
        }
    }

    protected class GetAddressTask extends AsyncTask<Location, Void, Address> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        private Activity activity;
        private PictureGroup pictureGroup;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Activity activity, PictureGroup pictureGroup) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            this.activity = activity;
            this.pictureGroup = pictureGroup;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected Address doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List <Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
            } catch (IOException exception1) {
                return null;
                // Catch incorrect latitude or longitude values
            } catch (IllegalArgumentException exception2) {
                return null;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);
                return address;

                // If there aren't any addresses, post a message
            } else {
                return null;
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(Address address) {

            if (address != null) {
                TextView locationTextView = (TextView) this.activity.findViewById(R.id.location_text);
                if (pictureGroup.getLocationText() != null) {
                    locationTextView.setText(pictureGroup.getLocationText());
                }
            }
        }
    }
}
