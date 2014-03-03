package com.toda.happyday.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.toda.happyday.R;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.presenters.PictureGroupPresenter;

import java.util.ArrayList;

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

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
//            PictureGroupPresenter pictureGroupPresenter = new PictureGroupPresenter(getActivity());
//            pictureGroupPresenter.initLoadPictureGroups();


            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Intent intent = new Intent(getActivity(), PictureGroupActivity.class);
//            intent.putParcelableArrayListExtra(getActivity().getString(R.string.EXTRA_PICTURE_GROUP_LIST), new ArrayList<PictureGroup>(pictureGroupList));
//            intent.putExtra(mActivity.getString(R.string.EXTRA_LAST_LOAD_DATE_TIME), mLastLoadDateValue);
            startActivity(intent);
            return rootView;
        }
    }
}
