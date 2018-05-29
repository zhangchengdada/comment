package com.netease.comment.model;


import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class CommentInfoHottest extends CommentInfo implements Comparable<CommentInfoHottest> {

    public int compareTo(CommentInfoHottest o) {
        Integer num = this.replyCount + this.praiseCount - o.replyCount - o.praiseCount;    //热度
        if (num == 0) {
            num = new Long(this.commentTime.getTime() - o.commentTime.getTime()).intValue();            //时间
        }
        return num == 0 ? this.infoId.compareTo(o.infoId) : num;
    }


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
