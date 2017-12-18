package robin.com.testmatrix.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Created by Robin Yang on 12/1/17.
 */
class PathOperate : View {

    private val paint1 = Paint()
    private val paint2 = Paint()
    private val paint3 = Paint()
    private val paint4 = Paint()

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        paint1.setStyle(Paint.Style.FILL)

        paint2.setStyle(Paint.Style.FILL)
        paint1.color = Color.BLUE

        paint3.setStyle(Paint.Style.FILL)
        paint1.color = Color.YELLOW

        paint4.setStyle(Paint.Style.FILL)
        paint1.color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width / 2F, height / 2F)

        val path1 = Path()
        val path2 = Path()
        val path3 = Path()
        val path4 = Path()

        path1.addCircle(0F, 0F, 200F, Path.Direction.CW)
        path2.addRect(0F, -200F, 200F, 200F, Path.Direction.CW)
        path3.addCircle(0F, -100F, 100F, Path.Direction.CW)
        path4.addCircle(0F, 100F, 100F, Path.Direction.CCW)


        path1.op(path2, Path.Op.DIFFERENCE)
        path1.op(path3, Path.Op.UNION)
        path1.op(path4, Path.Op.DIFFERENCE)

        canvas.drawPath(path1, paint1)
//        canvas.drawPath(path2, paint2)
//        canvas.drawPath(path3, paint3)
//        canvas.drawPath(path4, paint4)
    }
}