package com.netease.comment.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by zhaofeng01 on 2018/2/26.
 */
@Configuration
@PropertySource({"classpath:application.yml"})
@Setter
@Getter
public class RabbitMqConfig {

    public static String EXCHANGE;

    public static String ROUT_KEY;

    public static String QUEUE_CONSUMER;

    @Value("${rabbitmq.producer.exchange}")
    public void setEXCHANGE(String exchange) {
        EXCHANGE = exchange;
    }

    @Value("${rabbitmq.producer.routKey}")
    public void setRoutKey(String routKey) {
        ROUT_KEY = routKey;
    }

    @Value("${rabbitmq.consumer.queue}")
    public void setConsumerQueue(String queue) {
        QUEUE_CONSUMER = queue;
    }

}
