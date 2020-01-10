package com.jiatui.relation.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2019/12/28
 *      desc   :
 * </pre>
 */
public class NodeUtils {
    private static List<Integer> defaultColors = new ArrayList<>();
    private static List<Integer> colors;

    static {
        defaultColors.add(Color.parseColor("#73448D"));
        defaultColors.add(Color.parseColor("#8D6644"));
        defaultColors.add(Color.parseColor("#4C5BAB"));
        defaultColors.add(Color.parseColor("#0083BF"));
        defaultColors.add(Color.parseColor("#349C97"));
        defaultColors.add(Color.parseColor("#BE3535"));
        defaultColors.add(Color.parseColor("#D7A82F"));
        defaultColors.add(Color.parseColor("#5CA9FD"));
    }

    public static Point calcPointWithAngle(int x, int y, int radius, float angle) {
        double radian = Math.toRadians(angle);
        int x_ = (int) (x + Math.cos(radian) * radius);
        int y_ = (int) (y + Math.sin(radian) * radius);
        return new Point(x_, y_);
    }

    public static PointF calcPointWithAngleF(float x, float y, int radius, float angle) {
        double radian = Math.toRadians(angle);
        float x_ = Double.valueOf(x + Math.cos(radian) * radius).floatValue();
        float y_ = Double.valueOf(y + Math.sin(radian) * radius).floatValue();
        return new PointF(x_, y_);
    }

    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + .5f);
    }

    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + .5f);
    }

    /**
     * @param reset 是否重置颜色列表
     * @return
     */
    public static int generateChildColor(boolean reset) {
        if (colors == null) {
            colors = new ArrayList<>();
        }
        if (reset) {
            colors.clear();
        }
        if (colors.isEmpty()) {
            colors.addAll(defaultColors);
        }
        Random random = new Random();
        int nextIndex = random.nextInt(colors.size());
        return colors.remove(nextIndex);
    }

    public static int generateChildColor() {
        return generateChildColor(false);
    }

    public static int changeColorAlpha(int color, float percent) {
        int alpha = (int) (Color.alpha(color) * percent);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int getOtherNodeColor() {
        return Color.parseColor("#8D949B");
    }

    public static int getSearchNodeColor() {
        return Color.parseColor("#065B81");
    }
}
