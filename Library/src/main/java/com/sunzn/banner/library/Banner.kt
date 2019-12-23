package com.sunzn.banner.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

open class Banner<T> @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : BannerBaseView(context, attrs, defStyleAttr) {

    private val TAG = "Banner"

    private val mLock = Any()

    private var mAttached: Boolean = false

    private var mIndicatorMargin: Int = 0
    private var mIndicatorGravity: Int = 0

    private var mRecyclerView: RecyclerView? = null
    private var mLinearLayout: LinearLayout? = null

    private var mBannerAdapter: BannerAdapter? = null

    private val mHandler = Handler()

    private var isPlaying: Boolean = false
    private var mIsIndicatorShow: Boolean = false
    private var mNestedEnabled: Boolean = false

    internal var mFilter = IntentFilter()

    private val mData = ArrayList<T>()

    private var mOnItemPickListener: OnItemPickListener<T>? = null

    private var mOnItemBindListener: OnItemBindListener<T>? = null

    private var mOnItemClickListener: OnItemClickListener<T>? = null

    private var mIndicatorGainDrawable: Drawable? = null
    private var mIndicatorMissDrawable: Drawable? = null

    private var mInterval: Int = 0
    private var mCurrentIndex: Int = 0
    private var mIndicatorSize: Int = 0
    private var mIndicatorSpace: Int = 0

    companion object {
        private const val DEFAULT_GAIN_COLOR = -0x1
        private const val DEFAULT_MISS_COLOR = 0x50ffffff
    }

