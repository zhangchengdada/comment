package com.netease.comment.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.comment.dto.PictureData;
import com.netease.comment.model.Pic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class DunPicUtils {


    /**
     * 产品密钥ID，产品标识
     */
    @Value("${yidun.pic.secretId}")
    private static String SECRETID;

    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    @Value("${yidun.pic.secretkey}")
    private static String SECRETKEY;

    /**
     * 业务ID，云安全（易盾）根据产品业务特点分配
     */
    @Value("${yidun.pic.businessId}")
    private static String BUSINESSID;

    /**
     * 云安全（易盾）反垃圾云服务文本在线检测接口地址
     */
    @Value("${yidun.pic.apiUrl}")
    private static String API_URL;


    /**
     * 传入图片信息 返回 -1 表示不通过  -2 表示需要人审 -3接口异常 返回1 正常
     *
     * @param
     * @throws Exception
     */
    public Integer checkoutPic(List<PictureData> pics ) throws Exception {
        if (pics == null || pics.size() == 0) {
            return 1;
        }
        Map<String, String> params = new HashMap<>();
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("version", "v3.1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // 2.设置私有参数
        JSONArray jsonArray = new JSONArray();
        // 传图片url进行检测，name结构产品自行设计，用于唯一定位该图片数据
        for (PictureData pic : pics) {
            JSONObject image = new JSONObject();
            image.put("name", pic.getUrl());
            image.put("type", 1);
            image.put("data", pic.getUrl());
            jsonArray.add(image);
        }
        params.put("images", jsonArray.toString());
        // 3.生成签名信息
        String signature = Utils.genSignature(SECRETKEY, params);
        params.put("signature", signature);
        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClientUtil.doPost(API_URL, params);
        // 5.解析接口返回值
        JSONObject resultObject = JSONObject.parseObject(response);
        int code = Integer.valueOf(resultObject.get("code").toString());
        String msg = resultObject.get("msg").toString();
        if (code == 200) {
            JSONArray resultArray = JSONArray.parseArray(resultObject.get("result").toString());
            for (int i = 0; i < resultArray.size(); i++) {
                JSONObject jObject = resultArray.getJSONObject(i);
                String name = jObject.get("name").toString();
                String taskId = jObject.get("taskId").toString();
                JSONArray labelArray = JSONArray.parseArray(jObject.get("labels").toString());
                //  System.out.println(String.format("taskId=%s，name=%s，labels：", taskId, name));
                int maxLevel = -1;
                // 产品需根据自身需求，自行解析处理，本示例只是简单判断分类级别
                for (int x = 0; x < labelArray.size(); x++) {
                    JSONObject lObject = labelArray.getJSONObject(i);
                    int label = Integer.parseInt(lObject.get("label").toString());
                    int level = Integer.parseInt(lObject.get("level").toString());
                    double rate = Double.valueOf(lObject.get("rate").toString());
                    System.out.println(String.format("label:%s, level=%s, rate=%s", label, level, rate));
                    maxLevel = level > maxLevel ? level : maxLevel;
                }
                switch (maxLevel) {
                    case 0:
                        //System.out.println("#图片机器检测结果：最高等级为\"正常\"\n");
                        break;
                    case 1:
                        return -2;
                    //  System.out.println("#图片机器检测结果：最高等级为\"嫌疑\"\n");

                    case 2:
                        return -1;
                    //    System.out.println("#图片机器检测结果：最高等级为\"确定\"\n");
                    default:
                        break;
                }
            }
            return 1;
        } else {
            log.error(String.format("ERROR: code=%s, msg=%s", code, msg));
        }
        return -3;
    }


}
