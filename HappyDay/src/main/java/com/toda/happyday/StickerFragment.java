package com.toda.happyday;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fpgeek on 2014. 1. 19..
 */
public class StickerFragment extends Fragment {

    private Activity activity;

    public StickerFragment() {
        super();
    }

    public StickerFragment(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sticker, container, false);
    }
}