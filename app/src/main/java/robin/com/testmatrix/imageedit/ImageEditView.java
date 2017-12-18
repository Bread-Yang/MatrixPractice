package robin.com.testmatrix.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

import robin.com.testmatrix.R;
import robin.com.testmatrix.view.utils.DensityUtils;

/**
 * Created by Robin Yang on 12/7/17.
 */

public class ImageEditView extends View {

    private static String TAG = "ImageEditView";

    /**
     * 编辑模式
     */
    public enum EditMode {
        NONE,
        GRAFFTI, // 涂鸦模式
        STICKER, // 贴图模式
    }

    private int mThreshold; // 滑动临界值 : 20dp，超过20dp，则为scale手势

    private boolean mIsScaleable;
    private boolean mIsPaintable;
    private boolean mIsEditable; // 当前是否可以编辑，多点触控
    private EditMode mEditMode;
    private Bitmap mBitmap;
    private Bitmap mEditBitmap; // 用绘制涂鸦的图片
    private Canvas mEditBitmapCanvas;

    private int mOriginalWidth, mOriginalHeight; // 初始图片的尺寸
    private float mOriginalPivotX, mOriginalPivotY; // 图片中心

    private float mAdaptScale; // 图片适应屏幕时的缩放倍数
    private int mAdaptHeight, mAdaptWidth;// 图片适应屏幕时的大小（View窗口坐标系上的大小）
    private float mCentreAdaptTranX, mCentreAdaptTranY;

    private final float mMaxScale = 4f; // 最大缩放倍数
    private final float mMinScale = 0.25f; // 最小缩放倍数
    private float mScale = 1; // 在适应屏幕时的缩放基础上的缩放倍数 （ 图片真实的缩放倍数为 mAdaptScale * mScale ）
    private float mTransX = 0, mTransY = 0; // 图片在适应屏幕且处于居中位置的基础上的偏移量（ 图片真实偏移量为mCentreAdaptTranX + mTransX，View窗口坐标系上的偏移）

    private Path mCurrentPath; // 当前手写的路径

    private Paint mPaint;
    private float mPaintStrokeWidth;
    private int mPaintColor;

    private float mFirstPointerDownX, mFirstPointerDownY, mFirstPointerMoveX, mFirstPointerMoveY, mLastTouchX, mLastTouchY;

    // 保存涂鸦操作，用于撤销
    private CopyOnWriteArrayList<UndoPath> mUndoStack = new CopyOnWriteArrayList<>();

    // 手势缩放监听
    private OnScaleGestureListener mListener;

    private ScaleGestureDetector mScaleDetector;

    // 判断触控的是否是第一根手指
    private boolean mIsFirstPoint = false;

    public ImageEditView(Context context, Bitmap bitmap) {
        super(context);
        mBitmap = bitmap;
        init();
    }

