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

import timber.log.Timber;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2020/1/4
 *      desc   :
 * </pre>
 */
public class UsersNode extends View {

    private float startAngle;
    private NodeInfo info;
    private int rootColor;

    private int nodeChildSize = 40;//dp
    private int nodeChildNodeSize = 24;//dp
    private int nodeChildNodeSizeLarge = 32;//dp
    private int normalTextSize = 16;//dp
    private int textSize_10 = 10;//dp
    private int rootSpace = 2;//dp
    private int lineDistance = 90;//dp
    private int middleLineDistance = 120;//dp
    private int longLineDistance = 150;//dp
    private Paint bitmapPaint;
    private TextPaint textPaint;
    private Paint linePaint;
    private Rect rootTextRect;
    private Rect childTextRect;
    private Path linePath;
    private Map<String, Bitmap> cacheBitmapMap;

    private int circleCount = 15;
    private int NAME_MAX_LENGTH = 3;


    public UsersNode(Context context) {
        this(context, null);
    }

    public UsersNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UsersNode(Context context, AttributeSet attrs, int defStyle) {
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

    public void setNodeInfo(NodeInfo info, int rootColor, float startAngle) {
        this.info = info;
        this.rootColor = rootColor;
        this.startAngle = startAngle;
        loadNodeChildBitmap(info);
        invalidate();
    }

    private void loadNodeChildBitmap(NodeInfo info) {
        if (info != null && info.childes != null && !info.childes.isEmpty()) {
            for (NodeInfo child : info.childes) {
                if (!TextUtils.isEmpty(child.picUrl)) {
                    int size = RelationUtils.dp2px(getContext(), info.childes.size() >
                            circleCount ? nodeChildNodeSize : nodeChildNodeSizeLarge);
                    if (cacheBitmapMap.get(child.picUrl) == null) {
                        loadBitmap(child.picUrl, size);
                    } else {
                        invalidate();
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

        debugDrawChild(canvas);

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
            int size = Math.min(29, info.childes.size());

            for (int i = 0; i < size; i++) {
                NodeInfo child = info.childes.get(i);
                //绘制连接线
                int count = Math.min(circleCount, info.childes.size());
                float angle;
                int multiple = i / circleCount + 1;
                int position = i % count;
                float offsetAngle = 360f / count / 2;//设计稿起始偏移角度
                angle = startAngle + 360f / count * position + offsetAngle * multiple;
                int radius;
                if (i < circleCount) {
                    if (info.childes.size() < circleCount) {
                        radius = RelationUtils.dp2px(getContext(), lineDistance);
                    } else {
                        radius = RelationUtils.dp2px(getContext(), middleLineDistance);
                    }
                } else {
                    radius = RelationUtils.dp2px(getContext(), longLineDistance);
                }
                Point point = RelationUtils.calcPointWithAngle(x, y, radius, angle);
                linePath.reset();
                linePath.moveTo(x, y);
                linePath.lineTo(point.x, point.y);
                canvas.drawPath(linePath, linePaint);
                //绘制 node的 child
                drawNodeChild(canvas, point, child);
            }
        }
    }

    private void drawNodeChild(Canvas canvas, Point point, NodeInfo child) {
        if (child != null) {
            Bitmap nodeBitmap = cacheBitmapMap.get(child.picUrl);
            if (nodeBitmap != null) {
                // 绘制头像
                float left = point.x - nodeBitmap.getWidth() / 2;
                float top = point.y - nodeBitmap.getHeight() / 2;
                bitmapPaint.reset();
                canvas.drawBitmap(nodeBitmap, left, top, bitmapPaint);
                //绘制名字
                if (!TextUtils.isEmpty(child.name)) {
                    textPaint.setTextSize(RelationUtils.dp2px(getContext(), textSize_10));
                    textPaint.setColor(Color.parseColor("#555555"));
                    textPaint.getTextBounds(child.name, 0, child.name.length(), childTextRect);
                    int limitWidth = Math.round(textPaint.measureText(child.name, 0, child.name.length()));
                    StaticLayout layout = new StaticLayout(child.name, 0, child.name.length(), textPaint,
                            limitWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true,
                            TextUtils.TruncateAt.END, limitWidth);
                    //todo 名字宽度做限制。

                    // float x = point.x - childTextRect.width() / 2;
                    // float y = top + nodeBitmap.getHeight() + childTextRect.height() + space;
                    float space = RelationUtils.dp2px(getContext(), rootSpace);
                    canvas.save();
                    canvas.translate(point.x - layout.getWidth() / 2, point.y + layout.getHeight() + space);
                    layout.draw(canvas);
                    canvas.restore();
                }
            }
        }
    }


    private void drawRoot(Canvas canvas) {
        if (this.info == null) {
            return;
        }
        int color = this.rootColor;
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

        String text = this.info.name;
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(RelationUtils.dp2px(getContext(), normalTextSize));
        textPaint.getTextBounds(text, 0, text.length(), rootTextRect);
        float textX = cx - rootTextRect.exactCenterX();
        float textY = cy - rootTextRect.exactCenterY();
        canvas.drawText(text, textX, textY, textPaint);
    }
}
