package com.suanlutest.suanlu.service;

import com.alibaba.fastjson.JSONObject;

public interface SuanluService {
    JSONObject combineData(JSONObject json, String fileName,String type) throws Exception;
}
