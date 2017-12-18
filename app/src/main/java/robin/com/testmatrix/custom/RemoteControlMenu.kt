package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import robin.com.testmatrix.view.CustomView


/**
 * Created by Robin Yang on 12/6/17.
 */
class RemoteControlMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CustomView(context, attrs) {
    internal var up_p: Path
    internal var down_p: Path
    internal var left_p: Path
    internal var right_p: Path
    internal var center_p: Path
    internal var up: Region
    internal var down: Region
    internal var left: Region
    internal var right: Region
    internal var center: Region

    internal var mMapMatrix: Matrix = Matrix()

    internal var CENTER = 0
    internal var UP = 1
    internal var RIGHT = 2
    internal var DOWN = 3
    internal var LEFT = 4
    internal var touchFlag = -1
    internal var currentFlag = -1

    internal var mListener: MenuListener? = null

    internal var mDefauColor = -0xb1ad98
    internal var mTouchedColor = -0x20637f

    init {

        up_p = Path()
        down_p = Path()
        left_p = Path()
        right_p = Path()
        center_p = Path()

        up = Region()
        down = Region()
        left = Region()
        right = Region()
        center = Region()

        mDeafultPaint.color = mDefauColor
        mDeafultPaint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mMapMatrix.reset()

        // 注意这个区域的大小
        val globalRegion = Region(-w, -h, w, h)
        var minWidth: Float = if (w > h) h.toFloat() else w.toFloat()
        minWidth *= 0.8f

        val br = minWidth / 2
        val bigCircle = RectF((-br).toFloat(), (-br).toFloat(), br.toFloat(), br.toFloat())

        val sr = minWidth / 4
        val smallCircle = RectF((-sr).toFloat(), (-sr).toFloat(), sr.toFloat(), sr.toFloat())

        val bigSweepAngle = 84f
        val smallSweepAngle = -80f

        // 根据视图大小，初始化 Path 和 Region
        center_p.addCircle(0f, 0f, 0.2f * minWidth, Path.Direction.CW)
        center.setPath(center_p, globalRegion)

        right_p.addArc(bigCircle, -40f, bigSweepAngle)
        right_p.arcTo(smallCircle, 40f, smallSweepAngle)
        right_p.close()
        right.setPath(right_p, globalRegion)

        down_p.addArc(bigCircle, 50f, bigSweepAngle)
        down_p.arcTo(smallCircle, 130f, smallSweepAngle)
        down_p.close()
        down.setPath(down_p, globalRegion)

        left_p.addArc(bigCircle, 140f, bigSweepAngle)
        left_p.arcTo(smallCircle, 220f, smallSweepAngle)
        left_p.close()
        left.setPath(left_p, globalRegion)

        up_p.addArc(bigCircle, 230f, bigSweepAngle)
        up_p.arcTo(smallCircle, 310f, smallSweepAngle)
        up_p.close()
        up.setPath(up_p, globalRegion)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pts = FloatArray(2)
        pts[0] = event.x
        pts[1] = event.y
        mMapMatrix.mapPoints(pts)

        val x = pts[0].toInt()
        val y = pts[1].toInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchFlag = getTouchedPath(x, y)
                currentFlag = touchFlag
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    Log.e("TAG", "pointerIndex=" + i + ", pointerId=" + event.getPointerId(i))
                }
                currentFlag = getTouchedPath(x, y)
            }
            MotionEvent.ACTION_UP -> {
                currentFlag = getTouchedPath(x, y)
                // 如果手指按下区域和抬起区域相同且不为空，则判断点击事件
                if (currentFlag == touchFlag && currentFlag != -1 && mListener != null) {
                    if (currentFlag == CENTER) {
                        mListener!!.onCenterCliched()
                    } else if (currentFlag == UP) {
                        mListener!!.onUpCliched()
                    } else if (currentFlag == RIGHT) {
                        mListener!!.onRightCliched()
                    } else if (currentFlag == DOWN) {
                        mListener!!.onDownCliched()
                    } else if (currentFlag == LEFT) {
                        mListener!!.onLeftCliched()
                    }
                }
                currentFlag = -1
                touchFlag = currentFlag
            }
            MotionEvent.ACTION_CANCEL -> {
                currentFlag = -1
                touchFlag = currentFlag
            }
        }

        invalidate()
        return true
    }

    // 获取当前触摸点在哪个区域
    internal fun getTouchedPath(x: Int, y: Int): Int {
        if (center.contains(x, y)) {
            return 0
        } else if (up.contains(x, y)) {
            return 1
        } else if (right.contains(x, y)) {
            return 2
        } else if (down.contains(x, y)) {
            return 3
        } else if (left.contains(x, y)) {
            return 4
        }
        return -1
    }

    override protected fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(mViewWidth / 2f, mViewHeight / 2f)

        // 获取测量矩阵(逆矩阵)
        if (mMapMatrix.isIdentity()) {
            canvas.getMatrix().invert(mMapMatrix)
        }

        // 绘制默认颜色
        canvas.drawPath(center_p, mDeafultPaint)
        canvas.drawPath(up_p, mDeafultPaint)
        canvas.drawPath(right_p, mDeafultPaint)
        canvas.drawPath(down_p, mDeafultPaint)
        canvas.drawPath(left_p, mDeafultPaint)

        // 绘制触摸区域颜色
        mDeafultPaint.color = mTouchedColor
        if (currentFlag == CENTER) {
            canvas.drawPath(center_p, mDeafultPaint)
        } else if (currentFlag == UP) {
            canvas.drawPath(up_p, mDeafultPaint)
        } else if (currentFlag == RIGHT) {
            canvas.drawPath(right_p, mDeafultPaint)
        } else if (currentFlag == DOWN) {
            canvas.drawPath(down_p, mDeafultPaint)
        } else if (currentFlag == LEFT) {
            canvas.drawPath(left_p, mDeafultPaint)
        }
        mDeafultPaint.color = mDefauColor
    }

    fun setListener(listener: MenuListener) {
        mListener = listener
    }

    // 点击事件监听器
    interface MenuListener {
        fun onCenterCliched()

        fun onUpCliched()

        fun onRightCliched()

        fun onDownCliched()

        fun onLeftCliched()
    }
}