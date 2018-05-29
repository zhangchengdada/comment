package com.netease.comment.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:application.yml"})
@Setter
@Getter
public class DunContentConfig {

    /**
     * 产品密钥ID，产品标识
     */
    public static String SECRETID;

    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    public static String SECRETKEY;

    /**
     * 业务ID，云安全（易盾）根据产品业务特点分配
     */
    public static String BUSINESSID;

    /**
     * 云安全（易盾）反垃圾云服务文本在线检测接口地址
     */
    public static String API_URL;

    @Value("${yidun.content.secret-id}")
    public void setSECRETID(String secretId) {
        SECRETID = secretId;
    }

    @Value("${yidun.content.secretkey}")
    public void setSECRETKEY(String secretkey) {
        SECRETKEY = secretkey;
    }

    @Value("${yidun.content.business-id}")
    public void setBUSINESSID(String businessId) {
        BUSINESSID = businessId;
    }

    @Value("${yidun.content.api-url}")
    public void setApiUrl(String apiUrl) {
        API_URL = apiUrl;
    }

}
