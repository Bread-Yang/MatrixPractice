package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import robin.com.testmatrix.view.CustomView


/**
 * 绘制出第二个手指第位置
 */
class MultiTouchTest @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CustomView(context, attrs) {
    internal var TAG = "Gcs"

    // 用于判断第2个手指是否存在
    internal var haveSecondPoint = false

    // 记录第2个手指第位置
    internal var point = PointF(0f, 0f)

    init {

        mDeafultPaint.isAntiAlias = true
        mDeafultPaint.textAlign = Paint.Align.CENTER
        mDeafultPaint.textSize = 30f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = event.actionIndex

        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN ->
                // 判断是否是第2个手指按下
                if (event.getPointerId(index) == 1) {
                    haveSecondPoint = true
                    point.set(event.y, event.x)
                }
            MotionEvent.ACTION_POINTER_UP ->
                // 判断抬起的手指是否是第2个
                if (event.getPointerId(index) == 1) {
                    haveSecondPoint = false
                    point.set(0f, 0f)
                }
            MotionEvent.ACTION_MOVE -> if (haveSecondPoint) {
                // 通过 pointerId 来获取 pointerIndex
                val pointerIndex = event.findPointerIndex(1)
                // 通过 pointerIndex 来取出对应的坐标
                point.set(event.getX(pointerIndex), event.getY(pointerIndex))
            }
        }

        invalidate()   // 刷新

        return true
    }

    override protected fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(mViewWidth / 2f, mViewHeight / 2f)
        canvas.drawText("追踪第2个按下手指的位置", 0f, 0f, mDeafultPaint)
        canvas.restore()

        // 如果屏幕上有第2个手指则绘制出来其位置
        if (haveSecondPoint) {
            canvas.drawCircle(point.x, point.y, 50f, mDeafultPaint)
        }
    }
}