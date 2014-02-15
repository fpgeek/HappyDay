package com.toda.happyday.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toda.happyday.R;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.presenters.PictureGroupPresenter;

import java.util.ArrayList;
import java.util.List;

public class PictureGroupActivity extends Activity {

    private final static int REQUEST_CODE_TO_ONE_DAY_ACTIVITY = 1;
    private PictureGroupPresenter mPictureGroupPresenter;

    private List<PictureGroup> mPictureGroups;
    private ListView onTouchListView;
    private ListView mListViewLeft;
    private ListView mListViewRight;
    private PictureGroupAdapter mLeftAdapter;
    private PictureGroupAdapter mRightAdapter;

    private int[] mLeftViewsHeights;
    private int[] mRightViewsHeights;

    private PictureGroup mShouldUpdatePictureGroup = null;
    private View mShouldUpdateView = null;
    private static ImageListLoader mImageListLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_group_list);

        mImageListLoader = new ImageListLoader(this);

        mPictureGroupPresenter = new PictureGroupPresenter(this);
        mPictureGroupPresenter.loadPictureGroups();

        mListViewLeft = (ListView) findViewById(R.id.list_view_left);
        mListViewRight = (ListView) findViewById(R.id.list_view_right);

        mListViewLeft.setOnTouchListener(touchListener);
        mListViewRight.setOnTouchListener(touchListener);
        mListViewLeft.setOnScrollListener(scrollListener);
        mListViewRight.setOnScrollListener(scrollListener);
        mListViewLeft.setOnItemClickListener(itemClickListener);
        mListViewRight.setOnItemClickListener(itemClickListener);
    }

    // Passing the touch event to the opposite list
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        boolean dispatched = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.equals(mListViewLeft) && !dispatched) {
                dispatched = true;
                mListViewRight.dispatchTouchEvent(event);
                onTouchListView = mListViewLeft;
            } else if (v.equals(mListViewRight) && !dispatched) {
                dispatched = true;
                mListViewLeft.dispatchTouchEvent(event);
                onTouchListView = mListViewRight;
            }

            dispatched = false;
            return false;
        }
    };

    /**
     * Synchronizing scrolling
     * Distance from the top of the first visible element opposite list:
     * sum_heights(opposite invisible screens) - sum_heights(invisible screens) + distance from top of the first visible child
     */
    AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView v, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (view.getChildAt(0) != null) {
                if (view.equals(mListViewLeft) ){
                    mLeftViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < mListViewRight.getFirstVisiblePosition(); i++) {
                        h += mRightViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < mListViewLeft.getFirstVisiblePosition(); i++) {
                        hi += mLeftViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    mListViewRight.setSelectionFromTop(mListViewRight.getFirstVisiblePosition(), top);
                } else if (view.equals(mListViewRight)) {
                    mRightViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < mListViewLeft.getFirstVisiblePosition(); i++) {
                        h += mLeftViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < mListViewRight.getFirstVisiblePosition(); i++) {
                        hi += mRightViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    mListViewLeft.setSelectionFromTop(mListViewLeft.getFirstVisiblePosition(), top);
                }

            }

        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            if (adapterView == onTouchListView) {

                final int clickedPictureGroupIndex = getClickPictureGroupIndex(adapterView, i);
                if (clickedPictureGroupIndex >= mPictureGroups.size()) { return; }

                PictureGroup pictureGroup = mPictureGroups.get(clickedPictureGroupIndex);

                if (pictureGroup != null) {
                    mShouldUpdatePictureGroup = pictureGroup;
                    mShouldUpdateView = view;
                    Intent intent = new Intent(view.getContext(), OneDayActivity.class);
                    intent.putExtra(getString(R.string.extra_daily_data_array), (Parcelable)pictureGroup);
                    startActivityForResult(intent, REQUEST_CODE_TO_ONE_DAY_ACTIVITY);
                }
            }
        }
    };

    private int getClickPictureGroupIndex(AdapterView<?> adapterView, int i) {
        if (adapterView == mListViewLeft) {
            return i*2;
        } else if (adapterView == mListViewRight) {
            return i*2 + 1;
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TO_ONE_DAY_ACTIVITY && resultCode == OneDayActivity.RESULT_CODE_FROM_ONE_DAY_ACTIVITY) {
            PictureGroup updatedPictureGroup = data.getParcelableExtra(OneDayActivity.INTENT_EXTRA_NAME);
            updateItemView(updatedPictureGroup);
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
            if (dairyText != null) {
                TextView diaryTextView = (TextView)mShouldUpdateView.findViewById(R.id.dairy_text);
                diaryTextView.setText(dairyText);
            }
        }
    }

    public void setPictureGroups(List<PictureGroup> pictureGroups) {
        mPictureGroups = pictureGroups;

        List<PictureGroup> leftPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);
        List<PictureGroup> rightPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);
        dividePictureGroups(pictureGroups, leftPictureGroups, rightPictureGroups);

        final int leftPictureGroupsHeight = getMainPicturesHeight(leftPictureGroups);
        final int rightPictureGroupsHeight = getMainPicturesHeight(rightPictureGroups);

        int heightDiff = leftPictureGroupsHeight - rightPictureGroupsHeight;
        View footerView = getLayoutInflater().inflate(R.layout.picture_last, null);
        ImageView imageView = (ImageView)footerView.findViewById(R.id.picture_last_image);

        if (heightDiff > 0) {
            mLeftViewsHeights = new int[leftPictureGroups.size()];
            mRightViewsHeights = new int[rightPictureGroups.size() + 1];
            imageView.getLayoutParams().height = heightDiff;
            mListViewRight.addFooterView(footerView);
        } else if (heightDiff < 0) {
            mLeftViewsHeights = new int[leftPictureGroups.size() + 1];
            mRightViewsHeights = new int[rightPictureGroups.size()];
            imageView.getLayoutParams().height = -heightDiff;
            mListViewLeft.addFooterView(footerView);
        }

        mLeftAdapter = new PictureGroupAdapter(this, leftPictureGroups, mImageListLoader);
        mRightAdapter = new PictureGroupAdapter(this, rightPictureGroups, mImageListLoader);
        mListViewLeft.setAdapter(mLeftAdapter);
        mListViewRight.setAdapter(mRightAdapter);
    }

    private int getMainPicturesHeight(List<PictureGroup> pictureGroupList) {
        int height = 0;
        for (PictureGroup pictureGroup : pictureGroupList) {
            height += pictureGroup.getMainPictureHeight();
        }
        return height;
    }

    private void dividePictureGroups(List<PictureGroup> pictureGroups, List<PictureGroup> leftPictureGroups, List<PictureGroup> rightPictureGroups) {
        final int size = pictureGroups.size();
        for (int i=0; i<size; i++) {
            if (i % 2 == 0) {
                leftPictureGroups.add(pictureGroups.get(i));
            } else {
                rightPictureGroups.add(pictureGroups.get(i));
            }
        }
    }
}
