package com.jiatui.relation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    String[] defaultColors = {
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7"
    };

    List<String> colors = new ArrayList<>();

    @Test
    public void addition_isCorrect() {
        for (int i = 0; i < 13; i++) {
            int count = 6;
            float offsetAngle = 360f / count / 2;//设计稿起始偏移角度
            int multiple = i / 6 + 1;
            int position = i % 6;
            float angle = 360f / count * position + offsetAngle * multiple;
            System.out.println("[" + i + "]" + "angle:" + angle);
        }

        // for (int i = 0; i < 8; i++) {
        //
        //     if (colors.isEmpty()) {
        //         colors.addAll(Arrays.asList(defaultColors));
        //     }
        //
        //     Random random = new Random();
        //     int nextIndex = random.nextInt(colors.size());
        //     String color = colors.remove(nextIndex);
        //     System.out.println("color:" + color);
        // }
    }
}