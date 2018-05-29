package com.netease.comment.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import com.netease.comment.config.DunContentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 易盾工具类
 */
@Slf4j
@Component
public class DunContentUtils {




    /**
     * 传入评论信息和信息id 进行校验
     *
     * @param commentId
     * @param content
     * @return 1 成功 -1 失败 -2 待人审核 -3 代码异常
     * @throws Exception
     */
    public  Integer checkoutContent(String commentId, String content) throws Exception {
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", DunContentConfig.SECRETID);
        params.put("businessId", DunContentConfig.BUSINESSID);
        params.put("version", "v3.1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));

        // 2.设置私有参数
        params.put("dataId", commentId);
        params.put("content", content);

        // 3.生成签名信息
        String signature = Utils.genSignature(DunContentConfig.SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClientUtil.doPost(DunContentConfig.API_URL, params);

        // 5.解析接口返回值
        JSONObject jObject = JSONObject.parseObject(response);
        int code = Integer.valueOf(jObject.get("code").toString());
        String msg = jObject.get("msg").toString();
        if (code == 200) {
            JSONObject resultObject = JSONObject.parseObject(jObject.get("result").toString());
            int action = Integer.valueOf(resultObject.get("action").toString());
            /*String taskId = resultObject.get("taskId").toString();
            JSONArray labelArray = JSONArray.parseArray(resultObject.get("labels").toString());
            for (JsonElement labelElement : labelArray) {
                JsonObject lObject = labelElement.getAsJsonObject();
                int label = lObject.get("label").getAsInt();
                int level = lObject.get("level").getAsInt();
                JsonObject detailsObject=lObject.getAsJsonObject("details");
                JsonArray hintArray=detailsObject.getAsJsonArray("hint");
            }*/
            if (action == 0) {
                return 1;
                //  System.out.println(String.format("taskId=%s，文本机器检测结果：通过", taskId));
            } else if (action == 1) {
                return -2;
                //  System.out.println(String.format("taskId=%s，文本机器检测结果：嫌疑，需人工复审，分类信息如下：%s", taskId, labelArray.toString()));
            } else if (action == 2) {
                return -1;
                // System.out.println(String.format("taskId=%s，文本机器检测结果：不通过，分类信息如下：%s", taskId, labelArray.toString()));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
        return -3;
    }


}
