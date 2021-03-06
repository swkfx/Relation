package com.jiatui.relation.child;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Region;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.util.AttributeSet;
import android.view.View;

import com.jiatui.relation.model.Node;

import java.util.List;

import timber.log.Timber;

public abstract class BaseNodeView extends View {
    protected int viewId;
    protected List<Region> childRegions;

    protected Region parentRegion;

    protected PointF expandPoint;//在父容器的连线起点
    protected PointF parentPoint;//在父容器的layout 的中心店
    protected PointF originPoint;//原始点

    protected float transProgress;


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

    public PointF getExpandPoint() {
        return expandPoint;
    }

    public void setExpandPoint(PointF expandPoint) {
        this.expandPoint = expandPoint;
    }

    public PointF getParentPoint() {
        return parentPoint;
    }

    public void setParentPoint(PointF parentPoint) {
        this.parentPoint = parentPoint;
    }

    public PointF getOriginPoint() {
        return originPoint;
    }

    public void setOriginPoint(PointF originPoint) {
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

    public void transformAnimation(long duration, final float fromXDelta, float fromYDelta) {
        // Animation translateAnimation = new TranslateAnimation(fromXDelta, 0, fromYDelta, 0);//设置平移的起点和终点
        // translateAnimation.setDuration(duration);//动画持续的时间为10s
        // translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        // translateAnimation.setFillAfter(true);//不回到起始位置
        // // translateAnimation.setInterpolator(new DecelerateInterpolator(0.6F));
        // setAnimation(translateAnimation);//给imageView添加的动画效果
        // translateAnimation.startNow();//动画开始执行 放在最后即可
        clearAnimation();
        setTranslationX(fromXDelta);
        setTranslationY(fromYDelta);
        ViewCompat.animate(this)
                .setDuration(duration)
                .translationX(0)
                .translationY(0)
                .setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(View view) {
                        float translationX = view.getTranslationX();
                        float value = translationX / fromXDelta;
                        setTransProgress(1 - value);
                        Timber.d("getTransProgress[%s]", getTransProgress());
                    }
                }).start();
    }

    public float getTransProgress() {
        return transProgress;
    }

    public void setTransProgress(float transProgress) {
        this.transProgress = transProgress;
        invalidate();
    }
}
