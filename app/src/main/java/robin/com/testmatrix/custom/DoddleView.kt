package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class DoddleView : View {

    companion object {
        private val TOUCH_TOLERANCE = 4f
        // 保存Path路径的集合,用List集合来模拟栈
    }

    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private var mPath: Path? = Path()
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)// 画布的画笔
    private val mPaint = Paint()// 真实的画笔
    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()// 临时点坐标
    // 记录Path路径的对象
    private var dp = DrawPath()

    private var savePath: MutableList<DrawPath>? = ArrayList()

    private inner class DrawPath {
        var path: Path? = null// 路径
        var paint: Paint? = null// 画笔
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private fun init() {
        mBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        // 保存一次一次绘制出来的图形
        mCanvas = Canvas(mBitmap)
        mPaint.setAntiAlias(true)
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setStrokeJoin(Paint.Join.ROUND)// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.SQUARE)// 形状
        mPaint.setStrokeWidth(5f)// 画笔宽度
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(-0x555556)
        // 将前面已经画过得显示出来
        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint)
        }
    }

    private fun touch_start(x: Float, y: Float) {
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(mY - y)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        mPath!!.lineTo(mX, mY)
        mCanvas.drawPath(mPath, mPaint)
        //将一条完整的路径保存下来(相当于入栈操作)
        savePath!!.add(dp)
        mPath = null// 重新置空
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    fun undo() {
        mBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        mCanvas.setBitmap(mBitmap)// 重新设置画布，相当于清空画布
        // 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
        if (savePath != null && savePath!!.size > 0) {
            // 移除最后一个path,相当于出栈操作
            savePath!!.removeAt(savePath!!.size - 1)
            val iter = savePath!!.iterator()
            while (iter.hasNext()) {
                val drawPath = iter.next()
                mCanvas.drawPath(drawPath.path, drawPath.paint)
            }
            invalidate()// 刷新

            /*在这里保存图片纯粹是为了方便,保存图片进行验证*/
            val fileUrl = Environment.getExternalStorageDirectory()
                    .toString() + "/android/data/test.png"
            try {
                val fos = FileOutputStream(File(fileUrl))
                mBitmap!!.compress(CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 重做的核心思想就是将撤销的路径保存到另外一个集合里面(栈)，
     * 然后从redo的集合里面取出最顶端对象，
     * 画在画布上面即可。
     */
    fun redo() {
        //如果撤销你懂了的话，那就试试重做吧。
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 每次down下去重新new一个Path
                mPath = Path()
                //每一次记录的路径对象是不一样的
                dp = DrawPath()
                dp!!.path = mPath
                dp!!.paint = mPaint
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
        }
        return true
    }
}
