package com.suanlutest.suanlu.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suanlutest.suanlu.service.impl.SuanluServiceImpl;
import com.suanlutest.suanlu.vo.Edge;
import com.suanlutest.suanlu.vo.ServiceInfo;
import com.suanlutest.suanlu.vo.Vertex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: suanlu
 * @Author: zf
 * @CreateTime: 2019-11-21 10:21
 * @Description:
 */
@Slf4j
public class DataUtils {

    //图的顶节点集
    public static Map<String,Vertex> verMap = new HashMap<>();
    //图的每个顶点对应的有向边
    public static Map<String, List<Edge>> verEdgeMap = new HashMap<>();

    /**
     * 最优算法
     * @param souNodeId
     * @param dstNodeId
     * @return
     */
    public static JSONObject bestRoad(String souNodeId, String dstNodeId) throws Exception {
        JSONObject returnJson = new JSONObject();
        JSONArray returnArray = new JSONArray();
        Vertex souVertex = verMap.get(souNodeId);
        if(ObjectUtils.isEmpty(souVertex)){
            throw new Exception("souVertex is null");
        }
        souVertex.setDist(Double.valueOf(0));
        //更新领接表每个节点
        updateChildren(souVertex);
        Vertex dstVertex = verMap.get(dstNodeId);
        if(ObjectUtils.isEmpty(dstVertex)){
            log.info("souNodeId is {},dstNodeId is {}",souNodeId,dstNodeId);
            throw new Exception("nodeId is not Exist");
        }
        //returnArray.add(dstVertex);
        int size = verMap.size();
        modifyVertex(returnArray,dstVertex);
        log.info("bestRoadOfDelay {}",dstVertex.getDist());
        while(!StringUtils.isEmpty(dstVertex.getParentNodeId()) && (!dstVertex.getNodeId().equals(souNodeId))){
            dstVertex = verMap.get(dstVertex.getParentNodeId());
            if(!StringUtils.isEmpty(dstVertex.getParentNodeId()) && (!dstVertex.getNodeId().equals(souNodeId))){
                modifyVertex(returnArray,dstVertex);
            }
        }

        List<Vertex> vertexList = JSONObject.parseArray(returnArray.toJSONString(), Vertex.class);
        //返回格式整理
        managerData(returnJson,vertexList,souNodeId,dstNodeId);
        returnJson.put("path",returnArray);
        log.info("bestRoadOfDelay,returnJson is {}",returnJson);
        return returnJson;
    }

    public static void modifyVertex(JSONArray array,Vertex vertex) throws Exception {
        Vertex cloneVer = (Vertex) vertex.clone();
        String nodeId = cloneVer.getNodeId();
        String parentNodeId = cloneVer.getParentNodeId();
        List<ServiceInfo> serviceInfoList = SuanluServiceImpl.serviceInfoList;
        serviceInfoList.forEach(serviceInfo -> {
            if(nodeId.equals(serviceInfo.getOutDstNodeId()) && parentNodeId.equals(serviceInfo.getOutSrcNodeId())){
                array.addAll(serviceInfo.getPath());
                return;
            }
        });
    }

    /**
     * 整理数据
     * @param returnJson
     * @param souNodeId
     * @param dstNodeId
     */
    private static void managerData(JSONObject returnJson, List<Vertex> vertexList, String souNodeId, String dstNodeId) {
        double maxDist = vertexList.stream().mapToDouble(Vertex::getDist).sum();
        returnJson.put("cost",maxDist);
        returnJson.put("src-node-id",souNodeId);
        returnJson.put("dst-node-id",dstNodeId);
    }


    /**
     * 从初始节点开始递归更新领接表
     */
    public static void updateChildren(Vertex v) throws Exception{
        int size = verMap.size();
        if(ObjectUtils.isEmpty(v) || ObjectUtils.isEmpty(verEdgeMap.get(v.getNodeId())) || verEdgeMap.get(v.getNodeId()).size() == 0){
            return;
        }
        List<Vertex> childList = new ArrayList<>();
        List<Edge> edgeList = verEdgeMap.get(v.getNodeId());
        edgeList.forEach(edge -> {
            Vertex dstVertex = verMap.get(edge.getDstNodeId());
            if(false == dstVertex.getKonw()){
                dstVertex.setKonw(true);
                dstVertex.setDist(v.getDist()+ edge.getWeight());
                dstVertex.setParentNodeId(v.getNodeId());
                childList.add(dstVertex);
            }else {
                double nowDist = v.getDist() + edge.getWeight();
                if (nowDist < dstVertex.getDist()) {
                    dstVertex.setDist(nowDist);
                    dstVertex.setParentNodeId(edge.getSouNodeId());
                    childList.add(dstVertex);
                }
            }
        });

        for (Vertex vertex : childList) {
            updateChildren(vertex);
        }
      /*  childList.forEach(vertex -> {

        });*/
    }
}