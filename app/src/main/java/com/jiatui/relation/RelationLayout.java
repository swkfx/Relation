package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<RelationView> expandNodes = new ArrayList<>();

    private int[] EXPAND_ANGLES = new int[8];
    private RelationView root;
    private Map<String, PointZ> map = new HashMap<>();

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

        addRoot();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int angle = (int) Math.toDegrees(Math.atan2(h, w));
        EXPAND_ANGLES[0] = angle;
        EXPAND_ANGLES[1] = 90;
        EXPAND_ANGLES[2] = 180 - angle;
        EXPAND_ANGLES[3] = 180;
        EXPAND_ANGLES[4] = 180 + angle;
        EXPAND_ANGLES[5] = 270;
        EXPAND_ANGLES[6] = 360 - angle;
        EXPAND_ANGLES[7] = 360;

        if (root != null) {
            root.setParentPoint(new Point(w / 2, h / 2));
            root.setParentRegion(new Region(0, 0, w, h));
        }
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
            if (child == root) {
                child.layout(l, t, r, b);
                if (regions.get(i) != null) {
                    regions.get(i).set(l, t, r, b);
                    root.setParentRegion(regions.get(i));
                }
            } else {
                Rect bounds = ((RelationView) child).getParentRegion().getBounds();
                child.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
            }
        }
    }

    /**
     * 以参数为中心点寻找周围下一个可布局的中心点
     *
     * @param centerX 中心点x
     * @param centerY 中心点y
     * @return 周围下一个可布局的 位置的中心点
     */
    private Point findNextLayoutPoint(int centerX, int centerY) {
        float w = getMeasuredWidth() / 2f;
        float h = getMeasuredHeight() / 2f;
        double l = Math.hypot(w, h);//通过两条直角边算斜边长度
        for (int angle : EXPAND_ANGLES) {
            int lineLength;
            if (angle % 180 == 0) { //水平
                lineLength = (int) w;
            } else if (angle % 90 == 0) {
                lineLength = (int) h;
            } else {
                lineLength = (int) l;
            }
            Point point = RelationUtils.calcPointWithAngle(centerX, centerY, lineLength * 2, angle);
            boolean notUsed = true;
            for (Region region : regions) {
                if (region.contains(point.x, point.y)) {
                    notUsed = false;
                    break;
                }
            }
            if (notUsed) {
                return point;
            }
        }
        return null;
    }

    // private int[] findNextLayout() {
    //     int[] pos = new int[4];
    //     float w = getMeasuredWidth() / 2f;
    //     float h = getMeasuredHeight() / 2f;
    //     double l = Math.hypot(w, h);//通过两条直角边算斜边长度
    //     for (int angle : EXPAND_ANGLES) {
    //         int lineLength;
    //         if (angle % 180 == 0) { //水平
    //             lineLength = (int) w;
    //         } else if (angle % 90 == 0) { //垂直
    //             lineLength = (int) h;
    //         } else { //其他情况
    //             lineLength = (int) l;
    //         }
    //         Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, lineLength * 2, angle);
    //         boolean notUsed = true;
    //         for (Region region : regions) {
    //             if (region.contains(point.x, point.y)) {
    //                 notUsed = false;
    //                 break;
    //             }
    //         }
    //         if (notUsed) {
    //             pos[0] = (int) (point.x - w);
    //             pos[1] = (int) (point.y - h);
    //             pos[2] = (int) (point.x + w);
    //             pos[3] = (int) (point.y + h);
    //             return pos;
    //         }
    //     }
    //     return pos;
    // }

    private void addRoot() {
        if (getContext() != null) {
            root = new RelationView(getContext());
            addView(root);
            regions.add(new Region());
        }
    }

    private int id = 10000009;
    private final static int DRAW_COUNT = 30;

    /**
     * 添加一个下级节点
     *
     * @param upNode  上级节点 ，
     * @param clickPt 点击坐标 ，
     */
    private void addNext(RelationView upNode, int[] clickPt) {
        Rect nodeChildRect = upNode.getChildRect(clickPt[0] - upNode.getLeft(), clickPt[1] - upNode.getTop());
        if (nodeChildRect != null && !nodeChildRect.isEmpty()) {
            Rect bounds = upNode.getParentRegion().getBounds();
            int wR = upNode.getMeasuredWidth() / 2;
            int hR = upNode.getMeasuredHeight() / 2;
            Point point = findNextLayoutPoint(bounds.centerX(), bounds.centerY());
            if (point != null) {
                final RelationView nextNode = new RelationView(getContext());
                nextNode.setViewId(id ++);
                nextNode.setExpandPoint(new Point(clickPt[0], clickPt[1]));
                nextNode.setParentPoint(point);
                Region parentRegion = new Region();
                parentRegion.set(point.x - wR, point.y - hR, point.x + wR, point.y + hR);
                nextNode.setParentRegion(parentRegion);
                addView(nextNode);
                expandNodes.add(nextNode);
                regions.add(parentRegion);
            }
        }
    }

    private class PointZ {
        int originX;
        int originY;
        int count;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        canvas.drawColor(Color.BLACK);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        // drawDebugLine(canvas);
        super.onDraw(canvas);
        if (expandNodes != null && !expandNodes.isEmpty()) {
            for (RelationView node : expandNodes) {
                // 展开子节点
                Point expandPoint = node.getExpandPoint();
                Point parentPoint = node.getParentPoint();
                int startX = expandPoint.x;
                int startY = expandPoint.y;
                int endX = parentPoint.x;
                int endY = parentPoint.y;
                PointZ pointZ = map.get(node.getViewId() + "");
                int distanceX = endX - startX;
                int distanceY = endY - startY;
                if(pointZ == null ||pointZ.originX == 0) {
                    pointZ = new PointZ();
                    pointZ.originX = startX + distanceX / DRAW_COUNT;
                    pointZ.originY = startY + distanceY / DRAW_COUNT;
                    map.put(node.getViewId() + "", pointZ);
                }
                if(pointZ.count < DRAW_COUNT) {
                    canvas.drawLine(startX, startY, pointZ.originX, pointZ.originY, mPaint);
                    pointZ.count ++;
                    pointZ.originX = startX + distanceX / DRAW_COUNT * pointZ.count;
                    pointZ.originY = startY + distanceY / DRAW_COUNT * pointZ.count;
                    map.put(node.getViewId() + "", pointZ);
                    invalidate();
                    if(pointZ.count == DRAW_COUNT) {
                        // TODO: 2020-01-03
//                        matrix.postTranslate(-distanceX, -distanceY);
                    }
                } else {
                    canvas.drawLine(startX, startY, pointZ.originX,  pointZ.originY, mPaint);
                }
            }
        }


    }

    private void drawDebugLine(Canvas canvas) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        int childCount = getChildCount();
        //画框框
        canvas.drawLine(0, 0, getMeasuredWidth() * childCount, 0, mPaint);
        canvas.drawLine(0, 0, 0, getMeasuredHeight() * childCount, mPaint);
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(0);
            int height = child.getMeasuredHeight();
            int width = child.getMeasuredWidth();
            // drawX
            canvas.drawLine(0, height * (i + 1), getMeasuredWidth() * childCount, height * (i + 1), mPaint);
            // drawY
            canvas.drawLine(width * (i + 1), 0, width * (i + 1), getMeasuredHeight() * childCount, mPaint);
        }
        // //画角度延伸线
        // for (int angle : EXPAND_ANGLES) {
        //     int w = getMeasuredWidth();
        //     int h = getMeasuredHeight();
        //     int l = (int) Math.sqrt(w * w + h * h);
        //
        //     int lineLength;
        //     if (angle % 180 == 0) { //水平
        //         lineLength = w;
        //     } else if (angle % 90 == 0) {
        //         lineLength = h;
        //     } else {
        //         lineLength = l;
        //     }
        //     Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, lineLength, angle);
        //     canvas.drawLine(w / 2, h / 2, point.x, point.y, mPaint);
        // }
    }


    private void drawLine(Canvas canvas, int w, int h) {
        double angle = Math.toDegrees(Math.atan2(w, h));
        double lineLength = Math.hypot(w, h);//通过两条直角边算斜边长度
        Point point = RelationUtils.calcPointWithAngle(getWidth() / 2, getHeight() / 2, (int) lineLength, angle);
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
            int[] point = invertPoint(e);
            int childIndex = getClickChildIndex(point[0], point[1]);
            if (childIndex > -1) {
                RelationView upNode = (RelationView) getChildAt(childIndex);
                addNext(upNode, point);
                return true;
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

    private int getClickChildIndex(int x, int y) {
        int index = -1;
        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            if (region.contains(x, y)) {
                Timber.d("onSingleTapUp[%s]", i);
                index = i;
                break;
            }
        }
        return index;
    }

    private int[] invertPoint(MotionEvent e) {
        float[] downPoint = new float[]{e.getX(), e.getY()};
        float[] invertPoint = new float[2];//逆变换后的点击点数组
        Matrix invertMatrix = new Matrix();//当前Matrix矩阵的逆矩阵
        matrix.invert(invertMatrix);//通过当前Matrix得到对应的逆矩阵数据
        invertMatrix.mapPoints(invertPoint, downPoint);//通过逆矩阵变化得到逆变换后的点击点
        Timber.d("onSingleTapUp down:%s ,invert:%s", Arrays.toString(downPoint), Arrays.toString(invertPoint));
        return new int[]{((int) invertPoint[0]), ((int) invertPoint[1])};
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
