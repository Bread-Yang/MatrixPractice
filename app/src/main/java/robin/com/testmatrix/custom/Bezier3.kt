package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


/**
 * Created by Robin Yang on 12/1/17.
 */
class Bezier3 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        private val C = 0.551915024494f     // 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置
    }

    private val mPaint: Paint
    private var mCenterX: Float = 0F
    private var mCenterY: Float = 0F

    private val mCenter = PointF(0f, 0f)
    private val mCircleRadius = 200f                  // 圆的半径
    private val mDifference = mCircleRadius * C        // 圆形的控制点与数据点的差值

    private val mData = FloatArray(8)               // 顺时针记录绘制圆形的四个数据点
    private val mCtrl = FloatArray(16)              // 顺时针记录绘制圆形的八个控制点

    private val mDuration = 1000f                     // 变化总时长
    private var mCurrent = 0f                         // 当前已进行时长
    private val mCount = 100f                         // 将时长总共划分多少份
    private val mPiece = mDuration / mCount            // 每一份的时长

    init {

        mPaint = Paint()
        mPaint.setColor(Color.BLACK)
        mPaint.setStrokeWidth(8F)
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setTextSize(60F)


        // 初始化数据点,从圆最下面一个点逆时针开始
        mData[0] = 0f
        mData[1] = mCircleRadius

        mData[2] = mCircleRadius
        mData[3] = 0f

        mData[4] = 0f
        mData[5] = -mCircleRadius

        mData[6] = -mCircleRadius
        mData[7] = 0f

        // 初始化控制点，逆时针

        mCtrl[0] = mData[0] + mDifference
        mCtrl[1] = mData[1]

        mCtrl[2] = mData[2]
        mCtrl[3] = mData[3] + mDifference

        mCtrl[4] = mData[2]
        mCtrl[5] = mData[3] - mDifference

        mCtrl[6] = mData[4] + mDifference
        mCtrl[7] = mData[5]

        mCtrl[8] = mData[4] - mDifference
        mCtrl[9] = mData[5]

        mCtrl[10] = mData[6]
        mCtrl[11] = mData[7] - mDifference

        mCtrl[12] = mData[6]
        mCtrl[13] = mData[7] + mDifference

        mCtrl[14] = mData[0] - mDifference
        mCtrl[15] = mData[1]
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = w / 2F
        mCenterY = h / 2F
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCoordinateSystem(canvas)       // 绘制坐标系

        canvas.translate(mCenterX, mCenterY) // 将坐标系移动到画布中央
        canvas.scale(1F, -1F)                 // 翻转Y轴

        drawAuxiliaryLine(canvas)


        // 绘制贝塞尔曲线
        mPaint.setColor(Color.RED)
        mPaint.setStrokeWidth(8F)

        val path = Path()
        path.moveTo(mData[0], mData[1])

        path.cubicTo(mCtrl[0], mCtrl[1], mCtrl[2], mCtrl[3], mData[2], mData[3])
        path.cubicTo(mCtrl[4], mCtrl[5], mCtrl[6], mCtrl[7], mData[4], mData[5])
        path.cubicTo(mCtrl[8], mCtrl[9], mCtrl[10], mCtrl[11], mData[6], mData[7])
        path.cubicTo(mCtrl[12], mCtrl[13], mCtrl[14], mCtrl[15], mData[0], mData[1])

        canvas.drawPath(path, mPaint)

        mCurrent += mPiece
        if (mCurrent < mDuration) {

            mData[1] -= 120 / mCount
            mCtrl[7] += 80 / mCount
            mCtrl[9] += 80 / mCount

            mCtrl[4] -= 20 / mCount
            mCtrl[10] += 20 / mCount

            postInvalidateDelayed(mPiece.toLong())
        }
    }

    // 绘制辅助线
    private fun drawAuxiliaryLine(canvas: Canvas) {
        // 绘制数据点和控制点
        mPaint.setColor(Color.GRAY)
        mPaint.setStrokeWidth(20F)

        run {
            var i = 0
            while (i < 8) {
                canvas.drawPoint(mData[i], mData[i + 1], mPaint)
                i += 2
            }
        }

        run {
            var i = 0
            while (i < 16) {
                canvas.drawPoint(mCtrl[i], mCtrl[i + 1], mPaint)
                i += 2
            }
        }


        // 绘制辅助线
        mPaint.setStrokeWidth(4F)

        var i = 2
        var j = 2
        while (i < 8) {
            canvas.drawLine(mData[i], mData[i + 1], mCtrl[j], mCtrl[j + 1], mPaint)
            canvas.drawLine(mData[i], mData[i + 1], mCtrl[j + 2], mCtrl[j + 3], mPaint)
            i += 2
            j += 4
        }
        canvas.drawLine(mData[0], mData[1], mCtrl[0], mCtrl[1], mPaint)
        canvas.drawLine(mData[0], mData[1], mCtrl[14], mCtrl[15], mPaint)
    }

    // 绘制坐标系
    private fun drawCoordinateSystem(canvas: Canvas) {
        canvas.save()                      // 绘制做坐标系

        canvas.translate(mCenterX, mCenterY) // 将坐标系移动到画布中央
        canvas.scale(1F, -1F)                 // 翻转Y轴

        val fuzhuPaint = Paint()
        fuzhuPaint.setColor(Color.RED)
        fuzhuPaint.setStrokeWidth(5F)
        fuzhuPaint.setStyle(Paint.Style.STROKE)

        canvas.drawLine(0F, -2000F, 0F, 2000F, fuzhuPaint)
        canvas.drawLine(-2000F, 0F, 2000F, 0F, fuzhuPaint)

        canvas.restore()
    }
}
