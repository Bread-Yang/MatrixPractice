package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View



/**
 * Created by Robin Yang on 11/30/17.
 */
class TestPathView : View {

    val mPaint = Paint()    // 创建画笔

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint.setColor(Color.BLACK)           // 画笔颜色 - 黑色
        mPaint.setStyle(Paint.Style.STROKE)    // 填充模式 - 描边
        mPaint.setStrokeWidth(10F)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate(width / 2F, height / 2F)  // 移动坐标系到屏幕中心

        val path = Path()

//        path.addRect(200F, 200F, -200F, -200F, Path.Direction.CW)
        path.addRect(-200F, -200F, 200F, 200F, Path.Direction.CW)

//        path.setLastPoint(300F,-300F);

        canvas.drawPath(path, mPaint)
    }
}