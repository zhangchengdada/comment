package com.netease.comment.utils;


import com.alibaba.fastjson.JSONObject;
import com.netease.comment.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhaofeng01 on 2018/2/26.
 */
@Component
@Slf4j
public class RabbitMqSenderUtil {


    private static AmqpTemplate rabbitTemplate;

    public static void sendMsg(Object object) {
        log.info("send msg to  exchange {}, routkey {}, rabbit {},",
                RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUT_KEY,  JSONObject.toJSONString(object));
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUT_KEY,
                JSONObject.toJSONString(object));
    }

    @Autowired
    public void setRabbitTemplate(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}
