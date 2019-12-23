package com.sunzn.banner.library

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

abstract class BannerBaseView : FrameLayout, DefaultLifecycleObserver, BannerActionListener {

    private val TAG = "BannerBaseView"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "onDestroy")
    }

}
