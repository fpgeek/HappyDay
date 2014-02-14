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

import com.toda.happyday.R;
import com.toda.happyday.models.PictureGroup;
import com.toda.happyday.presenters.PictureGroupPresenter;

import java.util.ArrayList;
import java.util.List;

public class PictureGroupActivity extends Activity {

    public final static int RESULT_CODE_FROM_DAILY_ACTIVITY = 1;
    private PictureGroupPresenter mPictureGroupPresenter;

    private List<PictureGroup> mPictureGroups;
    private ListView onTouchListView;
    private ListView listViewLeft;
    private ListView listViewRight;
    private PictureGroupAdapter leftAdapter;
    private PictureGroupAdapter rightAdapter;

    private int[] leftViewsHeights;
    private int[] rightViewsHeights;

    private PictureGroup mShouldUpdatePictureGroup = null;
    private View mShouldUpdateView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list);

        mPictureGroupPresenter = new PictureGroupPresenter(this);
        mPictureGroupPresenter.loadPictureGroups();

        listViewLeft = (ListView) findViewById(R.id.list_view_left);
        listViewRight = (ListView) findViewById(R.id.list_view_right);

        listViewLeft.setOnTouchListener(touchListener);
        listViewRight.setOnTouchListener(touchListener);
        listViewLeft.setOnScrollListener(scrollListener);
        listViewRight.setOnScrollListener(scrollListener);
        listViewLeft.setOnItemClickListener(itemClickListener);
        listViewRight.setOnItemClickListener(itemClickListener);
    }

    // Passing the touch event to the opposite list
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        boolean dispatched = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.equals(listViewLeft) && !dispatched) {
                dispatched = true;
                listViewRight.dispatchTouchEvent(event);
                onTouchListView = listViewLeft;
            } else if (v.equals(listViewRight) && !dispatched) {
                dispatched = true;
                listViewLeft.dispatchTouchEvent(event);
                onTouchListView = listViewRight;
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
                if (view.equals(listViewLeft) ){
                    leftViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < listViewRight.getFirstVisiblePosition(); i++) {
                        h += rightViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < listViewLeft.getFirstVisiblePosition(); i++) {
                        hi += leftViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    listViewRight.setSelectionFromTop(listViewRight.getFirstVisiblePosition(), top);
                } else if (view.equals(listViewRight)) {
                    rightViewsHeights[view.getFirstVisiblePosition()] = view.getChildAt(0).getHeight();

                    int h = 0;
                    for (int i = 0; i < listViewLeft.getFirstVisiblePosition(); i++) {
                        h += leftViewsHeights[i];
                    }

                    int hi = 0;
                    for (int i = 0; i < listViewRight.getFirstVisiblePosition(); i++) {
                        hi += rightViewsHeights[i];
                    }

                    int top = h - hi + view.getChildAt(0).getTop();
                    listViewLeft.setSelectionFromTop(listViewLeft.getFirstVisiblePosition(), top);
                }

            }

        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            if (adapterView == onTouchListView) {
                PictureGroup pictureGroup = null;
                if (adapterView == listViewLeft) {
                    pictureGroup = mPictureGroups.get(i*2);
                } else if (adapterView == listViewRight) {
                    pictureGroup = mPictureGroups.get(i*2 + 1);
                }

                if (pictureGroup != null) {
                    mShouldUpdatePictureGroup = pictureGroup;
                    mShouldUpdateView = view;
                    Intent intent = new Intent(view.getContext(), DailyActivity.class);
                    intent.putExtra(getString(R.string.extra_daily_data_array), (Parcelable)pictureGroup);
                    startActivityForResult(intent, RESULT_CODE_FROM_DAILY_ACTIVITY);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_FROM_DAILY_ACTIVITY) {
            PictureGroup updatedPictureGroup = data.getParcelableExtra(DailyActivity.INTENT_EXTRA_NAME);
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
                // TODO
            }
        }


    }

    public void setPictureGroups(List<PictureGroup> pictureGroups) {
        mPictureGroups = pictureGroups;

        List<PictureGroup> leftPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);
        List<PictureGroup> rightPictureGroups = new ArrayList<PictureGroup>(pictureGroups.size() / 2);
        dividePictureGroups(pictureGroups, leftPictureGroups, rightPictureGroups);

        leftAdapter = new PictureGroupAdapter(this, leftPictureGroups);
        rightAdapter = new PictureGroupAdapter(this, rightPictureGroups);
        listViewLeft.setAdapter(leftAdapter);
        listViewRight.setAdapter(rightAdapter);

        leftViewsHeights = new int[leftPictureGroups.size()];
        rightViewsHeights = new int[rightPictureGroups.size()];
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
