package com.suanlutest.suanlu.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suanlutest.suanlu.constant.SuanluConstant;
import com.suanlutest.suanlu.service.SuanluService;
import com.suanlutest.suanlu.utils.DataUtils;
import com.suanlutest.suanlu.utils.ReadFileUtils;
import com.suanlutest.suanlu.utils.HttpClientUtils;
import com.suanlutest.suanlu.vo.CacuRoute;
import com.suanlutest.suanlu.vo.Edge;
import com.suanlutest.suanlu.vo.ServiceInfo;
import com.suanlutest.suanlu.vo.Vertex;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @BelongsProject: suanlu
 * @Author: zf
 * @CreateTime: 2019-11-21 10:02
 * @Description:
 */
@Slf4j
@Service
public class SuanluServiceImpl implements SuanluService {

    @Value("${url.a}")
    private String aUrl;
    @Value("${url.b}")
    private String bUrl;
    @Value("${url.c}")
    private String cUrl;

    //此处用本地线程localthread也可以
    private static ReentrantLock lock = new ReentrantLock();

    public static List<ServiceInfo> serviceInfoList = new ArrayList<>();

    public static final String testUrl = "http://localhost:8092/cacuRoute/cacuRouteList";

    @Override
    public JSONObject combineData(JSONObject json, String fileName, String type) throws Exception {
        log.info("come in combineData,fileName is {},json is {}", fileName, json.toJSONString());
        String requestId = json.getString("request-id");
        validateParam(requestId);
        String srcAreaId = json.getString("src-area-id");
        validateParam(srcAreaId);
        String srcNodeId = json.getString("src-node-id");
        validateParam(srcNodeId);
        String dstNodeId = json.getString("dst-node-id");
        validateParam(dstNodeId);
        String dstAreaId = json.getString("dst-area-id");
        validateParam(dstAreaId);
        String routineType = json.getString("routing-policy");
        validateParam(routineType);
        String slaType = json.getString("sla-type");
        String delay = json.getString("delay");
        String metric = json.getString("metric");
        String route = json.getString("route");
        String ratio = json.getString("ratio");
        validateParam(slaType);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("souNodeId", srcNodeId);
        jsonObject.put("dstNodeId", dstNodeId);
        jsonObject.put("cacuType", routineType);
        jsonObject.put("srcAreaId", srcAreaId);
        jsonObject.put("dstAreaId", dstAreaId);
        jsonObject.put("delayPro", delay);
        jsonObject.put("metricPro", metric);
        jsonObject.put("routePro", route);
        jsonObject.put("ratioPro", ratio);
        jsonObject.put("slaType",slaType);


        JSONObject endJson = new JSONObject();

        if(srcAreaId.equals(dstAreaId)){
            JSONObject singleJson = null;
            if(srcAreaId.equals("ggw")){
                jsonObject.put("isSingle",true);
                singleJson = HttpClientUtils.sendPost(cUrl, jsonObject);
            }else if(srcAreaId.equals("cywa")){
                jsonObject.put("isSingle",true);
                singleJson = HttpClientUtils.sendPost(aUrl, jsonObject);
            }else if(srcAreaId.equals("cywb")){
                jsonObject.put("isSingle",true);
                singleJson = HttpClientUtils.sendPost(bUrl, jsonObject);
            }
            JSONObject dataJson = singleJson.getJSONObject("data");
            if(!ObjectUtils.isEmpty(dataJson)){
                JSONArray pathArr = dataJson.getJSONArray("path");
                List<ServiceInfo> serviceInfos = JSONObject.parseArray(pathArr.toJSONString(), ServiceInfo.class);
                serviceInfos.forEach(serviceInfo -> {
                    serviceInfo.setSrcAreaId(srcAreaId);
                    serviceInfo.setDstAreaId(dstAreaId);
                });
                endJson.put("paths",serviceInfos);
                endJson.put("request-id", requestId);
            }
            //endJson.put("")
            return endJson;
        }else{
            jsonObject.put("isSingle",false);
        }

        //读取文件
        Map<String, List<Edge>> map = ReadFileUtils.getMap(fileName);
        String nodesStr = JSONObject.toJSONString(map);
        jsonObject.put("nodes", nodesStr);

        log.info("json is {}", jsonObject.toString());

        JSONObject aJson = HttpClientUtils.sendPost(aUrl, jsonObject);
        JSONObject bJson = HttpClientUtils.sendPost(bUrl, jsonObject);
        JSONObject cJson = HttpClientUtils.sendPost(cUrl, jsonObject);

        log.info("aJson is {}", aJson.toString());
        log.info("bJson is {}", bJson.toString());
        log.info("cJson is {}", cJson.toString());

        //初始化verMap和verEdgeMap
        try {
            lock.lock();
            List<Edge> list = new ArrayList<>();
            JSONArray array = new JSONArray();
            combineData(aJson, list);
            combineData(bJson, list);
            combineData(cJson, list);

            insertData(fileName, delay, metric, route, ratio, list);
            JSONObject returnJson = DataUtils.bestRoad(srcNodeId, dstNodeId);
            if (slaType.equals("protected")) {
                Map<String, Vertex> secondVerMap = DataUtils.verMap;
                Map<String, List<Edge>> secondVerEdgeMap = DataUtils.verEdgeMap;
                JSONObject secondJson = secondRoute(returnJson, srcNodeId, dstNodeId, secondVerMap, secondVerEdgeMap);
                secondJson.put("dst-area-id", dstAreaId);
                secondJson.put("src-area-id", srcAreaId);
                secondJson.put("type", "secondary");
                returnJson.put("type", "primary");
                array.add(secondJson);
            }
            returnJson.put("dst-area-id", dstAreaId);
            returnJson.put("src-area-id", srcAreaId);
            array.add(returnJson);
            endJson.put("request-id", requestId);
            endJson.put("paths", array);
            log.info("master service,returnJson is {}", returnJson.toString());
            return endJson;
        } catch (Exception e) {
            log.info("error is {}", e.getMessage());
            throw new Exception("Server Inter Error");
        } finally {
            DataUtils.verMap = new HashMap<>();
            DataUtils.verEdgeMap = new HashMap<>();
            serviceInfoList = new ArrayList<>();
            lock.unlock();
        }
    }

