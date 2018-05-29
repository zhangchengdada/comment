package com.netease.comment.controller;

import com.netease.comment.dto.BaseResponse;
import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.dto.CommentInfoQueryData;
import com.netease.comment.enums.ResponseCodeEnum;
import com.netease.comment.service.CommentService;
import com.netease.comment.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 评论信息 Controller
 */
@Slf4j
@Controller
@RequestMapping(value = "/comment")
public class CommentController {


    @Autowired
    CommentService commentService;

    /**
     * 评论信息拉取
     * @param commentInfoQueryData
     * @return
     */
    @ResponseBody
    @PostMapping("/info/query")
    public BaseResponse infoQuery(CommentInfoQueryData commentInfoQueryData) {
        String requestId = Utils.getUUID();
        log.info("CommentController infoQuery : requestId={} commentInfoQueryData={}",requestId,commentInfoQueryData);
        return commentService.selectCommentInfo(commentInfoQueryData,requestId);
    }




    /**
     * 评论信息添加
     * @param commentInfoAddData
     * @return
     */
    @ResponseBody
    @PostMapping("/info/add")
    public BaseResponse infoAdd(CommentInfoAddData commentInfoAddData) {
        String requestId = Utils.getUUID();
        log.info("CommentController infoAdd : requestId={} commentInfoAddData={}",requestId,commentInfoAddData.toString());
       Integer length =  commentInfoAddData.getContent().length();
        if(length > 1000){
            return  BaseResponse.createError(ResponseCodeEnum.PARAMETER_ERROR,requestId);
        }


        return commentService.addCommentInfo(commentInfoAddData,requestId);
    }


    /**
     * 文章id 获取文章评论数
     * @param infoIds
     * @return
     */
    @PostMapping("/praise/count/query")
    public BaseResponse praiseCountQuery(String infoIds) {
        String requestId = Utils.getUUID();
        log.info("CommentController praiseCountQuery : requestId={} infoIds={}",requestId,infoIds);
        return commentService.praiseCountQuery(infoIds,requestId);
    }





}
