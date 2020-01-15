package com.jiatui.relation.model;

import android.text.TextUtils;

import java.util.List;

/**
 * <pre>
 *      author : fangx
 *      e-mail : fangx@hyxt.com
 *      time   : 2020/1/4
 *      desc   :
 * </pre>
 */
public class NodeInfo {

    public String sourceType;
    public String userId;
    public String picUrl;
    public String name;
    public List<NodeInfo> childes;
    public @TYPE
    int nodeType;
    public boolean isExpand;
    public boolean isRoot;


    public @interface TYPE {
        int USER = 0;
        int ATLAS = 1;
    }

    public boolean isOtherNode() {
        return TextUtils.equals("C0000", sourceType);
    }


    public static NodeInfo generateOther() {
        NodeInfo info = new NodeInfo();
        info.sourceType = "C0000";
        info.name = "其他";
        info.nodeType = NodeInfo.TYPE.ATLAS;
        return info;
    }

    public String getNodeId() {
        // if (TextUtils.isEmpty(sourceType)) {
        //     return userId;
        // } else {
        //     return sourceType;
        // }
        String u = TextUtils.isEmpty(userId) ? "" : userId;
        String s = TextUtils.isEmpty(sourceType) ? "" : sourceType;
        return u + s;
    }


}
