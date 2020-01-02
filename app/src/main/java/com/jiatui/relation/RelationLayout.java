package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class RelationLayout extends ViewGroup {


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

    private int[] ANGLES = new int[8];

    public RelationLayout(Context context) {
        this(context, null);
    }

    public RelationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStrokeWidth(5);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        matrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(context,
                new MySimpleOnScaleGestureListener());
        changeScaleMin(context, mScaleGestureDetector);
        mScroller = new OverScroller(context);
        detector = new GestureDetectorCompat(context, new MyGestureListener());
        regions = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            addNext();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        double l = Math.hypot(w, h);//通过两条直角边算斜边长度
        // double angle1 = Math.toRadians(Math.atan2(h, w));
        int angle = (int) Math.toDegrees(Math.atan2(h, w));
        ANGLES[0] = angle;
        ANGLES[1] = 90;
        ANGLES[2] = 180 - angle;
        ANGLES[3] = 180;
        ANGLES[4] = 180 + angle;
        ANGLES[5] = 270;
        ANGLES[6] = 360 - angle;
        ANGLES[7] = 360;


    }

    private void changeScaleMin(Context context, ScaleGestureDetector detector) {
        // 调整最小跨度值。默认值27mm(>=sw600dp的32mm)，太大了，效果不好,如果不做处理会导致 缩放还没到达最小值事件响应给到了 移动
        Class clazz = ScaleGestureDetector.class;
        int newMinSpan = ViewConfiguration.get(context).getScaledTouchSlop();
        try {
            Field mMinSpanField = clazz.getDeclaredField("mMinSpan");
            mMinSpanField.setAccessible(true);
            mMinSpanField.set(detector, newMinSpan);
            mMinSpanField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (i == 0) {
                child.layout(l, t, r, b);
                if (regions.get(i) != null) {
                    regions.get(i).set(l, t, r, b);
                }
            } else {
                int[] nl = findNextLayout();
                child.layout(nl[0], nl[1], nl[2], nl[3]);
                if (regions.get(i) != null) {
                    regions.get(i).set(nl[0], nl[1], nl[2], nl[3]);
                }
            }
        }
    }

    private int[] findNextLayout() {
        int[] pos = new int[4];
        float w = getMeasuredWidth() / 2f;
        float h = getMeasuredHeight() / 2f;
        double l = Math.hypot(w, h);//通过两条直角边算斜边长度
        for (int angle : ANGLES) {
            int lineLength;
            if (angle % 180 == 0) { //水平
                lineLength = (int) w;
            } else if (angle % 90 == 0) {
                lineLength = (int) h;
            } else {
                lineLength = (int) l;
            }
            Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, lineLength * 2, angle);
            boolean notUsed = true;
            for (Region region : regions) {
                if (region.contains(point.x, point.y)) {
                    notUsed = false;
                    break;
                }
            }
            if (notUsed) {
                pos[0] = (int) (point.x - w);
                pos[1] = (int) (point.y - h);
                pos[2] = (int) (point.x + w);
                pos[3] = (int) (point.y + h);
                return pos;
            }
        }
        return pos;
    }

    private void addNext() {
        if (getContext() != null) {
            addView(new RelationView(getContext()));
            regions.add(new Region());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        int childCount = getChildCount();
        canvas.drawLine(0, 0, getMeasuredWidth(), 0, mPaint);
        canvas.drawLine(0, 0, 0, getMeasuredHeight(), mPaint);
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(0);
            int height = child.getMeasuredHeight();
            int width = child.getMeasuredWidth();
            // drawX
            canvas.drawLine(0, height * (i + 1), getMeasuredWidth() * childCount, height * (i + 1), mPaint);
            // drawY
            canvas.drawLine(width * (i + 1), 0, width * (i + 1), getMeasuredHeight() * childCount, mPaint);
            // Timber.d("onDraw.w:%s,h:%s", getMeasuredWidth(), height);
        }
        for (int angle : ANGLES) {
            int w = getMeasuredWidth();
            int h = getMeasuredHeight();
            int l = (int) Math.sqrt(w * w + h * h);

            int lineLength;
            if (angle % 180 == 0) { //水平
                lineLength = w;
            } else if (angle % 90 == 0) {
                lineLength = h;
            } else {
                lineLength = l;
            }
            Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, lineLength, angle);
            canvas.drawLine(w / 2, h / 2, point.x, point.y, mPaint);
        }
        // drawLine(canvas, getMeasuredHeight(), getMeasuredWidth());


        // Timber.d("onDraw...");
    }

    private void drawLine(Canvas canvas, int w, int h) {
        double angle1 = Math.toDegrees(Math.atan2(w, h));
        double lineLength = Math.hypot(w, h);//通过两条直角边算斜边长度
        Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, (int) lineLength, 180 - angle1);
        canvas.drawLine(getMeasuredWidth() / 2, getMeasuredHeight() / 2, point.x, point.y, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Timber.d("onTouchEvent");
        boolean result = mScaleGestureDetector.onTouchEvent(event);
        if (!mScaleGestureDetector.isInProgress()) {
            result = detector.onTouchEvent(event);
        }
        return result;
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float[] downPoint = new float[]{e.getX(), e.getY()};
            float[] invertPoint = new float[2];//逆变换后的点击点数组
            Matrix invertMatrix = new Matrix();//当前Matrix矩阵的逆矩阵
            matrix.invert(invertMatrix);//通过当前Matrix得到对应的逆矩阵数据
            invertMatrix.mapPoints(invertPoint, downPoint);//通过逆矩阵变化得到逆变换后的点击点
            Timber.d("onSingleTapUp down:%s ,invert:%s", Arrays.toString(downPoint), Arrays.toString(invertPoint));
            for (int i = 0; i < regions.size(); i++) {
                Region region = regions.get(i);
                if (region.contains(((int) invertPoint[0]), ((int) invertPoint[1]))) {
                    Timber.d("onSingleTapUp[%s]", i);
                    break;
                }
            }

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= distanceX;
            offsetY -= distanceY;
            matrix.postTranslate(-distanceX, -distanceY);
            // scrollBy(((int) distanceX), ((int) distanceY));
            invalidate();
            Timber.d("onScroll dx=%s , dy=%s", offsetX, offsetY);
            return true;
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
        private float mScale = 1;
        private static final float MAX_SCALE = 2f;
        private static final float MIN_SCALE = .1f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mScale *= detector.getScaleFactor();
            mScale = Math.max(MIN_SCALE, mScale);
            mScale = Math.min(MAX_SCALE, mScale);
            if (mScale > MIN_SCALE && mScale < MAX_SCALE) {
                matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());
                invalidate();
                Timber.d("onScale...focusX=%f, focusY=%f, scaleFactor=%f ,mScale=%s",
                        detector.getFocusX(), detector.getFocusY(), scaleFactor, mScale);
                return true;
            }
            return false;

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
