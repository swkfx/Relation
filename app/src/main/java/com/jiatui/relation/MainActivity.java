package com.jiatui.relation;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jiatui.relation.model.Node;
import com.jiatui.relation.model.NodeInfo;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
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

    String prefix = "C100-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);
        final NodeLayout node = findViewById(R.id.clue_node);
        node.setRoot(mockRootNodeInfo(), false);
        // node.setNodeInfo(info, NodeUtils.generateChildColor(true), 66);
        // node.setHasOtherNode(true);
        node.setNodeClickListener(new NodeLayout.NodeClickListener() {
            @Override
            public void onNodeClick(Node nodeData) {
                if (nodeData.getNodeInfo().nodeType == NodeInfo.TYPE.ATLAS) {
                    if (nodeData.getNodeInfo().isOtherNode()) {
                        if (nodeData.getNodeInfo().isExpand) {
                            Timber.d("该已经展开");
                            node.moveCenterPoint(nodeData.getEndPoint());
                        } else {
                            node.addOtherClueNode(nodeData, mockOtherNode());
                        }
                    } else {
                        Timber.d("点击了节点[%s]", nodeData.getNodeInfo().name);
                        if (nodeData.getNodeInfo().isExpand) {
                            Timber.d("该已经展开");
                            node.moveCenterPoint(nodeData.getEndPoint());
                        } else {
                            node.addUsersNode(nodeData, mockUserData(nodeData.getNodeInfo()));
                        }
                    }
                } else {
                    Timber.d("点击了线索[%s]", nodeData.getNodeInfo().name);
                    if (nodeData.getNodeInfo().isExpand) {
                        Timber.d("该已经展开");
                        node.moveCenterPoint(nodeData.getEndPoint());
                    } else {
                        node.addAtlasNode(nodeData, mockAtlasData(nodeData.getNodeInfo()), true);

                    }
                }
            }

            @Override
            public void onRootNodeClick(Node node) {
                Timber.d("RootNode Click [%s]", node.getNodeInfo().name);
            }
        });

        // findViewById(R.id.button).setVisibility(View.GONE);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // node.setSearchNode(mockRootNodeInfo(), true);
                node.reset();
            }
        });

    }

    private NodeInfo mockOtherNode() {
        final NodeInfo info = NodeInfo.generateOther();
        info.childes = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            NodeInfo child = new NodeInfo();
            if (i < titles.length) {
                child.name = titles[i];
            } else {
                child.name = "超出的";
            }
            child.sourceType = prefix + i;
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
                secondChild.name = getRandomName(2);
                secondChild.userId = child.sourceType + secondChild.name.hashCode();
                child.childes.add(secondChild);
            }
            info.childes.add(child);
        }
        return info;
    }

    @NotNull
    private NodeInfo mockRootNodeInfo() {
        final NodeInfo info = new NodeInfo();
        info.name = getRandomName(2);
        info.userId = prefix + info.name.hashCode();
        info.picUrl = "https://img.zcool.cn/community/015a465698b54432f87574be965625.png@1280w_1l_2o_100sh.png";
        info.childes = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            NodeInfo child = new NodeInfo();
            child.sourceType = prefix + i;
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
                secondChild.name = getRandomName(2);
                secondChild.userId = prefix + secondChild.name.hashCode();
                child.childes.add(secondChild);
            }
            info.childes.add(child);
        }
        return info;
    }

    private NodeInfo mockAtlasData(NodeInfo ni) {
        NodeInfo info = new NodeInfo();
        info.name = ni.name;
        info.picUrl = ni.picUrl;
        info.childes = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            NodeInfo child = new NodeInfo();
            child.sourceType = prefix + i;
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
                secondChild.name = getRandomName(2);
                secondChild.userId = prefix + secondChild.name.hashCode();
                child.childes.add(secondChild);
            }
            info.childes.add(child);
        }
        return info;
    }

    private NodeInfo mockUserData(NodeInfo nodeInfo) {
        final NodeInfo info = new NodeInfo();
        info.name = nodeInfo.name;
        info.userId = nodeInfo.userId;
        info.sourceType = nodeInfo.sourceType;
        info.childes = new ArrayList<>();
        Random random = new Random();
        int count = random.nextInt(30);
        for (int i = 0; i < count; i++) {
            NodeInfo child = new NodeInfo();
            child.name = getRandomName(3);
            child.userId = prefix + child.name.hashCode();
            if (i < urls.length) {
                child.picUrl = urls[i];
            } else {
                child.picUrl = "https://img.zcool.cn/community/015a465698b54432f87574be965625" +
                        ".png@1280w_1l_2o_100sh.png";
            }
            info.childes.add(child);
        }
        return info;
    }

    private String getRandomName(int len) {
        StringBuilder randomName = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String str = null;
            int heightPos, lowPos; // 定义高低位
            Random random = new Random();
            heightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            byte[] b = new byte[2];
            b[0] = (Integer.valueOf(heightPos).byteValue());
            b[1] = (Integer.valueOf(lowPos).byteValue());
            try {
                str = new String(b, "GBK"); // 转成中文
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            randomName.append(str);
        }
        return randomName.toString();
    }
}
