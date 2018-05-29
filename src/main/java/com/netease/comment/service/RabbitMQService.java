package com.netease.comment.service;

import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.utils.RabbitMqSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQService {


    /**
     * mq 写入
     * @param commentInfoAddData
     */
    public void commentWriteIn(CommentInfoAddData commentInfoAddData){
        RabbitMqSenderUtil.sendMsg(commentInfoAddData);
    }
}
