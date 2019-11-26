package com.suanlutest.suanlu.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: suanlu
 * @Author: zf
 * @CreateTime: 2019-11-22 14:29
 * @Description:
 */
@Data
public class ServiceInfo {
    @JsonProperty(value = "src-node-id")
    private String outSrcNodeId;
    @JsonProperty(value = "dst-node-id")
    private String outDstNodeId;
    @JsonProperty(value = "cost")
    private Double outWeight;
    private List<Vertex> path;
    private String type;
    @JsonProperty(value = "src-area-id")
    private String srcAreaId;
    @JsonProperty(value = "dst-area-id")
    private String dstAreaId;


    public ServiceInfo (Edge edge){
        String souNodeId = edge.getSouNodeId();
        String dstNodeId = edge.getDstNodeId();
        Double weight = edge.getWeight();
        this.outDstNodeId = dstNodeId;
        this.outSrcNodeId = souNodeId;
        this.outWeight = weight;
        List<Vertex> list = new ArrayList<>();
        Vertex vertex = new Vertex();
        vertex.setDist(outWeight);
        vertex.setParentNodeId(souNodeId);
        vertex.setNodeId(dstNodeId);
        vertex.setKonw(true);
        vertex.setSrcAreaId(edge.getSrcAreaId());
        vertex.setDstAreaId(edge.getDstAreaId());
        vertex.setSrcInterfaceIp(edge.getSrcInterfaceIp());
        vertex.setDstInterfaceIp(edge.getDstInterfaceIp());
        list.add(vertex);
        this.path = list;
    }

    public ServiceInfo() {
    }
}