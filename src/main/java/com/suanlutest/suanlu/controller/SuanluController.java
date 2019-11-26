package com.suanlutest.suanlu.controller;

import com.alibaba.fastjson.JSONObject;
import com.suanlutest.suanlu.constant.SuanluConstant;
import com.suanlutest.suanlu.en.StateCode;
import com.suanlutest.suanlu.resp.Result;
import com.suanlutest.suanlu.service.SuanluService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: suanlu
 * @Author: zf
 * @CreateTime: 2019-11-20 10:00
 * @Description:
 */
@Slf4j
@RequestMapping("/suanlu")
@RestController
public class SuanluController {

    @Autowired
    private SuanluService suanluService;

    @Value("${file.delay.location}")
    public String delayFileName;
    @Value("${file.ac.location}")
    public String acFileName;
    @Value("${file.node.location}")
    public String nodeFileName;

    @Value("${route.delay}")
    public String delayRoute;
    @Value("${route.ac}")
    public String acRoute;
    @Value("${route.node}")
    public String nodeRoute;
    @Value("${area.srcAreaId}")
    public String srcAreaId;
    @Value("${area.dstAreaId}")
    public String dstAreaId;

    @RequestMapping("/suanluCollection")
    public Result test(@RequestBody JSONObject json){
        log.info("come in test,json is {}",json.toString());
        try{
            String fileName = null;
            String type =null;
            String routineType = json.getString("routing-policy");
            if(delayRoute.equals(routineType)){
                fileName = delayFileName;
                type = SuanluConstant.DELYA;
            }else if(acRoute.equals(routineType)){
                fileName= acFileName;
                type = SuanluConstant.AC;
            }else if(nodeRoute.equals(nodeFileName)){
                fileName = nodeFileName;
                type = SuanluConstant.NODE;
            }else{
                throw new Exception("routing-policy type error");
            }
            JSONObject returnJson = suanluService.combineData(json,fileName,type);
            return Result.me().response(StateCode.SUCCESS.getCode(),StateCode.SUCCESS.getMsg(),returnJson);
        }catch (Exception e){
            return Result.me().response(StateCode.FAIL.getCode(),e.getMessage());
        }
    }

}