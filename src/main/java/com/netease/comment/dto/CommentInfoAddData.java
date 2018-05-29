package com.netease.comment.dto;


import com.alibaba.fastjson.JSONArray;
import com.netease.comment.model.CommentInfo;
import com.netease.comment.model.CommentInfoHottest;
import com.netease.comment.model.CommentInfoNewest;
import com.netease.comment.model.Pic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
public class CommentInfoAddData {

    Integer id;
  /*  *//**
     * 当前评论页码（对文章评论不需要该参数）
     *//*
    private Integer pageNo;

    *//**
     * 当前评论每页显示数（对文章评论不需要该参数）
     *//*
    private Integer pageSize;
*/
    /**
     * 评论应用id
     */
    @NotNull(message = "评论应用id不能为空")
    private String appId;
    /**
     * 评论用户id
     */
    private String userId;
    /**
     * 评论用户名称
     */
    private String userName;
    /**
     * 评论用户昵称
     */
    private String userNName = "火星游客";
    /**
     * 评论用户头像
     */
    private String userface;

    /**
     * 文章id
     */
    private String infoId;
    /**
     * 评论id
     */
    private String commentId;
    /**
     * 评论父id
     */
    private String commentFid;
    /**
     * 评论归宿主评论id
     */
    private String commentMid;

    public String getCommentMid() {
        return commentMid == null ? commentId : commentMid;
    }

    /**
     * 评论信息
     */
    private String content;
    /**
     * 评论时间
     */
    private Date commentTime;
    /**
     * 评论图集
     */
    private String picUrls;

    private List<PictureData> pics;

    public void setPicUrls(String picUrls) {
        this.picUrls = picUrls;
        this.pics = new ArrayList<>();
        String[] urls = picUrls.split(",");
        for (String url : urls) {
            PictureData pic = new PictureData();
            pic.setUrl(url);
            pics.add(pic);
        }
    }

    /**
     * 评论用户ip
     */
    private String fromIp;


    public CommentInfo getCommentInfo() {
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setCommentId(commentId);
        commentInfo.setInfoId(infoId);
        commentInfo.setCommentFid(commentFid);
        commentInfo.setCommentMid(commentMid);
        commentInfo.setCommentTime(commentTime);
        commentInfo.setFromAppId(appId);
        commentInfo.setFromUserId(userId);
        commentInfo.setFromUserNName(userNName);
        commentInfo.setFromUserface(userface);
        commentInfo.setContent(content);
        commentInfo.setPicUrls(JSONArray.toJSONString(pics));


        return commentInfo;
    }


}
