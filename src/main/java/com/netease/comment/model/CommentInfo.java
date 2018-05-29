package com.netease.comment.model;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class CommentInfo  {



    /**
     * 文章id
     */
    private String infoId;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论id
     */
    private String commentId;
    /**
     * 评论父id
     */
    private String commentFid;
    /**
     * 评论父id
     */
    private String commentMid;

    /**
     * 评论应用id
     */
    private String fromAppId;
    /**
     * 评论用户id
     */
    private String fromUserId;
    /**
     * 评论人名称
     */
    private String fromUserNName;
    /**
     * 评论人用户头像
     */
    private String fromUserface;
    /**
     * 评论时间
     */
    private Date commentTime;

    public long getCommentTime() {
        return commentTime.getTime();
    }

    /**
     * 评论点赞数
     */
    private Integer praiseCount;
    /**
     * 评论评论数
     */
    private Integer replyCount;
    /**
     * 评论图片
     */
    private List<Pic> picUrls;

    public void setPicUrls(String picUrls) {
        this.picUrls = JSONArray.parseArray(picUrls, Pic.class);
    }


    /**
     * 评论用户删除状态
     */
    private Integer deleted;


    /**
     * 子评论
     */
    private Set commentInfos;


    /**
     * 批评审核状态
     */
    private Integer status;

}
