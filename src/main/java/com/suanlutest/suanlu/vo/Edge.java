package com.suanlutest.suanlu.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @BelongsProject: calculationroad
 * @Author: zf
 * @CreateTime: 2019-11-18 15:31
 * @Description: 边的信息
 */
@Data
public class Edge {
    //边的起始节点
    @JSONField(name = "src-ip")
    private String souNodeId;

    //边的结束节点
    @JSONField(name = "dest-ip")
    private String dstNodeId;

    //边的权重
    @JSONField(name = "delay")
    private Double weight;

    @JSONField(name = "src-interface-ip")
    private String srcInterfaceIp;
    @JSONField(name = "dest-interface-ip")
    private String dstInterfaceIp;

    @JSONField(name = "src-area-id")
    private String srcAreaId;

    @JSONField(name = "dest-area-id")
    private String dstAreaId;

    @JSONField(name = "ratio")
    private Double ratio;

    @JSONField(name = "route")
    private Double route;

    @JSONField(name = "metric")
    private Double metric;

    public Edge(String souNodeId, String dstNodeId, Double weight) {
        this.souNodeId = souNodeId;
        this.dstNodeId = dstNodeId;
        this.weight = weight;
    }

    public Edge() {
    }
}