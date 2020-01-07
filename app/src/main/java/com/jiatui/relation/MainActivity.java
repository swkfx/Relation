package com.jiatui.relation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    String[] titles = {"文章", "海报", "宣传册", "名片分享", "名片扫码", "文件", "商品",
            "视频", "文章", "海报", "宣传册", "名片分享", "名片扫码", "文件", "商品", "视频"};
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
        NodeLayout node = findViewById(R.id.clue_node);
        NodeInfo info = new NodeInfo();
        info.name = "刘志文";
        info.picUrl = "https://img.zcool.cn/community/015a465698b54432f87574be965625.png@1280w_1l_2o_100sh.png";
        info.childes = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            NodeInfo child = new NodeInfo();
            if (i < titles.length) {
                child.name = titles[i];
            } else {
                child.name = "超出的";
            }

            Random random = new Random();
            int secondChildCount = random.nextInt(5) + 1;
            Timber.d("secondChildCount=%s", secondChildCount);
            child.childes = new ArrayList<>();
            for (int j = 0; j < secondChildCount; j++) {
                NodeInfo secondChild = new NodeInfo();
                if (j >= urls.length) {
                    secondChild.picUrl = "https://acg.toubiec.cn/random";
                } else {
                    secondChild.picUrl = urls[j];
                }

                secondChild.name = "刘志文";
                child.childes.add(secondChild);
            }
            info.childes.add(child);
        }
        node.addRoot(info, false);
        // node.setNodeInfo(info, RelationUtils.generateChildColor(true), 66);
        // node.setHasOtherNode(true);

    }
}
