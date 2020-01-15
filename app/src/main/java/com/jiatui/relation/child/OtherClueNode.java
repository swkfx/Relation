package com.jiatui.relation.child;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

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
public class OtherClueNode extends BaseNodeView {

    private float startAngle;
    private NodeInfo info;
    private int nodeChildSize = 40;//dp
    private int normalTextSize = 16;//dp
    private int smallTextSize = 12;//dp
    private Paint bitmapPaint;
    private TextPaint textPaint;
    private Paint linePaint;
    private Rect rootTextRect;
    private Rect childTextRect;
    private Path linePath;

    // private List<Node> nodes;
    private Map<String, Node> nodeMap;

    // private Map<String, Integer> map = new HashMap<>();

    public OtherClueNode(Context context) {
        this(context, null);
    }

    public OtherClueNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OtherClueNode(Context context, AttributeSet attrs, int defStyle) {
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

    }

    public void setNodeInfo(NodeInfo info) {
        this.info = info;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initNodes();
        Timber.d("onSizeChanged:%s-%s-%s-%s-w%s-h%s", getLeft(), getTop(), getRight(), getBottom(), w, h);
    }

    private void initNodes() {
        if (nodeMap == null) {
            nodeMap = new HashMap<>();
        } else {
            nodeMap.clear();
        }

        PointF startPoint = new PointF(getWidth() / 2f, getHeight() / 2f);

        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            int size = Math.min(13, info.childes.size());
            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
                int count = Math.min(6, info.childes.size());
                float angle;
                int multiple = i / 6 + 1;
                int position = i % 6;
                float offsetAngle = multiple > 2 ? 45 : 360f / count / 2 * multiple;//设计稿起始偏移角度//1圈-30 2圈-60 3圈-45
                angle = 360f / count * position + offsetAngle;
                int distance;
                if (multiple == 1) {
                    distance = NodeUtils.dp2px(getContext(), 90);
                } else if (multiple == 2) {
                    distance = NodeUtils.dp2px(getContext(), 140);
                } else {
                    distance = NodeUtils.dp2px(getContext(), 180);
                }

                int radius = NodeUtils.dp2px(getContext(), nodeChildSize) / 2;
                child.nodeType = NodeInfo.TYPE.ATLAS;
                Rect f = new Rect(getLeft(), getTop(), getRight(), getBottom());
                Node node = new Node(startPoint, angle, distance, radius, child, f);
                node.setColor(NodeUtils.generateChildColor(i == 0));
                nodeMap.put(child.getNodeId(), node);
            }
        }
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
        //debug draw startAngle line
        // debugDrawChild(canvas);

        //draw childes
        drawChildes(canvas);

        //draw 中心的rootView
        drawRoot(canvas);
    }

    private void debugDrawChild(Canvas canvas) {
        // int x = getWidth() / 2;
        // int y = getHeight() / 2;
        // Point point = NodeUtils.calcPointWithAngle(x, y, 2000, startAngle);
        // linePath.reset();
        // linePath.moveTo(x, y);
        // linePath.lineTo(point.x, point.y);
        // canvas.drawPath(linePath, linePaint);
        //
        // if (nodes != null && !nodes.isEmpty()) {
        //     for (Node node : nodes) {
        //         canvas.drawRect(node.getRect(), textPaint);
        //     }
        // }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    private void drawChildes(Canvas canvas) {
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        if (info.childes != null && !info.childes.isEmpty()) {
            int size = Math.min(13, info.childes.size());
            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
                Node node = nodeMap.get(child.getNodeId());
                //绘制连接线
                PointF point = node.getCenterPoint();
                linePath.reset();
                linePath.moveTo(x, y);
                float distanceX = (point.x - x);
                float distanceY = (point.y - y);
                //获取目标坐标的{x,y}
                float originX = x + distanceX * getTransProgress();
                float originY = y + distanceY * getTransProgress();
                linePath.lineTo(originX, originY);
                canvas.drawPath(linePath, linePaint);
                int color = node.getColor();
                //绘制子节点
                drawNodeChild(canvas, child, color, originX, originY);
            }
        }
    }

    private void drawNodeChild(Canvas canvas, NodeInfo child, int color, float originX, float originY) {

        bitmapPaint.reset();
        boolean hasBorder = child.childes != null && !child.childes.isEmpty();
        int size = NodeUtils.dp2px(getContext(), nodeChildSize);
        float radius = size / 2;
        if (hasBorder) {
            bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            int strokeWidth = NodeUtils.dp2px(getContext(), 3);
            bitmapPaint.setStrokeWidth(strokeWidth);
            bitmapPaint.setColor(NodeUtils.changeColorAlpha(color, 0.5f));
            canvas.drawCircle(originX, originY, radius + strokeWidth, bitmapPaint);
        }
        bitmapPaint.setColor(color);
        bitmapPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(originX, originY, radius, bitmapPaint);
        // 需要考虑字符串长度大于node宽度的情况
        int length = child.name.length();
        textPaint.setColor(Color.WHITE);
        if (length <= 3) {
            textPaint.setTextSize(NodeUtils.dp2px(getContext(), length < 3 ? normalTextSize :
                    smallTextSize));
            textPaint.getTextBounds(child.name, 0, length, childTextRect);
            float x = originX - childTextRect.exactCenterX();
            float y = originY - childTextRect.exactCenterY();
            canvas.drawText(child.name, x, y, textPaint);
        } else {
            textPaint.setTextSize(NodeUtils.dp2px(getContext(), smallTextSize));
            int limitWidth = Math.round(textPaint.measureText(child.name, 0, 2));
            StaticLayout layout = new StaticLayout(child.name, textPaint,
                    limitWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
            // TODO: 2020-01-06 宽度做限制
            canvas.save();
            canvas.translate(originX - (float) layout.getWidth() / 2, originY - (float) layout.getHeight() / 2);
            layout.draw(canvas);
            canvas.restore();
        }


    }


    private void drawRoot(Canvas canvas) {
        int color = NodeUtils.getOtherNodeColor();
        int size = NodeUtils.dp2px(getContext(), nodeChildSize);
        float r = size / 2f;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        int strokeWidth = NodeUtils.dp2px(getContext(), 3);
        bitmapPaint.setStrokeWidth(strokeWidth);
        bitmapPaint.setColor(NodeUtils.changeColorAlpha(color, 0.5f));
        canvas.drawCircle(cx, cy, r + strokeWidth, bitmapPaint);

        bitmapPaint.reset();
        bitmapPaint.setColor(color);
        bitmapPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, r, bitmapPaint);

        String text = "其他";
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(NodeUtils.dp2px(getContext(), normalTextSize));
        textPaint.getTextBounds(text, 0, text.length(), rootTextRect);
        float textX = cx - rootTextRect.exactCenterX();
        float textY = cy - rootTextRect.exactCenterY();
        canvas.drawText(text, textX, textY, textPaint);
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
