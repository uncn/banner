package com.sunzn.banner.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by sunzn on 2017/3/31.
 */

class BannerLayoutManager extends LinearLayoutManager {

    private static float MILLISECONDS_PER_INCH = 100f;

    private Context mContext;

    public BannerLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    public BannerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mContext = context;
    }

    BannerLayoutManager(Context context, int orientation, boolean reverseLayout, float inch) {
        super(context, orientation, reverseLayout);
        MILLISECONDS_PER_INCH = inch;
        mContext = context;
    }

    public BannerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        LinearSmoothScroller mScroller = new LinearSmoothScroller(mContext) {

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

        };

        mScroller.setTargetPosition(position);

        startSmoothScroll(mScroller);

    }
}
