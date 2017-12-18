package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import robin.com.testmatrix.R
import robin.com.testmatrix.view.CustomView




/**
 * 一个可以拖图片动的 View
 */
class DragView1 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CustomView(context, attrs) {
    internal var TAG = "Gcs"

    internal var mBitmap: Bitmap         // 图片
    internal var mBitmapRectF: RectF     // 图片所在区域
    internal var mBitmapMatrix: Matrix   // 控制图片的 matrix

    internal var canDrag = false
    internal var lastPoint = PointF(0f, 0f)

    init {

        // 调整图片大小WechatIMG191.jpeg
        val options = BitmapFactory.Options()
        options.outWidth = 960 / 2
        options.outHeight = 800 / 2

        mBitmap = BitmapFactory.decodeResource(this.resources, R.mipmap.clean_architecture_evolution, options)
        mBitmapRectF = RectF(0f, 0f, mBitmap.width.toFloat(), mBitmap.height.toFloat())
        mBitmapMatrix = Matrix()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                // ▼ 判断是否是第一个手指 && 是否包含在图片区域内
                if (event.getPointerId(event.getActionIndex()) == 0 && mBitmapRectF.contains(event.x.toInt().toFloat(), event.y.toInt().toFloat())) {
                    canDrag = true
                    lastPoint.set(event.x, event.y)
                }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                // ▼ 判断是否是第一个手指
                if (event.getPointerId(event.getActionIndex()) == 0){
                    canDrag = false
                }
            }
            MotionEvent.ACTION_MOVE -> if (canDrag) {
                // 如果存在第一个手指，且这个手指的落点在图片区域内
                if (canDrag) {
                    // ▼ 注意 getX 和 getY
                    val index = event.findPointerIndex(0)
                    // Log.i(TAG, "index="+index);
                    mBitmapMatrix.postTranslate(event.getX(index) - lastPoint.x, event.getY(index) - lastPoint.y)
                    lastPoint.set(event.getX(index), event.getY(index))

                    mBitmapRectF = RectF(0f, 0f, mBitmap.width.toFloat(), mBitmap.height.toFloat())
                    mBitmapMatrix.mapRect(mBitmapRectF)

                    invalidate()
                }
            }
        }

        return true
    }

    override protected fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(mBitmap, mBitmapMatrix, mDeafultPaint)
    }
}