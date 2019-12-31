package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RelationLayout extends ViewGroup {

    private float mScale = 1;

    private Matrix matrix;
    private Paint mPaint;

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private OverScroller mScroller;
    private GestureDetectorCompat detector;
    private float offsetX;
    private float offsetY;

    private List<Region> regions;

    public RelationLayout(Context context) {
        this(context, null);
    }

    public RelationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStrokeWidth(10);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        matrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(),
                new MySimpleOnScaleGestureListener());
        mScroller = new OverScroller(context);
        detector = new GestureDetectorCompat(context, new MyGestureListener());
        regions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            addView(new RelationView2(context, attrs, defStyle));
            regions.add(new Region());
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // int scaleWidth = (int) (widthSize * mScale);
        // int scaleHeight = (int) (heightSize * mScale);
        // for (int i = 0; i < getChildCount(); i++) {
        //     View child = getChildAt(i);
        //     // 2.计算自定义的ViewGroup中所有子控件的大小
        //     measureChild(child, MeasureSpec.makeMeasureSpec(scaleWidth, MeasureSpec.EXACTLY),
        //             MeasureSpec.makeMeasureSpec(scaleHeight, MeasureSpec.EXACTLY));
        //     Timber.d("onMeasure :widthSize=%s, heightSize=%s, scaleWidth=%s, scaleHeight=%s", widthSize, heightSize, scaleWidth, scaleHeight);
        //     // height = Math.max(height, measuredHeight);
        // }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(r * i, t, r * (i + 1), b);
            // int left = (int) (child.getMeasuredWidth() * i * mScale);
            // int top = 0;
            // int right = (int) (child.getMeasuredWidth() * (i + 1) * mScale);
            // int bottom = (int) (child.getMeasuredHeight() * mScale);
            // // child.layout((int) (right * i * mScale), (int) (top * mScale), (int) (right * (i + 1) * mScale), (int) (bottom * mScale));
            // child.layout(left, top, right, bottom);
            // Timber.d("onLayout:[%s] => l:%s,t:%s,r:%s,b:%s", i, left, top, right, bottom);
            // if (regions.get(i) != null) {
            //     regions.get(i).set(r * i, t, r * (i + 1), b);
            // }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(0);
            int height = child.getMeasuredHeight();
            int width = child.getMeasuredWidth();
            // drawX
            canvas.drawLine(0, height * (i + 1), getMeasuredWidth(), height * (i + 1), mPaint);
            // drawY
            canvas.drawLine(width * (i + 1), 0, width * (i + 1), getMeasuredHeight(), mPaint);
            // Timber.d("onDraw.w:%s,h:%s", getMeasuredWidth(), height);
        }
        // Timber.d("onDraw...");
    }


    // @Override
    // public void computeScroll() {
    //     if (mScroller.computeScrollOffset()) {
    //         scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
    //         postInvalidate();
    //     }
    // }


    // @Override
    // public boolean dispatchTouchEvent(MotionEvent ev) {
    //     boolean result = mScaleGestureDetector.onTouchEvent(ev);
    //     if (!mScaleGestureDetector.isInProgress()) {
    //         result = detector.onTouchEvent(ev);
    //     }
    //     return result;
    //
    // }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Timber.d("onTouchEvent");
        boolean result = mScaleGestureDetector.onTouchEvent(event);
        if (!mScaleGestureDetector.isInProgress()) {
            result = detector.onTouchEvent(event);
        }
        return result;
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Timber.d("onSingleTapConfirmed:x=%s,y=%s", e.getX(), e.getY());
            // int x = (int) (e.getX() + offsetX);
            // int y = (int) (e.getY() + offsetY);
            // Timber.d("onSingleTapConfirmed+offset:x=%s,y=%s", x, y);
            // // for (Region region : regions) {
            // //     if (region.contains(((int) e.getX()), ((int) e.getY()))) {
            // //         Timber.d("onClick:%s", regions.indexOf(region));
            // //         break;
            // //     }
            // // }
            // for (int i = 0; i < getChildCount(); i++) {
            //     if (regions.get(i).contains(x, y)) {
            //         Timber.d("onClick Child : [%s]", i);
            //         break;
            //     }
            //
            //     View child = getChildAt(i);
            //     child.dispatchTouchEvent(e);
            //     Timber.d("SingleTap[%s]:l=%s,t=%s,r=%s,b=%s", i, child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            // }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Timber.d("onSingleTapUp");
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Timber.d("onScroll");
            offsetX += distanceX;
            offsetY += distanceY;
            scrollBy(((int) distanceX), ((int) distanceY));
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }


    private class MySimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float initialScale;
        private float mMaxScale = 2;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mScale *= detector.getScaleFactor();
            matrix.setScale(mScale, mScale);
            // for (int i = 0; i < getChildCount(); i++) {
            //     // getChildAt(i).setScaleX(mScale);
            //     // getChildAt(i).setScaleY(mScale);
            //     ViewHelper.setScaleX( getChildAt(i), mScale);// x方向上缩小
            //     ViewHelper.setScaleY( getChildAt(i), mScale);// y方向上缩小
            // }

            // setScaleX(mScale);
            // setScaleY(mScale);
            // 缩放view
            // ViewHelper.setScaleX(RelationLayout.this, mScale);// x方向上缩小
            // ViewHelper.setScaleY(RelationLayout.this, mScale);// y方向上缩小

            // requestLayout();
            invalidate();
            Timber.d("onScale...focusX=%f, focusY=%f, scaleFactor=%f ,mScale=%s",
                    detector.getFocusX(), detector.getFocusY(), scaleFactor, mScale);


            // // 缩放因子，>0表示正在放大，<0表示正在缩小
            // float intentScale = detector.getScaleFactor();
            // float scale = getScale();
            //
            // // 进行缩放范围的控制
            // // 判断，如果<最大缩放值，表示可以放大，如果》最小缩放，说明可以缩小
            // // scale 变小时， intentScale变小
            // if (scale * intentScale < initialScale) {
            //     // intentScale * scale = mInitScale ;
            //     intentScale = initialScale / scale;
            // }
            //
            // // scale 变大时， intentScale变大
            // if (scale * intentScale > mMaxScale) {
            //     // intentScale * scale = mMaxScale ;
            //     intentScale = mMaxScale / scale;
            // }
            //
            // // 以控件为中心缩放
            // // mMatrix.postScale(intentScale, intentScale, getWidth()/2,
            // // getHeight()/2);
            // // 以手势为中心缩放
            // matrix.postScale(intentScale, intentScale, detector.getFocusX(), detector.getFocusY());
            //
            // // 检测边界与中心点
            // // checkSideAndCenterWhenScale();
            // invalidate();
            return true;

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initialScale = mScale;
            matrix.setScale(mScale, mScale);
            return true;
        }
    }


    /**
     * 获得缩放值
     *
     * @return
     */
    public float getScale() {
        /**
         * xscale xskew xtrans yskew yscale ytrans 0 0 0
         */
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }
}
