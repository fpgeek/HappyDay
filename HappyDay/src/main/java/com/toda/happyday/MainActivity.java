package com.toda.happyday;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static Bitmap loadingBitmap = null;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Cursor imagesCursor = getImagesCursor();

            ImageScrollView imageScroll = (ImageScrollView)rootView.findViewById(R.id.image_scroll);
            imageScroll.setLoadingBitmap(getLoadingBitmap());

            List<Uri> uriList = new ArrayList<Uri>(imagesCursor.getCount());
            LinearLayout imageContainer = (LinearLayout)rootView.findViewById(R.id.image_container);
            while(imagesCursor.moveToNext()) {
                long id = imagesCursor.getLong(imagesCursor.getColumnIndex(MediaStore.Images.Media._ID));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                uriList.add(imageUri);
                int takenDate = imagesCursor.getInt(imagesCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

                View itemView = inflater.inflate(R.layout.daily_item, container, false);
                TextView dateTextView = (TextView)itemView.findViewById(R.id.date_text);
                dateTextView.setText(new Date(takenDate).toString());

                TextView locationTextView = (TextView)itemView.findViewById(R.id.location_text);
                locationTextView.setText("location");

                ImageView pictureView = (ImageView)itemView.findViewById(R.id.picture);
                pictureView.setImageBitmap(getLoadingBitmap());

                imageContainer.addView(itemView);
            }
            imagesCursor.close();

            return rootView;
        }

        private Bitmap getLoadingBitmap() {
            if (loadingBitmap == null) {
                loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_loading);
            }
            return loadingBitmap;
        }

        private Cursor getImagesCursor() {
            final Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            final String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.IS_PRIVATE
            };
            final String selection = "";
            String[] selectionArgs = null;
            final String sortOreder = "";

            return getActivity().getContentResolver().query(
                    imagesUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOreder
            );
        }
    }

}
