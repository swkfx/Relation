package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import timber.log.Timber;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2019/12/28
 *      desc   :
 * </pre>
 */
public class RelationView extends View {
    private static final String TAG = "RelationView";
    private Paint mPaint;
    private Point centerPoint;
    private int radius = dp2px(50);
    private int lineLength = dp2px(350);
    private int lineCount = 8;
    private TextPaint mTextPaint;
    private float mScale = 1;
    private Matrix matrix;
    private int mLastX, mLastY;


    public RelationView(Context context) {
        this(context, null);
    }

    public RelationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Timber.plant(new Timber.DebugTree());
        init();
    }

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;


    private void init() {
        setClickable(true);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dp2px(18));
        mTextPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(dp2px(1));
        centerPoint = new Point();
        matrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();
                        mScale *= scaleFactor;
                        matrix.setScale(mScale, mScale, detector.getFocusX(), detector.getFocusY());
                        invalidate();
                        Timber.d("onScale...focusX=%f, focusY=%f, scaleFactor=%f",
                                detector.getFocusX(), detector.getFocusY(), scaleFactor);
                        return super.onScale(detector);

                    }
                });

        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        centerPoint.x = cx;
        centerPoint.y = cy;
        canvas.drawCircle(cx, cy, radius, mPaint);

        for (int i = 1; i <= lineCount; i++) {
            double angle = 360 / lineCount;
            Point point = RelationUtils.calcPointWithAngle(centerPoint, lineLength, angle * i);
            canvas.drawLine(cx, cy, point.x, point.y, mPaint);
            canvas.drawCircle(point.x, point.y, radius / 2, mPaint);
            canvas.drawText(String.valueOf(i), point.x, point.y, mTextPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        final int actionMasked = event.getActionMasked();
        final int action = event.getAction();
        final int pointerCount = event.getPointerCount();
        Timber.d("onTouchEvent: isScaling=%b, actionIndex=%d, pointerId=%d, actionMasked=%d, " +
                        "action=%d, pointerCount=%d",
                false, actionIndex, pointerId, actionMasked, action, pointerCount);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (event.getX() - mLastX);
                int dy = (int) (event.getY() - mLastY);
                matrix.setTranslate(dx, dy);
                invalidate();
                break;
        }
        return mScaleGestureDetector.onTouchEvent(event) && super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            matrix.setTranslate(mScroller.getCurrX() - mScroller.getStartX(),
                    mScroller.getCurrY() - mScroller.getStartY());
            invalidate();
        }
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + .5f);
    }
}
