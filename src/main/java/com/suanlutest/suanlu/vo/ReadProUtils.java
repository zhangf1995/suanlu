package com.suanlutest.suanlu.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @BelongsProject: calculationroad
 * @Author: zf
 * @CreateTime: 2019-11-19 14:34
 * @Description: 文件读取及处理
 */
@Slf4j
public class ReadProUtils {

    public static String readFile(String fileName) {
        FileReader fileReader = null;
        InputStreamReader isr = null;
        try {
            File file = new File(fileName);
            fileReader = new FileReader(file);
            isr = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = isr.read()) != -1) {
                StringBuffer append = sb.append((char)ch);
            }
            fileReader.close();
            isr.close();
            String jsonStr = sb.toString();
            log.info("jsonStr is {}", jsonStr);
            return jsonStr;
        } catch (IOException e) {

        }finally {
            try {
                fileReader.close();
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String jsonStr = readFile("F:\\test\\AREA-A.json");
        CacuRoute cacuRoute = JSONObject.parseObject(jsonStr, CacuRoute.class);
        log.info("edges is {}",cacuRoute.getDelayEdges());
    }
}