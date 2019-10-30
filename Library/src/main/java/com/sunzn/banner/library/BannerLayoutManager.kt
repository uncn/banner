package com.sunzn.banner.library

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

internal class BannerLayoutManager : LinearLayoutManager {

    companion object {
        private var MILLISECONDS_PER_INCH = 100f
    }

    private var mContext: Context? = null

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {
        mContext = context
    }

    constructor(context: Context, orientation: Int, reverseLayout: Boolean, inch: Float) : super(context, orientation, reverseLayout) {
        MILLISECONDS_PER_INCH = inch
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mContext = context
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {

        val mScroller = object : LinearSmoothScroller(mContext!!) {

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

        }

        mScroller.targetPosition = position

        startSmoothScroll(mScroller)

    }

}
