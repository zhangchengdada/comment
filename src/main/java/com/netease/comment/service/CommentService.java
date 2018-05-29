package com.netease.comment.service;

import com.alibaba.fastjson.JSONArray;
import com.netease.comment.dto.BaseResponse;
import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.dto.CommentInfoQueryData;
import com.netease.comment.mapper.CommentInfoMapper;
import com.netease.comment.mapper.CommentQueryMapper;
import com.netease.comment.mapper.InfoPraiseStatisticsMapper;
import com.netease.comment.model.CommentInfoNewest;
import com.netease.comment.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CommentService {


    @Autowired
    CommentInfoMapper commentInfoMapper;

    @Autowired
    CommentQueryMapper commentQueryMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    RabbitMQService rabbitmqService;

    @Autowired
    InfoPraiseStatisticsMapper infoPraiseStatisticsMapper;

    CacheManagerUtils cacheManager = CacheManagerUtils.getInstance();


    public BaseResponse praiseCountQuery(String infoIds, String requestId) {
        log.info("CommentService praiseCountQuery : requestId={} infoIds={}", requestId, infoIds);
        String[] infoIdS = infoIds.split(",");
        Map data = new HashMap<>();
        for (String infoId: infoIdS ) {
            String count = cacheManager.readMap(infoId+"-count");
            if(StringUtils.isBlank(count)){
                data.put(infoId,0);
            }else {
                data.put(infoId,count);
            }
        }
        return BaseResponse.createSuccess(data,requestId);
    }


    /**
     * 评论信息拉取
     *
     * @param commentInfoQueryData
     * @param requestId
     * @return
     */
    public BaseResponse selectCommentInfo(CommentInfoQueryData commentInfoQueryData, String requestId) {
        log.info("CommentService selectCommentInfo : requestId={} commentInfoQueryData={}", requestId, commentInfoQueryData);
        //取出参数
        Integer pageNo = commentInfoQueryData.getPageNo();
        Integer pageSize = commentInfoQueryData.getPageSize();
        //创建返回的分页对象
        PageUtil pageUtil = new PageUtil<>();
        pageUtil.setPageNo(pageNo);
        pageUtil.setPageSize(pageSize);
        //从redis 中获取数据和总数量
        pageUtil = redisService.getCommentData(pageUtil, commentInfoQueryData);
        return BaseResponse.createSuccess(pageUtil, requestId);
    }

    /**
     * 评论信息提交
     *
     * @param commentInfoAddData
     * @param requestId
     * @return
     */
    public BaseResponse addCommentInfo(CommentInfoAddData commentInfoAddData, String requestId) {

        log.info("CommentService addCommentInfo : requestId={} commentInfoAddData={}", requestId, commentInfoAddData);
        commentInfoAddData.setCommentId(requestId);
        commentInfoAddData.setCommentTime(new Date());
        //数据存入redis
        redisService.commentInsertRedis(commentInfoAddData);
        //写入mq
        rabbitmqService.commentWriteIn(commentInfoAddData);
        return BaseResponse.createSuccess(requestId);
    }

    /**
     * 数据存入数据库
     *
     * @param commentInfoAddData
     * @return
     */
    public CommentInfoAddData commentInfoAddDatabase(CommentInfoAddData commentInfoAddData) {

        commentInfoAddData.setCommentTime(new Date());
        Integer i = commentInfoMapper.insert(commentInfoAddData, JSONArray.toJSONString(commentInfoAddData.getPics()));
        commentInfoAddData = commentInsert(commentInfoAddData, i);
        // infoPraiseStatisticsMapper.insert(commentInfoAddData);
        commentQueryMapper.insert(commentInfoAddData);

        return commentInfoAddData;
    }

    public void updateCommentFReplyCount(String commentId, Integer i) {
        commentInfoMapper.updateReplyCount(commentId, i);
        commentQueryMapper.updateReplyCount(commentId, i);
    }


    /**
     * id 重复重新生成插入
     *
     * @param commentInfoAddData
     * @param i
     */
    private CommentInfoAddData commentInsert(CommentInfoAddData commentInfoAddData, Integer i) {
        if (i == 0) {
            commentInfoAddData.setCommentId(Utils.getUUID());
            i = commentInfoMapper.insert(commentInfoAddData, JSONArray.toJSONString(commentInfoAddData.getPics()));
            commentInfoAddData = commentInsert(commentInfoAddData, i);
        }
        return commentInfoAddData;
    }


}
