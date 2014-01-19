package com.toda.happyday;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toda.happyday.model.PictureGroup;
import com.toda.happyday.model.PictureInfo;

import java.util.ArrayList;
import java.util.List;

public class DailyActivity extends Activity {

    private PlaceholderFragment placeholderFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

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
        // TODO
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
    public static class PlaceholderFragment extends ListFragment {

        private PictureGroup pictureGroup;
        private View headerView;
        private List<PictureGroup> pictureGroupList;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_daily, container, false);

            Parcelable parcelable = getActivity().getIntent().getParcelableExtra(MainActivity.EXTRA_PICTURE_GROUP);
            pictureGroup = (PictureGroup)parcelable;

            pictureGroup.loadFromDb(getActivity());

            pictureGroupList = new ArrayList<PictureGroup>(pictureGroup.size());
            for (PictureInfo pictureInfo : pictureGroup) {
                PictureGroup pictureInfoList = new PictureGroup(1);
                pictureInfoList.add(pictureInfo);
                pictureGroupList.add(pictureInfoList);
            }

            headerView = inflater.inflate(R.layout.diary, null, false);

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
    }

}
