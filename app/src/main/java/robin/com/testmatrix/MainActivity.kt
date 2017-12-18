package robin.com.testmatrix

import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import robin.com.testmatrix.imageedit.ImageEditView
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    lateinit var btnUndo: Button
    lateinit var imageEditView: ImageEditView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        testMapPoints()
//        testMapRect()
        btnUndo = findViewById<Button>(R.id.btnUndo)
        imageEditView = findViewById<ImageEditView>(R.id.imageEditView)

        btnUndo.setOnClickListener { v -> imageEditView.undo() }
    }

    private fun testMapPoints() {
        // 初始数据为三个点 (0, 0) (80, 100) (400, 300)
        val src = floatArrayOf(0f, 0f, 80f, 100f, 400f, 300f)
        val dst1 = FloatArray(6)

        // 构造一个matrix，x坐标缩放0.5
        val matrix = Matrix()
        matrix.setScale(0.5f, 1f)

        // 输出计算之前数据
        Log.i(TAG, "before: src= ${Arrays.toString(src)}")
        Log.i(TAG, "before: dst= ${Arrays.toString(dst1)}")

        // 调用map方法计算(最后一个2表示两个点，即四个数值,并非两个数值)
        matrix.mapPoints(dst1, 0, src, 2, 2)

        // 输出计算之后数据
        Log.i(TAG, "matrix.mapPoints(dst, 0, src, 2, 2)")
        Log.i(TAG, "before: src= ${Arrays.toString(src)}")
        Log.i(TAG, "before: dst= ${Arrays.toString(dst1)}")

        val dst2 = FloatArray(6)

        // 调用map方法计算(最后一个2表示两个点，即四个数值,并非两个数值)
        matrix.mapPoints(dst2, 1, src, 2, 2)

        // 输出计算之后数据
        Log.i(TAG, "matrix.mapPoints(dst, 1, src, 2, 2)")
        Log.i(TAG, "before: src= ${Arrays.toString(src)}")
        Log.i(TAG, "before: dst= ${Arrays.toString(dst2)}")

        val dst3 = FloatArray(6)

        // 调用map方法计算(最后一个2表示两个点，即四个数值,并非两个数值)
        matrix.mapPoints(dst3, 2, src, 2, 2)

        // 输出计算之后数据
        Log.i(TAG, "matrix.mapPoints(dst, 1, src, 2, 2)")
        Log.i(TAG, "before: src= ${Arrays.toString(src)}")
        Log.i(TAG, "before: dst= ${Arrays.toString(dst3)}")
    }

    // boolean mapRect (RectF dst, RectF src) 测量src并将测量结果放入dst中，返回值是判断矩形经过变换后是否仍为矩形
    private fun testMapRect() {
        val rect = RectF(400f, 400f, 1000f, 800f)

        // 构造一个matrix
        val matrix = Matrix()
        matrix.setScale(0.5f, 1f)
//            matrix.postSkew(1f, 0f)

        Log.i(TAG, "mapRadius: ${rect.toString()}")

        val result = matrix.mapRect(rect)

        Log.i(TAG, "mapRadius: ${rect.toString()}")
        Log.e(TAG, "isRect: $result")

        matrix.postSkew(1F, 0F)

        val result1 = matrix.mapRect(rect)
        Log.e(TAG, "isRect: $result1")   // 由于使用了错切，所以返回结果为false
    }
}
