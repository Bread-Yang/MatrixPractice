package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import robin.com.testmatrix.view.CustomView
import robin.com.testmatrix.view.utils.CanvasAidUtils


/**
 * Created by Robin Yang on 12/5/17.
 */
class CanvasVonvertTouchTest @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CustomView(context, attrs) {
    internal var down_x = -1f
    internal var down_y = -1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // ▼ 注意此处使用 getRawX，而不是 getX
                down_x = event.x
                down_y = event.y
                invalidate()
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                down_y = -1f
                down_x = down_y
                invalidate()
            }
        }

        return true
    }

    override protected fun onDraw(canvas: Canvas) {
        val pts = floatArrayOf(down_x, down_y)

        drawTouchCoordinateSpace(canvas)            // 绘制触摸坐标系，灰色
        // ▼注意画布平移
        canvas.translate(mViewWidth / 2f, mViewHeight / 2f)

        drawTranslateCoordinateSpace(canvas)        // 绘制平移后的坐标系，红色

        if (pts[0] == -1f && pts[1] == -1f) return     // 如果没有就返回

        // ▼ 获得当前矩阵的逆矩阵
        val invertMatrix = Matrix()
        canvas.getMatrix().invert(invertMatrix)

        // ▼ 使用 mapPoints 将触摸位置转换为画布坐标
        invertMatrix.mapPoints(pts)
//        canvas.getMatrix().mapPoints(pts)

        // 在触摸位置绘制一个小圆
        canvas.drawCircle(pts[0], pts[1], 20f, mDeafultPaint)
    }

    /**
     * 绘制触摸坐标系，颜色为灰色，为了能够显示出坐标系，将坐标系位置稍微偏移了一点
     */
    private fun drawTouchCoordinateSpace(canvas: Canvas) {
        canvas.save()
        canvas.translate(10f, 10f)
        CanvasAidUtils.set2DAxisLength(1000f, 0f, 1400f, 0f)
        CanvasAidUtils.setLineColor(Color.GRAY)
        CanvasAidUtils.draw2DCoordinateSpace(canvas)
        canvas.restore()
    }

    /**
     * 绘制平移后的坐标系，颜色为红色
     */
    private fun drawTranslateCoordinateSpace(canvas: Canvas) {
        CanvasAidUtils.set2DAxisLength(500f, 500f, 700f, 700f)
        CanvasAidUtils.setLineColor(Color.RED)
        CanvasAidUtils.draw2DCoordinateSpace(canvas)
        CanvasAidUtils.draw2DCoordinateSpace(canvas)
    }
}