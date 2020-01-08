package com.jiatui.relation.model;

import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

import com.jiatui.relation.util.NodeUtils;

/**
 * 根据连接线的 起点坐标，角度，线长 可以计算出 此node 再界面上需要显示的中心坐标点。
 * 根据 半径 + 中心点坐标 计算出 自身 所占的 区域 Region 或 RectF
 * Region 只支持 int 参数。可能会丢失精度。
 */
public class Node {
    private PointF startPoint; //连接线的起点坐标
    private float startAngle; //连接线的角度
    private int lineDistance; //连接线的长度
    private float radius; //自身的半径
    private NodeInfo nodeInfo;

    private PointF centerPoint;
    private RectF rect;
    private Region region;

    private int color;


    public Node(PointF startPoint, float startAngle, int lineDistance, float radius, NodeInfo info) {
        this.startPoint = startPoint;
        this.startAngle = startAngle;
        this.lineDistance = lineDistance;
        this.radius = radius;
        this.nodeInfo = info;
    }

    public PointF getCenterPoint() {
        if (centerPoint == null) {
            centerPoint = NodeUtils.calcPointWithAngleF(startPoint.x,
                    startPoint.y, lineDistance, startAngle);
        }
        return centerPoint;
    }

    public RectF getRect() {
        if (rect == null) {
            PointF f = getCenterPoint();
            rect = new RectF(
                    f.x - radius, f.y - radius,
                    f.x + radius, f.y + radius
            );
        }
        return rect;
    }

    public Region getRegion() {
        if (region == null) {
            PointF f = getCenterPoint();
            region = new Region(
                    (int) (f.x - radius),
                    (int) (f.y - radius),
                    (int) (f.x + radius),
                    (int) (f.y + radius)
            );
        }

        return region;
    }

    public float getRadius() {
        return radius;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public PointF getStartPoint() {
        return startPoint;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
