package com.suanlutest.suanlu.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @BelongsProject: calculationroad
 * @Author: zf
 * @CreateTime: 2019-11-19 15:16
 * @Description: 算路json
 */
@Data
public class CacuRoute {

    //时延
    @JSONField(name = "delay")
    private List<Edge> delayEdges;
}