package com.suanlutest.suanlu.utils;

import com.alibaba.fastjson.JSONObject;
import com.suanlutest.suanlu.vo.CacuRoute;
import com.suanlutest.suanlu.vo.Edge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @BelongsProject: suanlu
 * @Author: zf
 * @CreateTime: 2019-11-20 11:28
 * @Description:
 */
@Slf4j
public class ReadFileUtils {

    public static String readFile(String fileName) throws Exception{
        FileReader fileReader = null;
        InputStreamReader isr = null;
        try{
            File file = new File(fileName);
            fileReader = new FileReader(file);
            isr = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = isr.read()) != -1){
                sb.append((char) ch);
            }
            log.info("test is {}",sb.toString());
            return sb.toString();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }finally {
            try{
                fileReader.close();;
                isr.close();
            }catch (IOException e1){
                throw new IOException(e1.getMessage());
            }
        }
    }

    public static Map<String,List<Edge>> getMap(String fileName) throws Exception{
        log.info("start read file");
        String str = readFile(fileName);
        log.info("ene read file ,fileStr is {}",str);
        if(!StringUtils.isEmpty(str)){
            CacuRoute cacuRoute = JSONObject.parseObject(str, CacuRoute.class);
            List<Edge> delayEdges = cacuRoute.getDelayEdges();
            Map<String, List<Edge>> map = delayEdges.stream().collect(Collectors.groupingBy(Edge::getSrcAreaId));
            log.info("only test");
            return map;
        }
        return null;
    }

    public static void main(String[] args) {
     /*   String str = readFile();
        if(!StringUtils.isEmpty(str)){
            CacuRoute cacuRoute = JSONObject.parseObject(str, CacuRoute.class);
            List<Edge> delayEdges = cacuRoute.getDelayEdges();
            Map<String, List<Edge>> map = delayEdges.stream().collect(Collectors.groupingBy(Edge::getSrcAreaId));

            log.info("");
        }*/
    }
}