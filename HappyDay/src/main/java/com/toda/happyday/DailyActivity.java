package com.toda.happyday;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class DailyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
        Intent intent = new Intent(this, StickerActivity.class);
        startActivity(intent);
    }

    private void openEditView() {
        Intent intent = new Intent(this, WriteActivity.class);
        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_daily, container, false);

            Parcelable[] dailyDataArray = getActivity().getIntent().getParcelableArrayExtra(MainActivity.EXTRA_DAILY_DATA_ARRAY);

            List<List<DailyData>> dailyDataGroup = new ArrayList<List<DailyData>>(dailyDataArray.length);
            for(Parcelable parcel : dailyDataArray) {
                DailyData dailyData = (DailyData)parcel;
                List<DailyData> dailyDataList = new ArrayList<DailyData>(1);
                dailyDataList.add(dailyData);
                dailyDataGroup.add(dailyDataList);
            }

            DailyListAdapter listAdapter = new DailyListAdapter(getActivity(), dailyDataGroup);
            setListAdapter(listAdapter);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.daily, menu);
        }
    }

}
