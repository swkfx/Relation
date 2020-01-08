package com.jiatui.relation.child;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jiatui.relation.model.Node;
import com.jiatui.relation.model.NodeInfo;
import com.jiatui.relation.util.NodeUtils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

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
    // private List<Node> nodes;
    private Map<String, Node> nodeMap;


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
        textPaint.setTextSize(NodeUtils.dp2px(context, normalTextSize));
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(NodeUtils.dp2px(context, 1));
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

    private void initNodes() {
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
        } else {
            nodeMap.clear();
        }
        PointF startPoint = new PointF(getWidth() / 2f, getHeight() / 2f);
        if (hasOtherNode) {
            int lineDistance = NodeUtils.dp2px(getContext(), longLineDistance);
            float radius = NodeUtils.dp2px(getContext(), nodeChildSize) / 2f;
            NodeInfo otherInfo = NodeInfo.generateOther();
            Node otherNode = new Node(startPoint, 60f, lineDistance, radius, otherInfo);
            otherNode.setColor(NodeUtils.getOtherNodeColor());
            nodeMap.put(otherInfo.getNodeId(), otherNode);
        }

        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            int size = Math.min(8, info.childes.size());
            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
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

                int distance = NodeUtils.dp2px(getContext(), i > 5 ? longLineDistance : lineDistance);
                float radius = NodeUtils.dp2px(getContext(), nodeChildSize) / 2;
                child.nodeType = NodeInfo.TYPE.ATLAS;
                Node node = new Node(startPoint, angle, distance, radius, child);
                node.setColor(NodeUtils.generateChildColor(i == 0));
                // nodes.add(node);
                nodeMap.put(child.getNodeId(), node);

                float startAngle = angle + 180 % 360;
                if (child.childes != null && !child.childes.isEmpty()) {
                    int childCount = Math.min(5, child.childes.size());
                    for (int j = 0; j < childCount; j++) {
                        int childDistance = NodeUtils.dp2px(getContext(), subLineDistance);
                        float averageAngle = 360f / childCount;
                        float offsetA = startAngle + averageAngle / 2;
                        float childStartAngle = averageAngle * j + offsetA;
                        int childRadius = NodeUtils.dp2px(getContext(), nodeChildNodeSize) / 2;
                        NodeInfo childNodeInfo = child.childes.get(j);
                        childNodeInfo.nodeType = NodeInfo.TYPE.USER;
                        Node childNode = new Node(node.getCenterPoint(), childStartAngle, childDistance, childRadius, childNodeInfo);
                        // nodes.add(childNode);
                        nodeMap.put(childNodeInfo.getNodeId(), childNode);
                    }
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initNodes();
        Timber.d("onSizeChanged");
    }

    private void loadNodeChildBitmap(NodeInfo info) {
        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            for (NodeInfo child : info.childes) {
                if (child != null && child.childes != null && !child.childes.isEmpty()) {
                    for (NodeInfo nodeChild : child.childes) {
                        if (!TextUtils.isEmpty(nodeChild.picUrl)) {
                            int size = NodeUtils.dp2px(getContext(), nodeChildNodeSize);
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
        int size = NodeUtils.dp2px(getContext(), rootSize);
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
        //debug draw
        debugDraw(canvas);

        //draw childes
        drawChildes(canvas);

        //draw 中心的rootView
        drawRoot(canvas);
    }

    private void debugDraw(Canvas canvas) {
        if (nodeMap != null && !nodeMap.isEmpty()) {
            for (Node node : nodeMap.values()) {
                canvas.drawRect(node.getRect(), textPaint);
            }
        }
    }

    private void drawChildes(Canvas canvas) {
        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            int size = Math.min(8, info.childes.size());
            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
                Node node = nodeMap.get(child.getNodeId());
                if (node != null) {
                    //绘制连接线
                    PointF start = node.getStartPoint();
                    PointF point = node.getCenterPoint();
                    linePath.reset();
                    linePath.moveTo(start.x, start.y);
                    linePath.lineTo(point.x, point.y);
                    canvas.drawPath(linePath, linePaint);
                    //绘制 node的 child
                    //childLineAngle
                    // float childAngle = node.getStartAngle() + 180 % 360;
                    int color = node.getColor();
                    drawNodeChild(canvas, point, child, color);
                }
            }
        }

        //绘制「其他」节点
        if (hasOtherNode) {
            Node otherNode = getOtherNode();
            PointF start = otherNode.getStartPoint();
            PointF point = otherNode.getCenterPoint();
            linePath.reset();
            linePath.moveTo(start.x, start.y);
            linePath.lineTo(point.x, point.y);
            canvas.drawPath(linePath, linePaint);

            bitmapPaint.reset();
            bitmapPaint.setColor(otherNode.getColor());
            bitmapPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(point.x, point.y, otherNode.getRadius(), bitmapPaint);

            textPaint.setTextSize(NodeUtils.dp2px(getContext(), normalTextSize));
            String text = otherNode.getNodeInfo().name;
            textPaint.getTextBounds(text, 0, text.length(), childTextRect);
            float textX = point.x - childTextRect.exactCenterX();
            float textY = point.y - childTextRect.exactCenterY();
            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    private void drawNodeChild(Canvas canvas, PointF point, NodeInfo child, int color) {
        Node node = nodeMap.get(child.getNodeId());
        if (node == null) {
            return;
        }
        //绘制子节点的子节点
        drawNodeChildNode(canvas, child, color);
        bitmapPaint.reset();
        boolean hasBorder = child.childes != null && child.childes.size() > 5;
        float radius = node.getRadius();
        if (hasBorder) {
            bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            int strokeWidth = NodeUtils.dp2px(getContext(), 3);
            bitmapPaint.setStrokeWidth(strokeWidth);
            bitmapPaint.setColor(NodeUtils.changeColorAlpha(color, 0.5f));
            canvas.drawCircle(point.x, point.y, radius + strokeWidth, bitmapPaint);
        }
        bitmapPaint.setColor(color);
        bitmapPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point.x, point.y, radius, bitmapPaint);

        // 处理字符串长度大于node宽度的情况
        int length = child.name.length();
        textPaint.setColor(Color.WHITE);
        if (length <= 3) {
            textPaint.setTextSize(NodeUtils.dp2px(getContext(), length < 3 ? normalTextSize :
                    smallTextSize));
            textPaint.getTextBounds(child.name, 0, length, childTextRect);
            float x = point.x - childTextRect.exactCenterX();
            float y = point.y - childTextRect.exactCenterY();
            canvas.drawText(child.name, x, y, textPaint);
        } else {
            textPaint.setTextSize(NodeUtils.dp2px(getContext(), smallTextSize));
            int limitWidth = Math.round(textPaint.measureText(child.name, 0, 2));
            StaticLayout layout = new StaticLayout(child.name, textPaint,
                    limitWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
            canvas.save();
            canvas.translate(point.x - layout.getWidth() / 2f, point.y - layout.getHeight() / 2f);
            layout.draw(canvas);
            canvas.restore();
        }


    }

    /**
     * 绘制子节点的子节点
     */
    private void drawNodeChildNode(Canvas canvas, NodeInfo child, int color) {
        if (child != null && child.childes != null && !child.childes.isEmpty()) {
            for (int i = 0; i < child.childes.size(); i++) {
                NodeInfo childInfo = child.childes.get(i);
                Node node = nodeMap.get(childInfo.getNodeId());
                Bitmap nodeBitmap = cacheBitmapMap.get(childInfo.picUrl);
                if (nodeBitmap != null && node != null) {
                    //绘制连接线
                    PointF start = node.getStartPoint();
                    PointF endPoint = node.getCenterPoint();
                    linePath.reset();
                    linePath.moveTo(start.x, start.y);
                    linePath.lineTo(endPoint.x, endPoint.y);
                    canvas.drawPath(linePath, linePaint);

                    //绘制头像的边框
                    bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    int strokeWidth = NodeUtils.dp2px(getContext(), 1.5f);
                    bitmapPaint.setStrokeWidth(strokeWidth);
                    bitmapPaint.setColor(NodeUtils.changeColorAlpha(color, 0.5f));
                    canvas.drawCircle(endPoint.x, endPoint.y, node.getRadius(), bitmapPaint);
                    // 绘制头像
                    float left = endPoint.x - nodeBitmap.getWidth() / 2f;
                    float top = endPoint.y - nodeBitmap.getHeight() / 2f;
                    bitmapPaint.reset();
                    canvas.drawBitmap(nodeBitmap, left, top, bitmapPaint);
                    //绘制头像蒙层
                    bitmapPaint.setColor(Color.parseColor("#4D000000"));
                    canvas.drawCircle(endPoint.x, endPoint.y, node.getRadius(), bitmapPaint);
                    //绘制名字
                    String text = node.getNodeInfo().name;
                    if (!TextUtils.isEmpty(text)) {
                        textPaint.setTextSize(NodeUtils.dp2px(getContext(), textSize_10));
                        textPaint.setColor(Color.parseColor("#555555"));
                        textPaint.getTextBounds(text, 0, text.length(), childTextRect);
                        float x = endPoint.x - childTextRect.width() / 2f;
                        float space = NodeUtils.dp2px(getContext(), rootSpace);
                        float y = top + nodeBitmap.getHeight() + childTextRect.height() + space;
                        canvas.drawText(text, x, y, textPaint);
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
                textPaint.setTextSize(NodeUtils.dp2px(getContext(), normalTextSize));
                textPaint.getTextBounds(info.name, 0, info.name.length(), rootTextRect);
                float x = (getWidth() - rootTextRect.width()) >> 1;
                float space = NodeUtils.dp2px(getContext(), rootSpace);
                float y = top + rootBitmap.getHeight() + rootTextRect.height() + space;
                canvas.drawText(info.name, x, y, textPaint);
            }
        }
    }

    public Node getOtherNode() {
        if (isHasOtherNode()) {
            for (Node node : nodeMap.values()) {
                if (node.getNodeInfo().isOtherNode()) {
                    return node;
                }
            }
        }
        return null;
    }

    public boolean isHasOtherNode() {
        return hasOtherNode;
    }

    @Override
    public Node getNodeByPoint(float x, float y) {
        if (nodeMap != null && !nodeMap.isEmpty()) {
            for (Node node : nodeMap.values()) {
                if (node.getRegion().contains((int) x, (int) y)) {
                    return node;
                }
            }
        }
        return null;
    }


}
