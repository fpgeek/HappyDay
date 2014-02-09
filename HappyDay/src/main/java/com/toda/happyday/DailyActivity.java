package com.toda.happyday;

import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.toda.happyday.db.DailyInfo;
import com.toda.happyday.db.DailyInfoDbHelper;
import com.toda.happyday.model.PictureGroup;
import com.toda.happyday.model.PictureInfo;

import java.util.ArrayList;
import java.util.List;

public class DailyActivity extends FragmentActivity {

    private static PlaceholderFragment placeholderFragment = null;
    private static Integer[] STICKER_THUMB_IDS = {
            R.drawable.sticker_1, R.drawable.sticker_2,
            R.drawable.sticker_3, R.drawable.sticker_4,
            R.drawable.sticker_5, R.drawable.sticker_6,
            R.drawable.sticker_7, R.drawable.sticker_8,
            R.drawable.sticker_9, R.drawable.sticker_10,
            R.drawable.sticker_11, R.drawable.sticker_12,
            R.drawable.sticker_13
    };
    private static final int STICKER_COUNT_PER_SCREEN = 10;
    private DailyInfoDbHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        dbHelper = new DailyInfoDbHelper(this);

        if (savedInstanceState == null) {
            placeholderFragment = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.daily, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_sticker:
                openStickerView();
                return true;
            case R.id.action_edit:
                openEditView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openStickerView() {
        final int visibility = this.placeholderFragment.getStickerViewPager().getVisibility();
        if (visibility == View.GONE) {
            this.placeholderFragment.getStickerViewPager().setVisibility(View.VISIBLE);
        } else {
            this.placeholderFragment.getStickerViewPager().setVisibility(View.GONE);
        }
    }

    private void openEditView() {
        Intent intent = new Intent(this, WriteActivity.class);
        intent.putExtra(getString(R.string.EXTRA_DAILY_GROUP_ID), this.placeholderFragment.getPictureGroup().getId());
        intent.putExtra(getString(R.string.EXTRA_DAIRY_TEXT), this.placeholderFragment.getPictureGroup().getDairyText());
        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends ListFragment {

        private PictureGroup pictureGroup;
        private View headerView;
        private List<PictureGroup> pictureGroupList;

        private ViewPager stickerViewPager = null;
        private StickerCollectionPagerAdapter stickerCollectionPagerAdapter = null;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_daily, container, false);

            Parcelable parcelable = getActivity().getIntent().getParcelableExtra( getString(R.string.extra_daily_data_array) );
            pictureGroup = (PictureGroup)parcelable;

            pictureGroup.loadFromDb(getActivity());

            pictureGroupList = new ArrayList<PictureGroup>(pictureGroup.size());
            for (PictureInfo pictureInfo : pictureGroup) {
                PictureGroup pictureInfoList = new PictureGroup(1);
                pictureInfoList.add(pictureInfo);
                pictureGroupList.add(pictureInfoList);
            }

            headerView = inflater.inflate(R.layout.diary, null, false);
            stickerViewPager = (ViewPager)rootView.findViewById(R.id.sticker_view_pager);
            stickerCollectionPagerAdapter = new StickerCollectionPagerAdapter( ((FragmentActivity)getActivity()).getSupportFragmentManager() );
            stickerViewPager.setAdapter(stickerCollectionPagerAdapter);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (headerView != null) {
                getListView().addHeaderView(headerView);
            }

            DailyListAdapter listAdapter = new DailyListAdapter(getActivity(), pictureGroupList);
            setListAdapter(listAdapter);
        }

        @Override
        public void onStart() {
            super.onStart();

            if (pictureGroup != null) {
                pictureGroup.loadFromDb(getActivity());
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.daily, menu);
        }

        public PictureGroup getPictureGroup() {
            return pictureGroup;
        }

        public ViewPager getStickerViewPager() {
            return stickerViewPager;
        }
    }

    public class StickerCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public StickerCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new StickerImageFragment();
            Bundle args = new Bundle();
            args.putInt(StickerImageFragment.ARG_OBJECT, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return (STICKER_THUMB_IDS.length / STICKER_COUNT_PER_SCREEN) + 1;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            return super.getPageTitle(position);
//        }
    }

    public class StickerImageFragment extends Fragment implements AdapterView.OnItemClickListener {

        public static final String ARG_OBJECT = "StickerImageObject";
        private int imageIndex = 0;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            GridView gridView = (GridView)inflater.inflate(R.layout.fragment_sticker, container, false);
            Bundle args = getArguments();
            imageIndex = args.getInt(ARG_OBJECT);
            gridView.setAdapter(new ImageAdapter(this.getActivity(), imageIndex));
            gridView.setOnItemClickListener(this);
            return gridView;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            new SaveStickerTask(placeholderFragment.getPictureGroup().getId()).execute(i + (imageIndex * STICKER_COUNT_PER_SCREEN));
        }
    }

    private class SaveStickerTask extends AsyncTask<Integer, Void, Boolean> {

        private long rowId;

        public SaveStickerTask(long rowId) {
            this.rowId = rowId;
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            int sticker_id = integers[0];
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DailyInfo.DailyEntry.COLUMN_NAME_STICKER, sticker_id);

            String selection = DailyInfo.DailyEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(rowId) };

            assert db != null;
            int count = db.update(
                    DailyInfo.DailyEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            return count == 1;
        }

        @Override
        protected void onPostExecute(Boolean updateSuccess) {
            super.onPostExecute(updateSuccess);

            if (updateSuccess) {
                openStickerView();
            }
        }
    }

    public static class ImageAdapter extends BaseAdapter {
        private Context context;
        private int index;

        public ImageAdapter(Context c, int index) {
            context = c;
            this.index = index;
        }

        @Override
        public int getCount() {
            if (STICKER_COUNT_PER_SCREEN * (index + 1) <= STICKER_THUMB_IDS.length) {
                return STICKER_COUNT_PER_SCREEN;
            } else {
                return (STICKER_THUMB_IDS.length % STICKER_COUNT_PER_SCREEN);
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            final int imageIndex = position + (index * STICKER_COUNT_PER_SCREEN);
            if (STICKER_THUMB_IDS.length <= imageIndex) {
                return null;
            }

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource( STICKER_THUMB_IDS[position + (index * STICKER_COUNT_PER_SCREEN)] );
            return imageView;
        }
    }
}
