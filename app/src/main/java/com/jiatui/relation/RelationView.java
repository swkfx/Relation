package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

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
    private int lineLength = dp2px(150);
    private int lineCount = 8;
    private TextPaint mTextPaint;
    private float mScale = 1;
    private float initialScale = 1;
    private Matrix matrix;
    private float offsetX;
    private float offsetY;

    private int viewId;

    private List<Region> childRegions;

    private Region parentRegion;

    private Point expandPoint;//在父容器的连线起点
    private Point parentPoint;//在父容器的layout 的中心店

    public RelationView(Context context) {
        this(context, null);
    }

    public RelationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Timber.plant(new Timber.DebugTree());
        init(context);
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private OverScroller mScroller;
    private GestureDetectorCompat detector;


    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dp2px(18));
        mTextPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(dp2px(1));
        centerPoint = new Point();
        matrix = new Matrix();
        // mScaleGestureDetector = new ScaleGestureDetector(getContext(),
        //         new MySimpleOnScaleGestureListener());
        mScroller = new OverScroller(context);
        detector = new GestureDetectorCompat(context, new MyGestureListener());
        childRegions = new ArrayList<>();
        for (int i = 0; i < lineCount + 1; i++) {
            childRegions.add(new Region());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        centerPoint.x = cx;
        centerPoint.y = cy;
        canvas.drawCircle(cx, cy, radius, mPaint);
        if (childRegions.get(0) != null) {
            childRegions.get(0).set(cx - radius, cy - radius, cx + radius, cy + radius);
        }
        for (int i = 1; i <= lineCount; i++) {
            double angle = 360 / lineCount;
            Point point = RelationUtils.calcPointWithAngle(centerPoint, lineLength, angle * i);
            canvas.drawLine(cx, cy, point.x, point.y, mPaint);
            int smallRadius = this.radius / 2;
            canvas.drawCircle(point.x, point.y, smallRadius, mPaint);
            canvas.drawText(String.valueOf(i), point.x, point.y, mTextPaint);
            if (childRegions.get(i) != null) {
                childRegions.get(i).set(point.x - smallRadius, point.y - smallRadius,
                        point.x + smallRadius, point.y + smallRadius);
            }
        }
    }

    public Rect getChildRect(int x, int y) {
        Rect rect = new Rect();
        for (Region region : childRegions) {
            if (region.contains(x, y)) {
                Timber.d("onClick:%s", childRegions.indexOf(region));
                rect.set(region.getBounds());
                return rect;
            }
        }
        return null;
    }

    // @Override
    //     // public boolean dispatchTouchEvent(MotionEvent event) {
    //     //     boolean result = mScaleGestureDetector.onTouchEvent(event);
    //     //     if (!mScaleGestureDetector.isInProgress()) {
    //     //         result = detector.onTouchEvent(event);
    //     //     }
    //     //     return result;
    //     //     // return super.dispatchTouchEvent(event);
    //     // }

    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    //     boolean result = mScaleGestureDetector.onTouchEvent(event);
    //     if (!mScaleGestureDetector.isInProgress()) {
    //         result = detector.onTouchEvent(event);
    //     }
    //     return result;
    // }


    // @Override
    // public boolean onTouchEvent(MotionEvent event) {
    //     final int actionIndex = event.getActionIndex();
    //     int pointerId = event.getPointerId(actionIndex);
    //     final int actionMasked = event.getActionMasked();
    //     final int action = event.getAction();
    //     final int pointerCount = event.getPointerCount();
    //     Timber.d("onTouchEvent: actionIndex=%d, pointerId=%d, actionMasked=%d, action=%d, pointerCount=%d",
    //             actionIndex, pointerId, actionMasked, action, pointerCount);
    //     if (mVelocityTracker == null) {
    //         mVelocityTracker = VelocityTracker.obtain();
    //     }
    //     mVelocityTracker.addMovement(event);
    //     switch (actionMasked) {
    //         case MotionEvent.ACTION_DOWN:
    //             mLastX = (int) event.getX();
    //             mLastY = (int) event.getY();
    //             break;
    //         case MotionEvent.ACTION_MOVE:
    //             int dx = (int) (event.getX() - mLastX);
    //             int dy = (int) (event.getY() - mLastY);
    //             matrix.setTranslate(dx, dy);
    //             invalidate();
    //             break;
    //         case MotionEvent.ACTION_UP:
    //         case MotionEvent.ACTION_CANCEL:
    //             break;
    //
    //     }
    //     return mScaleGestureDetector.onTouchEvent(event) && super.onTouchEvent(event);
    // }


    private class MyGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Timber.d("onSingleTapConfirmed:x=%s,y=%s", e.getX(), e.getY());
            for (Region region : childRegions) {
                if (region.contains(((int) e.getX()), ((int) e.getY()))) {
                    Timber.d("onClick:%s", childRegions.indexOf(region));
                    break;
                }
            }
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
            return false;
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
            // offsetX -= distanceX;
            // offsetY -= distanceY;
            // matrix.setTranslate(offsetX, offsetY);
            // invalidate();
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

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + .5f);
    }

    private class MySimpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mScale = initialScale * detector.getScaleFactor();
            // setScaleX(mScale);
            // setScaleY(mScale);
            // matrix.setScale(mScale, mScale);
            // invalidate();
            Timber.d("onScale...focusX=%f, focusY=%f, scaleFactor=%f ,mScale=%s",
                    detector.getFocusX(), detector.getFocusY(), scaleFactor, mScale);
            return super.onScale(detector);

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initialScale = mScale;
            // matrix.setScale(mScale, mScale);
            return true;
        }
    }

    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
    }

    public Region getParentRegion() {
        return parentRegion;
    }

    public Point getExpandPoint() {
        return expandPoint;
    }

    public void setExpandPoint(Point expandPoint) {
        this.expandPoint = expandPoint;
    }

    public Point getParentPoint() {
        return parentPoint;
    }

    public void setParentPoint(Point parentPoint) {
        this.parentPoint = parentPoint;
    }
}
