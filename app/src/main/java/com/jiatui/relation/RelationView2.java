package com.jiatui.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RelationView2 extends ViewGroup {

    private int childCount;
    private ImageView centerView;
    private int lineLength = dp2px(150);
    private List<ImageView> childs;
    private int radius = dp2px(50);
    private Paint mPaint;

    public RelationView2(Context context) {
        this(context, null);
    }

    public RelationView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelationView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        childs = new ArrayList<>();
        childCount = 8;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));

        centerView = new ImageView(context);
        centerView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        LayoutParams params = new LayoutParams(radius, radius);
        centerView.setLayoutParams(params);
        centerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("centerView onClick");
            }
        });
        addView(centerView);

        for (int i = 0; i < childCount; i++) {
            ImageView child = new ImageView(context);
            int size = dp2px(30);
            LayoutParams childParams = new LayoutParams(size, size);
            child.setLayoutParams(childParams);
            child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            final int finalI = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("child[%s] onClick", finalI);
                }
            });
            childs.add(child);
            addView(child);
        }
        Timber.d("init...");
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChild(centerView, widthMeasureSpec, heightMeasureSpec);
        for (ImageView child : childs) {
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        int cx = width / 2;
        int cy = height / 2;
        int centerWidth = centerView.getMeasuredWidth();
        int centerHeight = centerView.getMeasuredHeight();
        centerView.layout(cx - centerWidth / 2, cy - centerHeight / 2, cx + centerWidth / 2, cy + centerHeight / 2);
        int size = childs.size();
        for (int i = 1; i <= size; i++) {
            int childIndex = i - 1;
            ImageView child = childs.get(childIndex);
            int childRadius = child.getMeasuredWidth() / 2;
            double angle = 360 / size;
            Point point = RelationUtils.calcPointWithAngle(cx, cy, lineLength, angle * i);
            child.layout(point.x - childRadius, point.y - childRadius, point.x + childRadius, point.y + childRadius);
            Timber.d("onLayout:[%s] => l:%s,t:%s,r:%s,b:%s", childIndex, l, t, r, b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        int size = childs.size();
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        for (int i = 1; i <= size; i++) {
            double angle = 360 / size;
            Point point = RelationUtils.calcPointWithAngle(cx, cy, lineLength, angle * i);
            canvas.drawLine(cx, cy, point.x, point.y, mPaint);
        }

        super.onDraw(canvas);


    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + .5f);
    }


}
