package robin.com.testmatrix.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * Author: GcsSloop
 *
 *
 * Created Date: 16/5/31
 *
 *
 * Copyright (C) 2016 GcsSloop.
 *
 *
 * GitHub: https://github.com/GcsSloop
 */
class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // 画笔
    private var mPaint = Paint()

    // View 宽高
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    // 当前的状态(非常重要)
    private var mCurrentState = State.NONE

    // 放大镜与外部圆环
    private var path_search = Path()
    private var path_circle = Path()

    // 测量Path 并截取部分的工具
    private var mMeasure = PathMeasure()

    // 默认的动效周期 2s
    private val defaultDuration = 2000

    // 控制各个过程的动画
    private var mStartingAnimator = ValueAnimator()
    private var mSearchingAnimator = ValueAnimator()
    private var mEndingAnimator = ValueAnimator()

    // 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private var mAnimatorValue = 0f

    // 动效过程监听器
    private var mUpdateListener: ValueAnimator.AnimatorUpdateListener? = null
    private var mAnimatorListener: Animator.AnimatorListener? = null

    // 用于控制动画状态转换
    private var mAnimatorHandler = Handler()

    // 判断是否已经搜索结束
    private var isOver = false

    private var count = 0

    // 这个视图拥有的状态
    enum class State {
        NONE,
        STARTING,
        SEARCHING,
        ENDING
    }

    init {
        initAll()
    }

    fun initAll() {

        initPaint()

        initPath()

        initListener()

        initHandler()

        initAnimator()

        // 进入开始动画
        mCurrentState = State.STARTING
        mStartingAnimator.start()
    }

    private fun initPaint() {
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setColor(Color.WHITE)
        mPaint.setStrokeWidth(15F)
        mPaint.setStrokeCap(Paint.Cap.ROUND)
        mPaint.setAntiAlias(true)
    }

    private fun initPath() {
        path_search = Path()
        path_circle = Path()

        mMeasure = PathMeasure()

        // 注意,不要到360度,否则内部会自动优化,测量不能取到需要的数值
        val oval1 = RectF(-50f, -50f, 50f, 50f)          // 放大镜圆环
        path_search.addArc(oval1, 45F, 359.9f)

        val oval2 = RectF(-100f, -100f, 100f, 100f)      // 外部圆环
        path_circle.addArc(oval2, 45F, -359.9f)

        val pos = FloatArray(2)

        mMeasure.setPath(path_circle, false)               // 放大镜把手的位置
        mMeasure.getPosTan(0f, pos, null)

        path_search.lineTo(pos[0], pos[1])                 // 放大镜把手

        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1])
    }

    private fun initListener() {
        mUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
            mAnimatorValue = animation.animatedValue as Float
            invalidate()
        }

        mAnimatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                // getHandle发消息通知动画状态更新
                mAnimatorHandler.sendEmptyMessage(0)
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        }
    }

    private fun initHandler() {
        mAnimatorHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (mCurrentState) {
                    SearchView.State.STARTING -> {
                        // 从开始动画转换好搜索动画
                        isOver = false
                        mCurrentState = State.SEARCHING
//                        mStartingAnimator.removeAllListeners()
                        mSearchingAnimator.start()
                    }
                    SearchView.State.SEARCHING -> if (!isOver) {  // 如果搜索未结束 则继续执行搜索动画
                        mSearchingAnimator.start()
                        Log.e("Update", "RESTART")

                        count++
                        if (count > 2) {       // count大于2则进入结束状态
                            isOver = true
                        }
                    } else {        // 如果搜索已经结束 则进入结束动画
                        mCurrentState = State.ENDING
                        mEndingAnimator.start()
                    }
                    SearchView.State.ENDING -> {
                        // 从结束动画转变为无状态
//                        mCurrentState = State.NONE

                        initAnimator()
                        mCurrentState = State.STARTING
                        mStartingAnimator.start()
                    }
                }
            }
        }
    }

    private fun initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(defaultDuration.toLong())
        mSearchingAnimator = ValueAnimator.ofFloat(0F, 1F).setDuration(defaultDuration.toLong())
        mEndingAnimator = ValueAnimator.ofFloat(1F, 0F).setDuration(defaultDuration.toLong())

        mStartingAnimator.addUpdateListener(mUpdateListener)
        mSearchingAnimator.addUpdateListener(mUpdateListener)
        mEndingAnimator.addUpdateListener(mUpdateListener)

        mStartingAnimator.addListener(mAnimatorListener)
        mSearchingAnimator.addListener(mAnimatorListener)
        mEndingAnimator.addListener(mAnimatorListener)
    }


    override protected fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
    }

    override protected fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawSearch(canvas)
    }

    private fun drawSearch(canvas: Canvas) {

        Log.e("SearchView", "canvas.getMatrix : ${canvas.getMatrix().toShortString()}")

        mPaint.setColor(Color.WHITE)


        canvas.translate(mViewWidth / 2F, mViewHeight / 2F)

        canvas.drawColor(Color.parseColor("#0082D7"))

        when (mCurrentState) {
            SearchView.State.NONE -> canvas.drawPath(path_search, mPaint)
            SearchView.State.STARTING -> {
                mMeasure.setPath(path_search, false)
                val dst = Path()
                mMeasure.getSegment(mMeasure.length * mAnimatorValue, mMeasure.length, dst, true)
                canvas.drawPath(dst, mPaint)
            }
            SearchView.State.SEARCHING -> {
                mMeasure.setPath(path_circle, false)
                val dst2 = Path()
                val stop = mMeasure.length * mAnimatorValue
                val start = (stop - (0.5 - Math.abs(mAnimatorValue - 0.5)) * 200f).toFloat()
                mMeasure.getSegment(start, stop, dst2, true)
                canvas.drawPath(dst2, mPaint)
            }
            SearchView.State.ENDING -> {
                mMeasure.setPath(path_search, false)
                val dst3 = Path()
                mMeasure.getSegment(mMeasure.length * mAnimatorValue, mMeasure.length, dst3, true)
                canvas.drawPath(dst3, mPaint)
            }
        }
    }
}
