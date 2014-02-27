package com.toda.happyday.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.toda.happyday.R;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.utils.TextViewUtil;

import java.util.List;

public class PictureGroupActivity extends Activity {

    private final static int REQUEST_CODE_TO_ONE_DAY_ACTIVITY = 1;

    private List<PictureGroup> mPictureGroups;
    private ListView onTouchListView;
    private StaggeredGridView mGridView;
    private PictureGroupAdapter mPictureGroupAdapter;

    private PictureGroup mShouldUpdatePictureGroup = null;
    private View mShouldUpdateView = null;
    private static ImageListLoader mImageListLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.picture_group_list);

        List<PictureGroup> pictureGroups = getIntent().getParcelableArrayListExtra(getString(R.string.EXTRA_PICTURE_GROUP_LIST));
        mPictureGroups = pictureGroups;

        mImageListLoader = new ImageListLoader(this);

        mGridView = (StaggeredGridView)findViewById(R.id.grid_view);
        mGridView.setOnItemClickListener(itemClickListener);

        mPictureGroupAdapter = new PictureGroupAdapter(this, pictureGroups, mImageListLoader);
        mGridView.setAdapter(mPictureGroupAdapter);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            PictureGroup pictureGroup = mPictureGroups.get(i);
            if (pictureGroup != null) {
                mShouldUpdatePictureGroup = pictureGroup;
                mShouldUpdateView = view;
                Intent intent = new Intent(view.getContext(), OneDayActivity.class);
                intent.putExtra(getString(R.string.extra_daily_data_array), (Parcelable)pictureGroup);
                startActivityForResult(intent, REQUEST_CODE_TO_ONE_DAY_ACTIVITY);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TO_ONE_DAY_ACTIVITY && resultCode == OneDayActivity.RESULT_CODE_FROM_ONE_DAY_ACTIVITY) {
            PictureGroup updatedPictureGroup = data.getParcelableExtra(OneDayActivity.INTENT_EXTRA_NAME);
            updateItemView(updatedPictureGroup);
            mPictureGroupAdapter.notifyDataSetChanged();
        }
    }

    private void updateItemView(PictureGroup pictureGroup) {
        if (mShouldUpdatePictureGroup != null) {
            mShouldUpdatePictureGroup.changeProperties(pictureGroup);
        }

        if (mShouldUpdateView != null) {
            final int sticker = pictureGroup.getSticker();
            ImageView stickerImageView = (ImageView)mShouldUpdateView.findViewById(R.id.sticker_thumb);
            stickerImageView.setImageResource(sticker);

            final String dairyText = pictureGroup.getDairyText();
            TextView diaryTextView = (TextView)mShouldUpdateView.findViewById(R.id.dairy_text);
            diaryTextView.setText(dairyText);
            TextViewUtil.setText(diaryTextView, dairyText);
        }
    }
}
