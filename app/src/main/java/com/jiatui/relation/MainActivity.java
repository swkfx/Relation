package com.jiatui.relation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    String[] titles = {"文章", "海报", "宣传册", "名片分享", "名片扫码", "文件"};
    String[] urls = {
            "https://acg.toubiec.cn/random",
            "http://tva2.sinaimg.cn/large/a15b4afegy1fmvjhu7kbgj21hc0u0qns.jpg",
            "http://tva3.sinaimg.cn/large/0072Vf1pgy1foxkiijgosj31hc0u0140.jpg",
            "http://tva2.sinaimg.cn/large/a15b4afegy1fmvjgbajynj21hc0u07du.jpg",
            "http://tva1.sinaimg.cn/large/a15b4afegy1fmvk7jys0cj21hc0u0k53.jpg",
            "http://tva1.sinaimg.cn/large/0072Vf1pgy1foxlnk8bzcj31hc0u0qia.jpg",
            "http://tva2.sinaimg.cn/large/a15b4afegy1fmvjlvdc24j21hc0u0ngw.jpg",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);
//        ClueNode node = findViewById(R.id.clue_node);
//        NodeInfo info = new NodeInfo();
//        info.url = "http://ww1.sinaimg.cn/large/0065oQSqly1g2pquqlp0nj30n00yiq8u.jpg";
//        info.text = "刘志文";
//        info.childes = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            NodeInfo child = new NodeInfo();
//            child.text = titles[i];
//            Random random = new Random();
//            int secondChildCount = random.nextInt(5) + 1;
//            Timber.d("secondChildCount=%s", secondChildCount);
//            child.childes = new ArrayList<>();
//            for (int j = 0; j < secondChildCount; j++) {
//                NodeInfo secondChild = new NodeInfo();
//                if (j >= urls.length) {
//                    secondChild.url = "https://acg.toubiec.cn/random";
//                } else {
//                    secondChild.url = urls[j];
//                }
//
//                secondChild.text = "刘志文";
//                child.childes.add(secondChild);
//            }
//            info.childes.add(child);
//        }
//        node.setNodeInfo(info);

    }
}
