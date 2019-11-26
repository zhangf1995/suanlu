package com.suanlutest.suanlu.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @BelongsProject: calculationroad
 * @Author: zf
 * @CreateTime: 2019-11-18 15:35
 * @Description: 第一个节点到这个节点距离
 */
@Data
public class Vertex implements Cloneable{
    @JsonProperty(value = "dst-node-id")
    private String nodeId;
    private Double dist;
    private Boolean konw;
    @JsonProperty(value = "src-node-id")
    private String parentNodeId;
    @JsonProperty(value = "src-area-id")
    private String srcAreaId;
    @JsonProperty(value = "dst-area-id")
    private String dstAreaId;
    @JsonProperty(value = "src-node-port-id")
    private String srcInterfaceIp;
    @JsonProperty(value = "dst-node-port-id")
    private String dstInterfaceIp;
    private final static Double infinite_dis = Double.MAX_VALUE;

    public Vertex(String nodeId) {
        this.nodeId = nodeId;
        this.dist = infinite_dis;
        this.konw = false;
        this.parentNodeId = null;
    }

    public void resotre(){
        this.dist = infinite_dis;
        this.konw = false;
        this.parentNodeId = null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Vertex() {
    }
}