    public ImageEditView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.clean_architecture_evolution);

        init();
    }

    private void init() {
        mThreshold = DensityUtils.dip2px(getContext(), 20);

        // 关闭硬件加速，因为bitmap的Canvas不支持硬件加速
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        mOriginalWidth = mBitmap.getWidth();
        mOriginalHeight = mBitmap.getHeight();
        mOriginalPivotX = mOriginalWidth / 2f;
        mOriginalPivotY = mOriginalHeight / 2f;

        mScale = 1f;

        // 初始画笔
        mPaint = new Paint();
        mPaintStrokeWidth = 11;
        mPaintColor = Color.RED;
        mPaint.setStrokeWidth(mPaintStrokeWidth);
        mPaint.setColor(mPaintColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);   // 圆滑

        mEditMode = EditMode.GRAFFTI;

        mListener = new OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.e(TAG, "onScale");
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                float relativeX = toX(focusX);
                float relativeY = toY(focusY);

                // 缩放图片
                float scale = mScale * detector.getScaleFactor();

                if (scale > mMaxScale) {
                    scale = mMaxScale;
                } else if (scale < mMinScale) {
                    scale = mMinScale;
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                Log.e(TAG, "onScaleBegin");

                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                Log.e(TAG, "onScaleEnd");
            }
        };

        mScaleDetector = new ScaleGestureDetector(getContext(), mListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBG();
    }

    private void setBG() {
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            mAdaptScale = 1 / nw;
            mAdaptWidth = getWidth();
            mAdaptHeight = (int) (h * mAdaptScale);
        } else {
            mAdaptScale = 1 / nh;
            mAdaptWidth = (int) (w * mAdaptScale);
            mAdaptHeight = getHeight();
        }
        // 使图片居中
        mCentreAdaptTranX = (getWidth() - mAdaptWidth) / 2f;
        mCentreAdaptTranY = (getHeight() - mAdaptHeight) / 2f;

        initCanvas();
    }

    private void initCanvas() {
        if (mEditBitmap != null) {
            mEditBitmap.recycle();
        }
        mEditBitmap = mBitmap.copy(Bitmap.Config.RGB_565, true);
        mEditBitmapCanvas = new Canvas(mEditBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int index = event.getActionIndex();
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mFirstPointerDownX = mLastTouchX = touchX;
                mFirstPointerDownY = mLastTouchY = touchY;

                if (mEditMode == EditMode.GRAFFTI) {
                    mCurrentPath = new Path();
                    mCurrentPath.moveTo(toX(mFirstPointerDownX), toY(mFirstPointerDownY));
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (!isExceedThreshold(mFirstPointerDownX, mFirstPointerDownY, mFirstPointerMoveX, mFirstPointerMoveY)) {
                    mIsScaleable = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerId(index) == 0) {  // 第一根手指滑动的位置
                        mFirstPointerMoveX = touchX;
                        mFirstPointerMoveY = touchY;
                }
                if (mIsScaleable) {
                    if (event.getPointerCount() >= 2) {
                        mScaleDetector.onTouchEvent(event);
                    } else {

                    }
                } else {
                    switch (mEditMode) {
                        case GRAFFTI:
                            if (event.getPointerId(index) == 0) {  // 检测第一个手指滑动的位置
                                mCurrentPath.quadTo(
                                        toX(mLastTouchX),
                                        toY(mLastTouchY),
                                        toX((touchX + mLastTouchX) / 2),
                                        toY((touchY + mLastTouchY) / 2));
                            }
                            if (!mIsPaintable && isExceedThreshold(touchX, touchY, mFirstPointerDownX, mFirstPointerDownY)) {
                                mIsPaintable = true;
                            }
                            mLastTouchX = touchX;
                            mLastTouchY = touchY;
                            if (mIsPaintable) {
                                drawPath(mCurrentPath, mPaint);
                                invalidate();
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsScaleable) {

                } else {
                    if (mEditMode == EditMode.GRAFFTI) {
                        mCurrentPath.quadTo(
                                toX(mLastTouchX),
                                toY(mLastTouchY),
                                toX((touchX + mLastTouchX) / 2),
                                toY((touchY + mLastTouchY) / 2));

                        // 保存到撤销栈中
                        UndoPath newUndoPath = new UndoPath(mPaintStrokeWidth, mPaintColor, mCurrentPath);
                        mUndoStack.add(newUndoPath);
                        if (mIsPaintable) {
                            drawPath(mCurrentPath, mPaint);
                            invalidate();
                        }
                        mIsPaintable = false;
                    }
                }
                mIsScaleable = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }

        return true;
    }

    private void drawPath(Path path, Paint paint) {
        mEditBitmapCanvas.drawPath(path, paint);
    }

    // 还原推栈中的path操作
    private void drawPathStack() {
        for (UndoPath undoPath : mUndoStack) {
            mPaint.setColor(undoPath.color);
            mPaint.setStrokeWidth(undoPath.strokeWidth);

            drawPath(undoPath.path, mPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        float left = mCentreAdaptTranX + mTransX;
        float top = mCentreAdaptTranY + mTransY;

        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.translate(left, top); // 偏移画布
        canvas.scale(mAdaptScale * mScale, mAdaptScale * mScale); // 缩放画布

        canvas.drawBitmap(mEditBitmap, 0, 0, null);

        canvas.restore();
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mCentreAdaptTranX - mTransX) / (mAdaptScale * mScale);
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mCentreAdaptTranY - mTransY) / (mAdaptScale * mScale);
    }

    /**
     * 将图片坐标x转换成屏幕触摸坐标
     */
    public final float toTouchX(float x) {
        return x * ((mAdaptScale * mScale)) + mCentreAdaptTranX + mTransX;
    }

    /**
     * 将图片坐标y转换成屏幕触摸坐标
     */
    public final float toTouchY(float y) {
        return y * ((mAdaptScale * mScale)) + mCentreAdaptTranY + mTransY;
    }

    public void setEditMode(@NonNull EditMode editMode) {
        this.mEditMode = editMode;
    }

    public void undo() {
        if (mUndoStack.size() > 0) {
            mUndoStack.remove(mUndoStack.size() - 1);

            initCanvas();
            drawPathStack();
            invalidate();
        }
    }

    private boolean isExceedThreshold(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) >= mThreshold;
    }
}
