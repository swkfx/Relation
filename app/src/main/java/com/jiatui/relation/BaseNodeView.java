package com.jiatui.relation;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class BaseNodeView extends View {
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
}
