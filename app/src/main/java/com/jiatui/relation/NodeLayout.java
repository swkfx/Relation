package com.jiatui.relation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.jiatui.relation.child.BaseNodeView;
import com.jiatui.relation.child.ClueNode;
import com.jiatui.relation.child.OtherClueNode;
import com.jiatui.relation.child.UsersNode;
import com.jiatui.relation.model.Node;
import com.jiatui.relation.model.NodeInfo;
import com.jiatui.relation.util.NodeUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class NodeLayout extends ViewGroup {


    private Matrix matrix;
    private Paint mPaint;

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat detector;

    private List<Region> regions = new ArrayList<>();
    private List<BaseNodeView> expandNodes = new ArrayList<>();

    private int[] EXPAND_ANGLES = new int[8];
    private int[] EXPAND_ANGLES_UP = new int[4];
    private int[] EXPAND_ANGLES_DOWN = new int[4];
    private ClueNode root;
    private Map<String, PointZ> map = new HashMap<>();

    PaintFlagsDrawFilter drawFilter;

    public NodeLayout(Context context) {
        this(context, null);
    }

    public NodeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint = new Paint();
        mPaint.setStrokeWidth(5);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        matrix = new Matrix();
        mScaleGestureDetector = new ScaleGestureDetector(context,
                new MySimpleOnScaleGestureListener());
        changeScaleMin(context, mScaleGestureDetector);
        detector = new GestureDetectorCompat(context, new MyGestureListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int angle = (int) Math.toDegrees(Math.atan2(h, w));
        EXPAND_ANGLES[0] = angle; //右下
        EXPAND_ANGLES[1] = 90; //下
        EXPAND_ANGLES[2] = 180 - angle; //左下
        EXPAND_ANGLES[3] = 180; // 左
        EXPAND_ANGLES[4] = 180 + angle;//左上
        EXPAND_ANGLES[5] = 270; // 上
        EXPAND_ANGLES[6] = 360 - angle; //右上
        EXPAND_ANGLES[7] = 360; // 右

        EXPAND_ANGLES_UP[0] = 270; // 上
        EXPAND_ANGLES_UP[1] = 180 + angle;//左上
        EXPAND_ANGLES_UP[2] = 360 - angle; //右上
        EXPAND_ANGLES_UP[3] = 180; // 左

        EXPAND_ANGLES_DOWN[0] = 90; //下
        EXPAND_ANGLES_DOWN[1] = angle; //右下
        EXPAND_ANGLES_DOWN[2] = 180 - angle; //左下
        EXPAND_ANGLES_DOWN[3] = 360; // 右

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
                Rect bounds = ((BaseNodeView) child).getParentRegion().getBounds();
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
    private Point findNextLayoutPoint(int centerX, int centerY, int[] findAngles) {
        float w = getMeasuredWidth();
        float h = getMeasuredHeight();
        double l = Math.hypot(w, h);//通过两条直角边算斜边长度
        boolean search = true;
        int count = 1;
        while (search) {
            for (int angle : findAngles) {
                int lineLength;
                if (angle % 180 == 0) { //水平
                    lineLength = (int) w;
                } else if (angle % 90 == 0) {
                    lineLength = (int) h;
                } else {
                    lineLength = (int) l;
                }
                Point point = NodeUtils.calcPointWithAngle(centerX, centerY, lineLength * count, angle);
                boolean used = false;
                for (Region region : regions) {
                    if (region.contains(point.x, point.y)) {
                        used = true;
                        break;
                    }
                }
                if (!used) {
                    return point;
                }
            }
            count++;
            //先设置查找20圈。防止死循环
            search = count < 20;
        }
        return null;
    }

    /**
     * 以参数为中心点寻找周围下一个可布局的中心点
     *
     * @param centerX 中心点x
     * @param centerY 中心点y
     * @return 周围下一个可布局的 位置的中心点
     */
    private PointF findNextLayoutPointF(float centerX, float centerY, int[] findAngles) {
        float w = getMeasuredWidth();
        float h = getMeasuredHeight();
        double l = Math.hypot(w, h);//通过两条直角边算斜边长度
        boolean search = true;
        int count = 1;
        while (search) {
            for (int angle : findAngles) {
                int lineLength;
                if (angle % 180 == 0) { //水平
                    lineLength = (int) w;
                } else if (angle % 90 == 0) {
                    lineLength = (int) h;
                } else {
                    lineLength = (int) l;
                }
                PointF point = NodeUtils.calcPointWithAngleF(centerX, centerY, lineLength * count, angle);
                boolean used = false;
                for (Region region : regions) {
                    if (region.contains(((int) point.x), ((int) point.y))) {
                        used = true;
                        break;
                    }
                }
                if (!used) {
                    return point;
                }
            }
            count++;
            //先设置查找20圈。防止死循环
            search = count < 20;
        }
        return null;
    }


    public void addRoot(NodeInfo rootInfo, boolean hasMore) {
        if (getContext() != null) {
            root = new ClueNode(getContext());
            root.setNodeInfo(rootInfo, hasMore);
            addView(root);
            regions.add(new Region());
        }
    }

    public void addUsersNode(Node parentNode, NodeInfo info) {
        parentNode.getNodeInfo().isExpand = true;
        Rect bounds = parentNode.getRegion().getBounds();
        float startAngle = parentNode.getStartAngle();
        int[] findAngles = startAngle >= 180 ? EXPAND_ANGLES_UP : EXPAND_ANGLES_DOWN;
        Point point = findNextLayoutPoint(bounds.centerX(), bounds.centerY(), findAngles);
        int wR = root.getMeasuredWidth() / 2;
        int hR = root.getMeasuredHeight() / 2;
        if (point != null) {
            final UsersNode nextNode = new UsersNode(getContext());
//                nextNode.setVisibility(INVISIBLE);
            nextNode.setViewId(id++);
            nextNode.setExpandPoint(new Point(((int) parentNode.getCenterPoint().x), ((int) parentNode.getCenterPoint().y)));
            nextNode.setParentPoint(point);
            Region parentRegion = new Region();
            parentRegion.set(point.x - wR, point.y - hR, point.x + wR, point.y + hR);
            nextNode.setParentRegion(parentRegion);
            addView(nextNode);
            float childStartAngle = startAngle + 180 % 360;
            nextNode.setNodeInfo(info, parentNode.getColor(), childStartAngle);
            expandNodes.add(nextNode);
            regions.add(parentRegion);
        }
    }

    public void addAtlasNode(Node parentNode, NodeInfo info, boolean hasMore) {
        parentNode.getNodeInfo().isExpand = true;
        Rect bounds = parentNode.getRegion().getBounds();
        float startAngle = parentNode.getStartAngle();
        int[] findAngles = startAngle >= 180 ? EXPAND_ANGLES_UP : EXPAND_ANGLES_DOWN;
        Point point = findNextLayoutPoint(bounds.centerX(), bounds.centerY(), findAngles);
        int wR = root.getMeasuredWidth() / 2;
        int hR = root.getMeasuredHeight() / 2;
        if (point != null) {
            final ClueNode nextNode = new ClueNode(getContext());
//                nextNode.setVisibility(INVISIBLE);
            nextNode.setViewId(id++);
            nextNode.setExpandPoint(new Point(((int) parentNode.getCenterPoint().x), ((int) parentNode.getCenterPoint().y)));
            nextNode.setParentPoint(point);
            Region parentRegion = new Region();
            parentRegion.set(point.x - wR, point.y - hR, point.x + wR, point.y + hR);
            nextNode.setParentRegion(parentRegion);
            addView(nextNode);
            nextNode.setNodeInfo(info, hasMore);
            expandNodes.add(nextNode);
            regions.add(parentRegion);
        }
    }


    public void addOtherClueNode(NodeInfo info) {
        if (root != null && root.isHasOtherNode()) {
            Node otherNode = root.getOtherNode();
            otherNode.getNodeInfo().isExpand = true;
            Rect bounds = otherNode.getRegion().getBounds();
            float startAngle = otherNode.getStartAngle();
            int[] findAngles = startAngle >= 180 ? EXPAND_ANGLES_UP : EXPAND_ANGLES_DOWN;
            Point point = findNextLayoutPoint(bounds.centerX(), bounds.centerY(), findAngles);
            int wR = root.getMeasuredWidth() / 2;
            int hR = root.getMeasuredHeight() / 2;
            if (point != null) {
                final OtherClueNode nextNode = new OtherClueNode(getContext());
//                nextNode.setVisibility(INVISIBLE);
                nextNode.setViewId(id++);
                nextNode.setExpandPoint(new Point(((int) otherNode.getCenterPoint().x), ((int) otherNode.getCenterPoint().y)));
                nextNode.setParentPoint(point);
                Region parentRegion = new Region();
                parentRegion.set(point.x - wR, point.y - hR, point.x + wR, point.y + hR);
                nextNode.setParentRegion(parentRegion);
                addView(nextNode);
                nextNode.setNodeInfo(info);
                expandNodes.add(nextNode);
                regions.add(parentRegion);
            }
        }
    }

    public void removeOtherClueNode() {

    }

    private int id = 10000009;
    private final static int DRAW_COUNT = 50;


//     /**
//      * 添加一个下级节点
//      *
//      * @param upNode  上级节点 ，
//      * @param clickPt 点击坐标 ，
//      */
//     private void addNext(BaseNodeView upNode, int[] clickPt) {
//         Rect nodeChildRect = upNode.getChildRect(clickPt[0] - upNode.getLeft(), clickPt[1] - upNode.getTop());
//         if (nodeChildRect != null && !nodeChildRect.isEmpty()) {
//             Rect bounds = upNode.getParentRegion().getBounds();
//             int wR = upNode.getMeasuredWidth() / 2;
//             int hR = upNode.getMeasuredHeight() / 2;
//             double childAngle = upNode.getChildAngle(clickPt[0] - upNode.getLeft(), clickPt[1] - upNode.getTop());
//             int[] findAngles = childAngle >= 180 ? EXPAND_ANGLES_UP : EXPAND_ANGLES_DOWN;
//             Point point = findNextLayoutPoint(bounds.centerX(), bounds.centerY(), findAngles);
//             if (point != null) {
//                 final BaseNodeView nextNode = new BaseNodeView(getContext());
// //                nextNode.setVisibility(INVISIBLE);
//                 nextNode.setViewId(id++);
//                 nextNode.setExpandPoint(new Point(clickPt[0], clickPt[1]));
//                 nextNode.setParentPoint(point);
//                 Region parentRegion = new Region();
//                 parentRegion.set(point.x - wR, point.y - hR, point.x + wR, point.y + hR);
//                 nextNode.setParentRegion(parentRegion);
//                 addView(nextNode);
//                 expandNodes.add(nextNode);
//                 regions.add(parentRegion);
//             }
//         }
//     }

    private class PointZ {
        int originX;
        int originY;
        int count;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        canvas.drawColor(Color.BLACK);
        canvas.setDrawFilter(drawFilter);

        // drawDebugLine(canvas);
        super.onDraw(canvas);
        if (expandNodes != null && !expandNodes.isEmpty()) {
            for (BaseNodeView node : expandNodes) {
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
                if (pointZ == null || pointZ.originX == 0) {
                    pointZ = new PointZ();
                    pointZ.originX = startX + distanceX / DRAW_COUNT;
                    pointZ.originY = startY + distanceY / DRAW_COUNT;
                    map.put(node.getViewId() + "", pointZ);
                }
                if (pointZ.count < DRAW_COUNT) {
                    canvas.drawLine(startX, startY, pointZ.originX, pointZ.originY, mPaint);
                    pointZ.count++;
                    pointZ.originX = startX + distanceX / DRAW_COUNT * pointZ.count;
                    pointZ.originY = startY + distanceY / DRAW_COUNT * pointZ.count;
                    map.put(node.getViewId() + "", pointZ);
                    invalidate();
                    if (pointZ.count == DRAW_COUNT) {
                        // int childIndex = getClickChildIndex(endX, endY);
                        // if (childIndex > -1) {
                        //     BaseNodeView nextView = (BaseNodeView) getChildAt(childIndex);
//                            nextView.setVisibility(VISIBLE);
//                            setShowAnimation(nextView, 300);
//                         }
                    }
                    int childIndex = getClickChildIndex(startX, startY);
                    if (childIndex > -1) {
                        BaseNodeView upNode = (BaseNodeView) getChildAt(childIndex);
                        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                        int winX = display.getWidth() / 2;
                        int winY = display.getHeight() / 2;
                        Point op = upNode.getOriginPoint();
                        int finalX = winX - op.x;
                        int finalY = winY - op.y;
                        // Log.d(RelationLayout.class.getSimpleName(), "final: opdata " + op.x + ' ' + op.y);
                        // Log.d(RelationLayout.class.getSimpleName(), "final: " + finalX + ' ' + finalY);
                        matrix.postTranslate(-((distanceX - finalX) * getScale()) / DRAW_COUNT, -((distanceY - finalY) * getScale()) / DRAW_COUNT);
//
                    }
                } else {
                    canvas.drawLine(startX, startY, pointZ.originX, pointZ.originY, mPaint);
                }
            }
        }
    }

    /**
     * View渐现动画效果
     */
    public void setShowAnimation(View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        AlphaAnimation mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
        mShowAnimation.setFillAfter(true);
        view.startAnimation(mShowAnimation);
    }

    private float[] invertPoint(MotionEvent e) {
        float[] downPoint = new float[]{e.getX(), e.getY()};
        float[] invertPoint = new float[2];//逆变换后的点击点数组
        Matrix invertMatrix = new Matrix();//当前Matrix矩阵的逆矩阵
        matrix.invert(invertMatrix);//通过当前Matrix得到对应的逆矩阵数据
        invertMatrix.mapPoints(invertPoint, downPoint);//通过逆矩阵变化得到逆变换后的点击点
        Timber.d("onSingleTapUp down:%s ,invert:%s", Arrays.toString(downPoint), Arrays.toString(invertPoint));
        return new float[]{invertPoint[0], invertPoint[1]};
    }

    private int[] invertPoint(int x, int y) {
        float[] downPoint = new float[]{x, y};
        float[] invertPoint = new float[2];//逆变换后的点击点数组
        Matrix invertMatrix = new Matrix();//当前Matrix矩阵的逆矩阵
        matrix.invert(invertMatrix);//通过当前Matrix得到对应的逆矩阵数据
        invertMatrix.mapPoints(invertPoint, downPoint);//通过逆矩阵变化得到逆变换后的点击点
        Timber.d("onSingleTapUp down:%s ,invert:%s", Arrays.toString(downPoint), Arrays.toString(invertPoint));
        return new int[]{((int) invertPoint[0]), ((int) invertPoint[1])};
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
            // 转换点击坐标 从 matrix 到 标准坐标
            float[] point = invertPoint(e);
            // 通过点击坐标 识别点击的 child
            int childIndex = getClickChildIndex(point[0], point[1]);
            // 根据点击的child 判断再此child 中 具体点击是哪个node
            if (childIndex > -1) {
                BaseNodeView upNode = (BaseNodeView) getChildAt(childIndex);
                Node node = upNode.getNodeByPoint(point[0] - upNode.getLeft(), point[1] - upNode.getTop());
                int[] originPt = new int[2];
                originPt[0] = (int) e.getX();
                originPt[1] = (int) e.getY();
                upNode.setOriginPoint(new Point(originPt[0], originPt[1]));
                if (nodeClickListener != null && node != null) {
                    nodeClickListener.onNodeClick(node);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            matrix.postTranslate(-distanceX, -distanceY);
            // scrollBy(((int) distanceX), ((int) distanceY));
            invalidate();
            //  Timber.d("onScroll dx=%s , dy=%s", offsetX, offsetY);
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

    private int getClickChildIndex(float x, float y) {
        int index = -1;
        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            if (region.contains((int) x, (int) y)) {
                Timber.d("getClickChildIndex[%s],regions size[%s]", i, regions.size());
                return i;
            }
        }
        return index;
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
                matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
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

    NodeClickListener nodeClickListener;

    public void setNodeClickListener(NodeClickListener nodeClickListener) {
        this.nodeClickListener = nodeClickListener;
    }

    public interface NodeClickListener {
        void onNodeClick(Node node);
    }

}
