package com.netease.comment.model;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class CommentInfoNewest extends CommentInfo implements Comparable<CommentInfoNewest> {

    public int compareTo(CommentInfoNewest o) {
        Long num = this.commentTime.getTime() - o.getCommentTime();                //时间
        return num == 0 ? this.commentId.compareTo(o.getCommentId()) : num.intValue();
    }

    public CommentInfoNewest() {
    }

    public CommentInfoNewest(CommentInfo commentInfo) {
        this.infoId = commentInfo.getInfoId();
        this.content = commentInfo.getContent();
        this.commentMid = commentInfo.getCommentMid();
        this.commentId = commentInfo.getCommentId();
        this.commentFid = commentInfo.getCommentFid();
        this.fromAppId = commentInfo.getFromAppId();
        this.fromUserId = commentInfo.getFromUserId();
        this.fromUserNName = commentInfo.getFromUserNName();
        this.fromUserface = commentInfo.getFromUserface();
        this.commentTime = new Date(commentInfo.getCommentTime());
        this.praiseCount = commentInfo.getPraiseCount();
        this.replyCount = commentInfo.getReplyCount();
        this.picUrls = commentInfo.getPicUrls();
        this.deleted = commentInfo.getDeleted();
        this.status = commentInfo.getStatus();

    }


    /**
     * 文章id
     */
    private String infoId;


    private String content;

    private String commentMid;
    /**
     * 评论id
     */
    private String commentId;
    /**
     * 评论父id
     */
    private String commentFid;

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
     * 子评论
     */
    private Set commentInfos;

    /**
     * 评论用户删除状态
     */
    Integer deleted;
    /**
     * 批评审核状态
     */
    Integer status;


}
