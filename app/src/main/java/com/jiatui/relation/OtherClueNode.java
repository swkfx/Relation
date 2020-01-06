package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2020/1/4
 *      desc   :
 * </pre>
 */
public class OtherClueNode extends View {

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
        textPaint.setTextSize(RelationUtils.dp2px(context, normalTextSize));
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(RelationUtils.dp2px(context, 1));
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
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        Point point = RelationUtils.calcPointWithAngle(x, y, 2000, startAngle);
        linePath.reset();
        linePath.moveTo(x, y);
        linePath.lineTo(point.x, point.y);
        canvas.drawPath(linePath, linePaint);
    }

    private void drawChildes(Canvas canvas) {
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        if (info.childes != null && !info.childes.isEmpty()) {
            for (int i = 0; i < info.childes.size(); i++) {
                NodeInfo child = info.childes.get(i);
                //绘制连接线
                int count = Math.min(6, info.childes.size());
                float angle;
                int multiple = i / 6 + 1;
                int position = i % 6;
                float offsetAngle = multiple > 2 ? 45 : 360f / count / 2 * multiple;//设计稿起始偏移角度//1圈-30 2圈-60 3圈-45
                angle = 360f / count * position + offsetAngle;
                int radius;
                if (multiple == 1) {
                    radius = RelationUtils.dp2px(getContext(), 90);
                } else if (multiple == 2) {
                    radius = RelationUtils.dp2px(getContext(), 140);
                } else {
                    radius = RelationUtils.dp2px(getContext(), 180);
                }
                Point point = RelationUtils.calcPointWithAngle(x, y, radius, angle);
                linePath.reset();
                linePath.moveTo(x, y);
                linePath.lineTo(point.x, point.y);
                canvas.drawPath(linePath, linePaint);
                //绘制 node的 child
                //childLineAngle
                int color = RelationUtils.generateChildColor(i == 0);
                drawNodeChild(canvas, point, child, color);
            }
        }
    }

    private void drawNodeChild(Canvas canvas, Point point, NodeInfo child, int color) {
        bitmapPaint.reset();
        boolean hasBorder = child.childes != null && !child.childes.isEmpty();
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
            // TODO: 2020-01-06 宽度做限制
            canvas.save();
            canvas.translate(point.x - layout.getWidth() / 2, point.y - layout.getHeight() / 2);
            layout.draw(canvas);
            canvas.restore();
        }


    }


    private void drawRoot(Canvas canvas) {
        int color = RelationUtils.getOtherNodeColor();
        int size = RelationUtils.dp2px(getContext(), nodeChildSize);
        float r = size / 2f;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        bitmapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        int strokeWidth = RelationUtils.dp2px(getContext(), 3);
        bitmapPaint.setStrokeWidth(strokeWidth);
        bitmapPaint.setColor(RelationUtils.changeColorAlpha(color, 0.5f));
        canvas.drawCircle(cx, cy, r + strokeWidth, bitmapPaint);

        bitmapPaint.reset();
        bitmapPaint.setColor(color);
        bitmapPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, r, bitmapPaint);

        String text = "其他";
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(RelationUtils.dp2px(getContext(), normalTextSize));
        textPaint.getTextBounds(text, 0, text.length(), rootTextRect);
        float textX = cx - rootTextRect.exactCenterX();
        float textY = cy - rootTextRect.exactCenterY();
        canvas.drawText(text, textX, textY, textPaint);
    }
}