    public void insertData(String fileName, String delay, String metric, String route, String ratio, List<Edge> list) throws Exception {
        String str = ReadFileUtils.readFile(fileName);
        if (!StringUtils.isEmpty(str)) {
            CacuRoute cacuRoute = JSONObject.parseObject(str, CacuRoute.class);
            List<Edge> delayEdges = cacuRoute.getDelayEdges();
            Map<String, Map<String, List<Edge>>> newMap = delayEdges.stream().collect(Collectors.groupingBy(Edge::getSouNodeId, Collectors.groupingBy(Edge::getDstNodeId)));

            newMap.keySet().stream().forEach(key -> {
                Map<String, List<Edge>> inMap = newMap.get(key);
                inMap.keySet().forEach(inKey -> {
                    List<Edge> edgeList = inMap.get(inKey);
                    Edge edge = edgeList.stream().min(Comparator.comparingDouble((edge1) -> edge1.getWeight())).get();
                    if (StringUtils.isEmpty(delay) && StringUtils.isEmpty(ratio) && StringUtils.isEmpty(route) && StringUtils.isEmpty(metric)) {
                        log.info("no combine");
                    } else {
                        double delayWe = (StringUtils.isEmpty(delay) ? 0 : Double.valueOf(delay)) * (null == edge.getWeight() ? 0 : edge.getWeight());
                        double ratioWe = StringUtils.isEmpty(ratio) ? 0 : Double.valueOf(ratio) * (null == edge.getRatio() ? 0 : edge.getRatio());
                        double routeWe = StringUtils.isEmpty(route) ? 0 : Double.valueOf(route) * (null == edge.getRoute() ? 0 : edge.getRoute());
                        double metricWe = StringUtils.isEmpty(metric) ? 0 : Double.valueOf(metric) * (null == edge.getMetric() ? 0 : edge.getMetric());
                        double newWeight = delayWe + ratioWe + metricWe + routeWe;
                        edge.setWeight(Double.valueOf(String.format("%.2f", newWeight)));
                    }
                    ServiceInfo serviceInfo = new ServiceInfo(edge);
                    serviceInfoList.add(serviceInfo);
                });
            });
            list.addAll(delayEdges);
        }
        DataUtils.verEdgeMap = list.stream().collect(Collectors.groupingBy(Edge::getSouNodeId));
        log.info("verMap is {}", DataUtils.verMap);
        log.info("verEdgeMap is {}", DataUtils.verEdgeMap);
    }

