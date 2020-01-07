package com.jiatui.relation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2020/1/4
 *      desc   :
 * </pre>
 */
public class ClueNode extends BaseNodeView {

    private NodeInfo info;
    private int rootSize = 60;//dp
    private int nodeChildSize = 40;//dp
    private int nodeChildNodeSize = 24;//dp
    private int normalTextSize = 16;//dp
    private int smallTextSize = 12;//dp
    private int textSize_10 = 10;//dp
    private int rootSpace = 2;//dp
    private int lineDistance = 130;//dp
    private int longLineDistance = 230;//dp
    private int subLineDistance = 48;//dp
    private Paint bitmapPaint;
    private TextPaint textPaint;
    private Paint linePaint;
    private Bitmap rootBitmap;
    private Rect rootTextRect;
    private Rect childTextRect;
    private Path linePath;
    private Map<String, Bitmap> cacheBitmapMap;

    private boolean hasOtherNode;//是否有其他node


    public ClueNode(Context context) {
        this(context, null);
    }

    public ClueNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClueNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(RelationUtils.dp2px(context, normalTextSize));
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(RelationUtils.dp2px(context, 1));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.parseColor("#444444"));
        linePaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        rootTextRect = new Rect();
        childTextRect = new Rect();

        linePath = new Path();

        cacheBitmapMap = new HashMap<>();
    }

    public void setNodeInfo(NodeInfo info, boolean has) {
        this.info = info;
        this.hasOtherNode = has;

        loadRootBitmap(info.picUrl);
        loadNodeChildBitmap(info);
        invalidate();
    }


    private void loadNodeChildBitmap(NodeInfo info) {
        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            for (NodeInfo child : info.childes) {
                if (child != null && child.childes != null && !child.childes.isEmpty()) {
                    for (NodeInfo nodeChild : child.childes) {
                        if (!TextUtils.isEmpty(nodeChild.picUrl)) {
                            int size = RelationUtils.dp2px(getContext(), nodeChildNodeSize);
                            if (cacheBitmapMap.get(nodeChild.picUrl) == null) {
                                loadBitmap(nodeChild.picUrl, size);
                            } else {
                                invalidate();
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadBitmap(final String url, int size) {
        RequestOptions options = new RequestOptions().fitCenter().circleCrop();
        Glide.with(this)
                .asBitmap()
                .apply(options)
                .load(url)
                .into(new CustomTarget<Bitmap>(size, size) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<?
                            super Bitmap> transition) {
                        cacheBitmapMap.put(url, resource);
                        invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void loadRootBitmap(String url) {
        int size = RelationUtils.dp2px(getContext(), rootSize);
        RequestOptions options = new RequestOptions().fitCenter().circleCrop();
        Glide.with(this)
                .asBitmap()
                .apply(options)
                .load(url)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>(size, size) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<?
                            super Bitmap> transition) {
                        rootBitmap = resource;
                        invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw childes
        drawChildes(canvas);

        //draw 中心的rootView
        drawRoot(canvas);
    }

    private void drawChildes(Canvas canvas) {
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        if (info.childes != null && !info.childes.isEmpty()) {
            int size = Math.min(8, info.childes.size());
            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
                //绘制连接线
                int count = Math.min(6, info.childes.size());
                float offsetAngle = 360f / count / 2;//设计稿起始偏移角度
                float angle;
                if (i > 5) {
                    //这里特殊处理。 如果超过 6 个child 第7个 角度在 第4个之后+偏移角度，第8个在第5个之后+偏移角度 最多只能有八个
                    int position = i == 6 ? 3 : 4;
                    angle = 360f / count * position + offsetAngle + offsetAngle;
                } else {
                    angle = 360f / count * i + offsetAngle;
                }
                int radius = RelationUtils.dp2px(getContext(), i > 5 ? longLineDistance : lineDistance);
                Point point = RelationUtils.calcPointWithAngle(x, y, radius, angle);
                linePath.reset();
                linePath.moveTo(x, y);
                linePath.lineTo(point.x, point.y);
                canvas.drawPath(linePath, linePaint);
                //绘制 node的 child
                //childLineAngle
                float childAngle = angle + 180 % 360;
                int color = RelationUtils.generateChildColor(i == 0);
                drawNodeChild(canvas, point, child, childAngle, color);
            }
        }

        //绘制「其他」节点
        if (hasOtherNode) {
            float angle = 60f;
            int radius = RelationUtils.dp2px(getContext(), longLineDistance);
            Point point = RelationUtils.calcPointWithAngle(x, y, radius, angle);
            linePath.reset();
            linePath.moveTo(x, y);
            linePath.lineTo(point.x, point.y);
            canvas.drawPath(linePath, linePaint);

            int color = RelationUtils.getOtherNodeColor();
            int size = RelationUtils.dp2px(getContext(), nodeChildSize);
            float r = size / 2f;
            bitmapPaint.reset();
            bitmapPaint.setColor(color);
            bitmapPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(point.x, point.y, r, bitmapPaint);

            textPaint.setTextSize(RelationUtils.dp2px(getContext(), normalTextSize));
            String text = "其他";
            textPaint.getTextBounds(text, 0, text.length(), childTextRect);
            float textX = point.x - childTextRect.exactCenterX();
            float textY = point.y - childTextRect.exactCenterY();
            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    private void drawNodeChild(Canvas canvas, Point point, NodeInfo child, float offsetAngle, int color) {
        //绘制子节点的子节点
        drawNodeChildNode(canvas, point, child, offsetAngle, color);

        bitmapPaint.reset();
        boolean hasBorder = child.childes != null && child.childes.size() > 5;
        int size = RelationUtils.dp2px(getContext(), nodeChildSize);
        float radius = size / 2;
        if (hasBorder) {
            bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            int strokeWidth = RelationUtils.dp2px(getContext(), 3);
            bitmapPaint.setStrokeWidth(strokeWidth);
            bitmapPaint.setColor(RelationUtils.changeColorAlpha(color, 0.5f));
            canvas.drawCircle(point.x, point.y, radius + strokeWidth, bitmapPaint);
        }
        bitmapPaint.setColor(color);
        bitmapPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point.x, point.y, radius, bitmapPaint);

        // 需要考虑字符串长度大于node宽度的情况
        int length = child.name.length();
        textPaint.setColor(Color.WHITE);
        if (length <= 3) {
            textPaint.setTextSize(RelationUtils.dp2px(getContext(), length < 3 ? normalTextSize :
                    smallTextSize));
            textPaint.getTextBounds(child.name, 0, length, childTextRect);
            float x = point.x - childTextRect.exactCenterX();
            float y = point.y - childTextRect.exactCenterY();
            canvas.drawText(child.name, x, y, textPaint);
        } else {
            textPaint.setTextSize(RelationUtils.dp2px(getContext(), smallTextSize));
            int limitWidth = Math.round(textPaint.measureText(child.name, 0, 2));
            StaticLayout layout = new StaticLayout(child.name, textPaint,
                    limitWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
            canvas.save();
            canvas.translate(point.x - layout.getWidth() / 2, point.y - layout.getHeight() / 2);
            layout.draw(canvas);
            canvas.restore();
        }


    }

    /**
     * 绘制子节点的子节点
     *
     * @param canvas
     * @param point
     * @param child
     */
    private void drawNodeChildNode(Canvas canvas, Point point, NodeInfo child, float offsetAngle, int color) {
        if (child != null && child.childes != null && !child.childes.isEmpty()) {
            int count = Math.min(5, child.childes.size());
            for (int i = 0; i < child.childes.size(); i++) {
                NodeInfo info = child.childes.get(i);
                Bitmap nodeBitmap = cacheBitmapMap.get(info.picUrl);
                if (nodeBitmap != null) {
                    //绘制连接线
                    int radius = RelationUtils.dp2px(getContext(), subLineDistance);
                    float averageAngle = 360f / count;
                    float offsetA = offsetAngle + averageAngle / 2;
                    float angle = averageAngle * i + offsetA;
                    Point endPoint = RelationUtils.calcPointWithAngle(point, radius, angle);
                    linePath.reset();
                    linePath.moveTo(point.x, point.y);
                    linePath.lineTo(endPoint.x, endPoint.y);
                    canvas.drawPath(linePath, linePaint);

                    //绘制头像的边框
                    bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    int strokeWidth = RelationUtils.dp2px(getContext(), 1.5f);
                    bitmapPaint.setStrokeWidth(strokeWidth);
                    bitmapPaint.setColor(RelationUtils.changeColorAlpha(color, 0.5f));
                    int r = nodeBitmap.getWidth() / 2;
                    canvas.drawCircle(endPoint.x, endPoint.y, r, bitmapPaint);
                    // 绘制头像
                    float left = endPoint.x - nodeBitmap.getWidth() / 2;
                    float top = endPoint.y - nodeBitmap.getHeight() / 2;
                    bitmapPaint.reset();
                    canvas.drawBitmap(nodeBitmap, left, top, bitmapPaint);
                    // //绘制头像蒙层
                    bitmapPaint.setColor(Color.parseColor("#4D000000"));
                    canvas.drawCircle(endPoint.x, endPoint.y, r, bitmapPaint);
                    //绘制名字
                    if (!TextUtils.isEmpty(info.name)) {
                        textPaint.setTextSize(RelationUtils.dp2px(getContext(), textSize_10));
                        textPaint.setColor(Color.parseColor("#555555"));
                        textPaint.getTextBounds(info.name, 0, info.name.length(), childTextRect);
                        float x = endPoint.x - childTextRect.width() / 2;
                        float space = RelationUtils.dp2px(getContext(), rootSpace);
                        float y = top + nodeBitmap.getHeight() + childTextRect.height() + space;
                        canvas.drawText(info.name, x, y, textPaint);
                    }
                }
            }
        }
    }

    private void drawRoot(Canvas canvas) {
        if (this.rootBitmap != null) {
            float left = (getWidth() - rootBitmap.getWidth()) >> 1;
            float top = (getHeight() - rootBitmap.getHeight()) >> 1;
            canvas.drawBitmap(rootBitmap, left, top, bitmapPaint);

            if (!TextUtils.isEmpty(info.name)) {
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(RelationUtils.dp2px(getContext(), normalTextSize));
                textPaint.getTextBounds(info.name, 0, info.name.length(), rootTextRect);
                float x = (getWidth() - rootTextRect.width()) >> 1;
                float space = RelationUtils.dp2px(getContext(), rootSpace);
                float y = top + rootBitmap.getHeight() + rootTextRect.height() + space;
                canvas.drawText(info.name, x, y, textPaint);
            }
        }
    }
}
