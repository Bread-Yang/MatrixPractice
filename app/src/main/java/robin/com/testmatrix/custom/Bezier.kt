package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * Created by Robin Yang on 12/1/17.
 */
class Bezier : View {

    private val mPaint: Paint
    private var centerX: Int = 0
    private var centerY: Int = 0

    private val start: PointF
    private val end: PointF
    private val control: PointF

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    init {
        mPaint = Paint()
        mPaint.setColor(Color.BLACK)
        mPaint.setStrokeWidth(8F)
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setTextSize(60F)

        start = PointF(0f, 0f)
        end = PointF(0f, 0f)
        control = PointF(0f, 0f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h / 2

        // 初始化数据点和控制点的位置
        start.x = (centerX - 200).toFloat()
        start.y = centerY.toFloat()
        end.x = (centerX + 200).toFloat()
        end.y = centerY.toFloat()
        control.x = centerX.toFloat()
        control.y = (centerY - 100).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 根据触摸位置更新控制点，并提示重绘
        control.x = event.x
        control.y = event.y
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制数据点和控制点
        mPaint.setColor(Color.GRAY)
        mPaint.setStrokeWidth(20F)
        canvas.drawPoint(start.x, start.y, mPaint)
        canvas.drawPoint(end.x, end.y, mPaint)
        canvas.drawPoint(control.x, control.y, mPaint)

        // 绘制辅助线
        mPaint.setStrokeWidth(4F)
        canvas.drawLine(start.x, start.y, control.x, control.y, mPaint)
        canvas.drawLine(end.x, end.y, control.x, control.y, mPaint)

        // 绘制贝塞尔曲线
        mPaint.setColor(Color.RED)
        mPaint.setStrokeWidth(8F)

        val path = Path()

        path.moveTo(start.x, start.y)
        path.quadTo(control.x, control.y, end.x, end.y)

        canvas.drawPath(path, mPaint)
    }
}