    private val mBannerTask = object : Runnable {

        override fun run() {
            if (isPlaying) {
                val firstPos = (mRecyclerView!!.layoutManager as BannerLayoutManager).findFirstVisibleItemPosition()
                if (firstPos >= mCurrentIndex) {
                    mRecyclerView!!.smoothScrollToPosition(++mCurrentIndex)
                    switchIndicator()
                    mHandler.postDelayed(this, mInterval.toLong())
                } else {
                    mHandler.postDelayed(this, (mInterval * 2).toLong())
                }
            }
        }

    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when {
                Intent.ACTION_USER_PRESENT == action -> {
                    setPlaying(true)
                }
                Intent.ACTION_SCREEN_ON == action -> {
                    setPlaying(true)
                }
                Intent.ACTION_SCREEN_OFF == action -> {
                    setPlaying(false)
                }
                BannerAction.ACTION_LOOP == action -> {
                    setPlaying(true)
                }
                BannerAction.ACTION_STOP == action -> {
                    setPlaying(false)
                }
            }
        }
    }

    interface OnItemClickListener<T> {

        fun onItemClick(position: Int, item: T)

    }

    interface OnItemPickListener<T> {

        fun onItemPick(position: Int, item: T)

    }

    interface OnItemBindListener<T> {

        fun onItemBind(position: Int, item: T, view: ImageView)

    }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Banner)
        val gainDrawable = attributes.getDrawable(R.styleable.Banner_indicator_gain)
        val missDrawable = attributes.getDrawable(R.styleable.Banner_indicator_miss)
        mIndicatorGravity = attributes.getInt(R.styleable.Banner_indicator_gravity, 1)
        mIsIndicatorShow = attributes.getBoolean(R.styleable.Banner_indicator_show, true)
        mNestedEnabled = attributes.getBoolean(R.styleable.Banner_nested_enabled, true)
        val mInch = attributes.getFloat(R.styleable.Banner_banner_inch, 100f)
        mInterval = attributes.getInt(R.styleable.Banner_banner_interval, 3000)
        mIndicatorSize = attributes.getDimensionPixelSize(R.styleable.Banner_indicator_size, 0)
        mIndicatorSpace = attributes.getDimensionPixelSize(R.styleable.Banner_indicator_space, dp2px(4))
        mIndicatorMargin = attributes.getDimensionPixelSize(R.styleable.Banner_indicator_margin, dp2px(8))

        mIndicatorGainDrawable = if (gainDrawable == null) {
            getDefaultDrawable(DEFAULT_GAIN_COLOR)
        } else {
            if (gainDrawable is ColorDrawable) {
                getDefaultDrawable(gainDrawable)
            } else {
                gainDrawable
            }
        }

        mIndicatorMissDrawable = if (missDrawable == null) {
            getDefaultDrawable(DEFAULT_MISS_COLOR)
        } else {
            if (missDrawable is ColorDrawable) {
                getDefaultDrawable(missDrawable)
            } else {
                missDrawable
            }
        }

        when (mIndicatorGravity) {
            0 -> mIndicatorGravity = GravityCompat.START
            1 -> mIndicatorGravity = Gravity.CENTER
            2 -> mIndicatorGravity = GravityCompat.END
        }

        attributes.recycle()

        mRecyclerView = RecyclerView(context)
        mLinearLayout = LinearLayout(context)

        mFilter.addAction(Intent.ACTION_SCREEN_ON)
        mFilter.addAction(Intent.ACTION_SCREEN_OFF)
        mFilter.addAction(Intent.ACTION_USER_PRESENT)
        mFilter.addAction(BannerAction.ACTION_LOOP)
        mFilter.addAction(BannerAction.ACTION_STOP)

        PagerSnapHelper().attachToRecyclerView(mRecyclerView)
        mBannerAdapter = BannerAdapter()
        mRecyclerView!!.adapter = mBannerAdapter
        mRecyclerView!!.overScrollMode = View.OVER_SCROLL_NEVER
        mRecyclerView!!.isNestedScrollingEnabled = mNestedEnabled
        mRecyclerView!!.layoutManager = BannerLayoutManager(context, LinearLayoutManager.HORIZONTAL, false, mInch)
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val sPos = (recyclerView.layoutManager as BannerLayoutManager).findFirstVisibleItemPosition()
                    val ePos = (recyclerView.layoutManager as BannerLayoutManager).findLastVisibleItemPosition()
                    if (sPos == ePos && mCurrentIndex != ePos) {
                        mCurrentIndex = ePos
                        switchIndicator()
                    }
                }
            }
        })

        val recyclerViewParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val linearLayoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayoutParams.gravity = Gravity.BOTTOM or mIndicatorGravity
        linearLayoutParams.setMargins(mIndicatorMargin, mIndicatorMargin, mIndicatorMargin, mIndicatorMargin)
        mLinearLayout!!.orientation = LinearLayout.HORIZONTAL
        mLinearLayout!!.gravity = Gravity.CENTER

        addView(mRecyclerView, recyclerViewParams)
        addView(mLinearLayout, linearLayoutParams)
    }

    private fun getDefaultDrawable(drawable: Drawable): Drawable {
        return getDefaultDrawable((drawable as ColorDrawable).color)
    }

    private fun getDefaultDrawable(color: Int): Drawable {
        val gradient = GradientDrawable()
        gradient.setSize(dp2px(6), dp2px(6))
        gradient.cornerRadius = dp2px(6).toFloat()
        gradient.setColor(color)
        return gradient
    }

    fun setDefaultGainColor(color: Int) {
        mIndicatorGainDrawable = getDefaultDrawable(color)
    }

    fun setDefaultMissColor(color: Int) {
        mIndicatorMissDrawable = getDefaultDrawable(color)
    }

    fun setIndicatorGravity(gravity: Int) {
        mIndicatorGravity = gravity
        val params = mLinearLayout!!.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM or mIndicatorGravity
        mLinearLayout!!.layoutParams = params
    }

    fun setIndicatorMargin(margin: Int) {
        mIndicatorMargin = dp2px(margin)
        val params = mLinearLayout!!.layoutParams as FrameLayout.LayoutParams
        params.setMargins(mIndicatorMargin, mIndicatorMargin, mIndicatorMargin, mIndicatorMargin)
        mLinearLayout!!.layoutParams = params
    }

    private fun createIndicators() {
        if (mLinearLayout != null) {
            if (mIsIndicatorShow) {
                mLinearLayout!!.removeAllViews()
                mLinearLayout!!.visibility = View.VISIBLE
                for (i: Int in 0 until mData.size) {
                    val img = AppCompatImageView(context)
                    val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    lp.leftMargin = mIndicatorSpace / 2
                    lp.rightMargin = mIndicatorSpace / 2
                    if (mIndicatorSize >= dp2px(4)) {
                        lp.height = mIndicatorSize
                        lp.width = lp.height
                    } else {
                        img.minimumWidth = dp2px(2)
                        img.minimumHeight = dp2px(2)
                    }
                    img.setImageDrawable(if (i == 0) mIndicatorGainDrawable else mIndicatorMissDrawable)
                    mLinearLayout!!.addView(img, lp)
                }
            } else {
                mLinearLayout!!.removeAllViews()
                mLinearLayout!!.visibility = View.GONE
            }
        }
    }

    private fun switchIndicator() {
        if (mData.size > 0) {
            val position = mCurrentIndex % mData.size

            if (mIsIndicatorShow && mLinearLayout != null && mLinearLayout!!.childCount > 0) {
                for (i in 0 until mLinearLayout!!.childCount) {
                    (mLinearLayout!!.getChildAt(i) as AppCompatImageView).setImageDrawable(if (i == position) mIndicatorGainDrawable else mIndicatorMissDrawable)
                }
            }

            if (mOnItemPickListener != null) {
                mOnItemPickListener!!.onItemPick(position, mData[position])
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> setPlaying(false)
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> setPlaying(true)
            MotionEvent.ACTION_CANCEL -> setPlaying(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e(TAG, "Banner onAttachedToWindow")
        setPlaying(true)
        regReceiver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e(TAG, "Banner onDetachedFromWindow")
        setPlaying(false)
        unrReceiver()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        setPlaying(true)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        setPlaying(false)
    }

    fun setBannerData(data: List<T>?) {
        setPlaying(false)
        if (!data.isNullOrEmpty()) {
            if (data.size > 1) {
                mData.clear()
                mData.addAll(data)
                mIsIndicatorShow = true
                mCurrentIndex = mData.size * 100000
                mBannerAdapter!!.notifyDataSetChanged()
                mRecyclerView!!.scrollToPosition(mCurrentIndex)
                createIndicators()
                setPlaying(true)
            } else {
                mData.clear()
                mCurrentIndex = 0
                mData.addAll(data)
                mIsIndicatorShow = false
                mBannerAdapter!!.notifyDataSetChanged()
                createIndicators()
            }
        }
    }

    fun scrollToCurrentPosition() {
        if (mRecyclerView != null) {
            mRecyclerView!!.scrollToPosition(mCurrentIndex)
            switchIndicator()
        }
    }

    private inner class BannerAdapter : RecyclerView.Adapter<BannerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context)
            val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.id = R.id.banner_image_view_id
            imageView.layoutParams = params
            imageView.setOnClickListener {
                if (mOnItemClickListener != null)
                    mOnItemClickListener!!.onItemClick(mCurrentIndex % mData.size, mData[mCurrentIndex % mData.size])
            }
            return BannerViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            if (mOnItemBindListener != null)
                mOnItemBindListener!!.onItemBind(position % mData.size, mData[position % mData.size], holder.mImageView)
        }

        override fun getItemCount(): Int {
            return if (mData.size < 2) mData.size else Integer.MAX_VALUE
        }

    }

    private class BannerViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var mImageView: ImageView = itemView.findViewById(R.id.banner_image_view_id)

    }

    fun setPlaying(playing: Boolean) {
        synchronized(mLock) {
            if (playing) {
                playBanner()
            } else {
                stopBanner()
            }
        }
    }

    private fun playBanner() {
        if (!isPlaying && mBannerAdapter!!.itemCount > 1) {
            isPlaying = true
            mHandler.removeCallbacks(mBannerTask)
            mHandler.postDelayed(mBannerTask, mInterval.toLong())
            Log.e(TAG, "Play Banner")
        }
    }

    private fun stopBanner() {
        isPlaying = false
        mHandler.removeCallbacks(mBannerTask)
        Log.e(TAG, "Stop Banner")
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        mOnItemClickListener = listener
    }

    fun setOnItemPickListener(listener: OnItemPickListener<T>) {
        mOnItemPickListener = listener
    }

    fun setOnItemBindListener(listener: OnItemBindListener<T>) {
        mOnItemBindListener = listener
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics).toInt()
    }

    private fun regReceiver() {
        if (!mAttached) {
            mAttached = true
            context.registerReceiver(mReceiver, mFilter)
        }
    }

    private fun unrReceiver() {
        if (mAttached) {
            context.unregisterReceiver(mReceiver)
            mAttached = false
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }

}
