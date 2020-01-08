package com.jiatui.relation.child;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import com.jiatui.relation.model.Node;

import java.util.List;

import timber.log.Timber;

public abstract class BaseNodeView extends View {
    protected int viewId;
    protected List<Region> childRegions;

    protected Region parentRegion;

    protected Point expandPoint;//在父容器的连线起点
    protected Point parentPoint;//在父容器的layout 的中心店
    protected Point originPoint;//原始点


    public BaseNodeView(Context context) {
        this(context, null);
    }

    public BaseNodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseNodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 根据 点击的X,Y 判断是否有点击到child 并且 返回 子View 所在的区域
     *
     * @param x 在当前View中的x
     * @param y 在当前View中的y
     * @return 返回 子View 所在的区域 如果没有点击到 则返回null
     */
    // abstract Rect getChildRect(int x, int y);

    /**
     * 根据 点击的X,Y 返回绘制的child所在位置到View中心点的角度，及连接线的角度。
     * 该方法影响父容器判断寻找下个区域的位置。
     *
     * @param x 在当前View中的x
     * @param y 在当前View中的y
     * @return 返回 子View 所在的区域 如果没有点击到 则返回null
     */
    // abstract float getChildAngle(int x, int y);
    public abstract Node getNodeByPoint(float x, float y);

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public List<Region> getChildRegions() {
        return childRegions;
    }

    public void setChildRegions(List<Region> childRegions) {
        this.childRegions = childRegions;
    }

    public Region getParentRegion() {
        return parentRegion;
    }

    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
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

    public Point getOriginPoint() {
        return originPoint;
    }

    public void setOriginPoint(Point originPoint) {
        this.originPoint = originPoint;
    }

    protected double getChildAngle(int x, int y) {
        int w = x - getWidth() / 2;
        int h = y - getHeight() / 2;
        double angle = Math.toDegrees(Math.atan2(h, w));
        // 修正角度 返回  0-360 之间的角度
        if (angle != 0) {
            angle = angle % 360 == 0 ? 360 : angle % 360;
            if (angle < 0) {
                angle = angle + 360;
            }
        }
        Timber.d("childAngle:%s", angle);
        return angle;
    }
}
