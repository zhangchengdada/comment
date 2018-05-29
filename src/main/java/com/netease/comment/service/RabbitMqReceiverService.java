package com.netease.comment.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.dto.PictureData;
import com.netease.comment.mapper.CommentInfoMapper;
import com.netease.comment.mapper.CommentQueryMapper;
import com.netease.comment.model.Pic;
import com.netease.comment.utils.DunContentUtils;
import com.netease.comment.utils.DunPicUtils;
import com.netease.comment.utils.NosPicUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * mq 消费
 */
@Slf4j
@Component
public class RabbitMqReceiverService {


    @Autowired
    DunPicUtils dunPicUtils;
    @Autowired
    DunContentUtils dunContentUtils;
    @Autowired
    CommentInfoMapper commentInfoMapper;
    @Autowired
    NosPicUtils nosPicUtils;
    @Autowired
    CommentQueryMapper commentQueryMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    CommentService commentService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.consumer.queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.consumer.exchange}"),
            key = "${rabbitmq.consumer.routkey}"
    ))
    public void receiveMsg(String content) {
        //数据插入表
        log.info("receive msg {}", content);
        CommentInfoAddData response = JSONObject.parseObject(content, CommentInfoAddData.class);
        //数据写入数据库
        response = commentService.commentInfoAddDatabase(response);
        List<PictureData> picList = response.getPics();
        Integer picCheckStatus = 0;
        String picErrorReason = null;
        Integer commentCheckStatus = 0;
        String commentErrorReason = null;
        try {
            picCheckStatus = dunPicUtils.checkoutPic(picList);
        } catch (Exception e) {
            log.error("评论图片信息审核异常,commentId = {}", response.getCommentId(), e);
        }
        try {
            commentCheckStatus = dunContentUtils.checkoutContent(response.getCommentId(), response.getContent());
        } catch (Exception e) {
            log.error("评论内容信息审核异常,commentId = {}", response.getCommentId(), e);
        }
        if (picCheckStatus > 0 && commentCheckStatus > 0) {

            String userface = nosPicUtils.processPictureUrl(response.getUserface());
            if (picList != null && picList.size() >= 1) {
                for (int i = 0; i < picList.size(); i++) {
                    PictureData pic = picList.get(i);
                    pic = nosPicUtils.processPictureSingle(pic.getUrl());
                }
            }
            commentInfoMapper.updatePicAndStatus(response.getCommentId(), JSONArray.toJSONString(response.getPics()),userface, 1);
            commentQueryMapper.updatePicAndStatus(response.getCommentId(), JSONArray.toJSONString(response.getPics()), 1);
            commentService.updateCommentFReplyCount(response.getCommentId(), 1);
            //将redis 上数据清理
            redisService.deletCache(response);
        } else {
            commentInfoMapper.updatePicAndStatus(response.getCommentId(), null, null,-1);
            commentQueryMapper.updatePicAndStatus(response.getCommentId(), null, -1);
        }


    }
}
