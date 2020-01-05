package com.jiatui.relation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;

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
public class RelationUtils {
    private static List<Integer> defaultColors = new ArrayList<>(7);
    private static List<Integer> colors;

    static {
        defaultColors.add(Color.parseColor("#73448D"));
        defaultColors.add(Color.parseColor("#8D6644"));
        defaultColors.add(Color.parseColor("#4C5BAB"));
        defaultColors.add(Color.parseColor("#0083BF"));
        defaultColors.add(Color.parseColor("#349C97"));
        defaultColors.add(Color.parseColor("#BE3535"));
        defaultColors.add(Color.parseColor("#D7A82F"));
    }

    public static Point calcPointWithAngle(Point center, int radius, double angle) {
        double radian = Math.toRadians(angle);
        int x = (int) (center.x + Math.cos(radian) * radius);
        int y = (int) (center.y + Math.sin(radian) * radius);
        return new Point(x, y);
    }

    public static Point calcPointWithAngle(int x, int y, int radius, double angle) {
        double radian = Math.toRadians(angle);
        int x_ = (int) (x + Math.cos(radian) * radius);
        int y_ = (int) (y + Math.sin(radian) * radius);
        return new Point(x_, y_);
    }

    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + .5f);
    }

    public static int generateChildColor() {
        if (colors == null) {
            colors = new ArrayList<>();
        }
        if (colors.isEmpty()) {
            colors.addAll(defaultColors);
        }
        Random random = new Random();
        int nextIndex = random.nextInt(colors.size());
        return colors.remove(nextIndex);
    }

    public static int changeColorAlpha(int color, float percent) {
        int alpha = (int) (Color.alpha(color) * percent);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

}