    /**
     * 参数判断 ,后期用@Validate注解进行参数判断
     *
     * @param param
     */
    private void validateParam(String param) throws Exception {
        if (StringUtils.isEmpty(param)) {
            log.info("{} is null", param);
            throw new Exception("param is null");
        }
    }

    public void combineData(JSONObject aJson, List<Edge> list) {
        JSONObject data = aJson.getJSONObject("data");
        if (!ObjectUtils.isEmpty(data)) {
            JSONArray delay = data.getJSONArray("path");
            if (!ObjectUtils.isEmpty(delay)) {
                for (int a = 0; a < delay.size(); a++) {
                    JSONObject json1 = delay.getJSONObject(a);
                    ServiceInfo serviceInfo = JSONObject.parseObject(json1.toJSONString(), ServiceInfo.class);
                    String outSrcNodeId = json1.getString("outSrcNodeId");
                    String outDstNodeId = json1.getString("outDstNodeId");
                    Double outWeight = json1.getDouble("outWeight");
                    Edge edge = new Edge(outSrcNodeId, outDstNodeId, outWeight);
                    Vertex srcVertex = new Vertex(outSrcNodeId);
                    Vertex dstVertex = new Vertex(outDstNodeId);
                    DataUtils.verMap.put(outSrcNodeId, srcVertex);
                    DataUtils.verMap.put(outDstNodeId, dstVertex);
                    list.add(edge);
                    serviceInfoList.add(serviceInfo);
                }
            }
        }
    }

    public JSONObject secondRoute(JSONObject json, String srcNodeId, String dstNodeId, Map<String, Vertex> verMap, Map<String, List<Edge>> verEdgeMap) throws Exception {
        JSONArray path = json.getJSONArray("path");
        List<Vertex> vertices = JSONObject.parseArray(path.toJSONString(), Vertex.class);
        List<String> list = vertices.stream().map(Vertex::getParentNodeId).collect(Collectors.toList());
        verMap.entrySet().forEach(entry -> {
            entry.getValue().resotre();
        });
        list.forEach(str -> {
            if (!str.equals(srcNodeId)) {
                verMap.remove(str);
                if (verEdgeMap.containsKey(str)) {
                    verEdgeMap.remove(str);
                }
                verEdgeMap.keySet().stream().forEach(key -> {
                    List<Edge> edges = verEdgeMap.get(key);
                    List<Edge> edges1 = edges.stream().filter(edge -> edge.getDstNodeId().equals(str)).collect(Collectors.toList());
                    edges1.forEach(inEdge -> {
                        edges.remove(inEdge);
                    });
                });
            } else {
                List<Edge> edges = verEdgeMap.get(str);
                if (!ObjectUtils.isEmpty(edges)) {
                    list.forEach(str1 -> {
                        if (!str1.equals(str)) {
                            List<Edge> edges1 = edges.stream().filter(edge -> edge.getDstNodeId().equals(str1)).collect(Collectors.toList());
                            if (!ObjectUtils.isEmpty(edges1)) {
                                edges.remove(edges1.get(0));
                            }
                        }
                    });
                }
            }
        });


        DataUtils.verMap = verMap;
        DataUtils.verEdgeMap = verEdgeMap;

        JSONObject secondJson = DataUtils.bestRoad(srcNodeId, dstNodeId);
        log.info("secondRoute is {}", secondJson.toString());
        return secondJson;
    }
}