package com.netease.comment.service;


import com.netease.comment.mapper.CommentInfoMapper;
import com.netease.comment.mapper.CommentQueryMapper;
import com.netease.comment.model.CommentInfoHottest;
import com.netease.comment.model.CommentInfoNewest;
import com.netease.comment.utils.CacheManagerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;

@Component
public class CommentCacheService {


    @Autowired
    CommentInfoMapper commentPrimaryMapper;

    @Autowired
    CommentQueryMapper commentQueryMapper;


    @Value("${comment.cache.number}")
    Integer number;


    /**
     * 拉取指定文章id 指定页码  根据创建时间倒序的数据 并缓存至redis
     * 文章id + 页码（1开始 每页数据30条 可根据配置文件配置每页缓存条数 ） + 拉取类型 （ hottest 热度 /  newest 创建时间） 中间用 '-' 分开
     * 例子            II2TICDVPTXDQDY-1-newest
     *
     * @param infoId
     * @param pageNo
     * @return
     */
    public List<CommentInfoNewest> InfoCommentNewestSave(String infoId, Integer pageNo) {
        //数据转换json存redis
        List<String> hottestCommentIds = commentQueryMapper.selectByInfoIdNewest(infoId, pageNo * number, number);
        if (hottestCommentIds.size() == 0) {
            return null;
        }
        List<CommentInfoNewest> commentInfoNewests = commentPrimaryMapper.selectByCommentIds(hottestCommentIds);
        Set<CommentInfoNewest> commentInfoNewestSet = dataOrderNewest(commentInfoNewests);
        Map commentInfoNewestMap = new HashMap();
        for (CommentInfoNewest commentInfoHottest : commentInfoNewestSet) {
            commentInfoNewestMap.put(commentInfoHottest.getCommentId(), commentInfoHottest);
        }
        CacheManagerUtils.getInstance().writeModel(infoId + "-" + (pageNo + 1) + "-newest", commentInfoNewestMap);
        return commentInfoNewests;
    }


    /**
     * 拉取指定文章id 指定页码  根据创建时间倒序的数据 并缓存至redis
     * 文章id + 页码（1开始 每页数据30条 可根据配置文件配置每页缓存条数 ） + 拉取类型 （ hottest 热度 /  newest 创建时间） 中间用 '-' 分开
     * 例子            II2TICDVPTXDQDY-1-newest
     *
     * @param infoId
     * @return
     */
    public List<CommentInfoNewest> InfoCommentNTrialSave(String infoId) {
        //数据转换json存redis
        List<String> commentIds = commentQueryMapper.selectByInfoIdNTrial(infoId);
        if (commentIds.size() == 0) {
            return null;
        }
        List<CommentInfoNewest> commentInfoNewests = commentPrimaryMapper.selectByCommentIds(commentIds);
        Map<String,List<CommentInfoNewest> > commentInfoNewestMap = new HashMap();
        for (CommentInfoNewest commentInfoNewest : commentInfoNewests) {
            List<CommentInfoNewest> commentInfoNewestList = commentInfoNewestMap.get(commentInfoNewest.getFromAppId() + "-" + commentInfoNewest.getFromUserId());
            if(commentInfoNewestList == null){
                 commentInfoNewestList =  new ArrayList<>();
                commentInfoNewestList.add(commentInfoNewest);
                commentInfoNewestMap.put(commentInfoNewest.getFromAppId() + "-" + commentInfoNewest.getFromUserId(), commentInfoNewestList);
            }else {
                commentInfoNewestList.add(commentInfoNewest);
                commentInfoNewestMap.put(commentInfoNewest.getFromAppId() + "-" + commentInfoNewest.getFromUserId(), commentInfoNewestList);
            }
        }
        CacheManagerUtils.getInstance().writeModel(infoId, commentInfoNewestMap);
        return commentInfoNewests;
    }


    /**
     * 拉取指定文章id 指定页码  根据热度倒序的数据 并缓存至redis
     * 文章id + 页码（1开始 每页数据30条 可根据配置文件配置每页缓存条数 ） + 拉取类型 （ hottest 热度 /  newest 创建时间） 中间用 '-' 分开
     * 例子            II2TICDVPTXDQDY-1-hottest
     * @param infoId
     * @param pageNo
     * @return
     */
   /* public List<CommentInfoHottest> InfoCommentHottestSave(String infoId, Integer pageNo) {
        //数据转换json存redis
        List<String> hottestInfoIds = commentInfoMapper.selectByInfoIdHottest(infoId, pageNo * number, number);
        List<CommentInfoHottest> commentInfoHottests = commentPrimaryMapper.selectByCommentIds(hottestInfoIds);
        Set<CommentInfoHottest> commentInfoHottestSet = dataOrderHottest(commentInfoHottests);
        Map commentInfoHottestMap = new HashMap();
        for (CommentInfoHottest commentInfoHottest: commentInfoHottestSet ) {
            commentInfoHottestMap.put(commentInfoHottest.getCommentId(),commentInfoHottest);
        }
        CacheManagerUtils.getInstance().writeModel(infoId + "-"+(pageNo + 1)+"-newest", commentInfoHottestMap);
        return commentInfoHottests;
    }*/

    /**
     * 数据排序
     *
     * @param commentInfoNewests
     * @return
     */
    public Set<CommentInfoNewest> dataOrderNewest(List<CommentInfoNewest> commentInfoNewests) {
        Set<CommentInfoNewest> commentInfoHottestSet = new TreeSet<>();
        Map<String, Set<CommentInfoNewest>> commentInfoHottestMap = new HashMap<>();
        for (CommentInfoNewest commentInfo : commentInfoNewests) {
            if (commentInfo.getCommentFid() == null) {    //父id没的加入一级评论
                commentInfoHottestSet.add(commentInfo);
            } else {
                if (commentInfoHottestMap.get(commentInfo.getCommentFid()) == null) {           //子评论都规制到一块 根据 父 id 分开
                    Set<CommentInfoNewest> commentInfos = new TreeSet<>();
                    commentInfos.add(commentInfo);
                    commentInfoHottestMap.put(commentInfo.getCommentFid(), commentInfos);
                } else {
                    commentInfoHottestMap.get(commentInfo.getCommentFid()).add(commentInfo);
                }
            }
        }
        if (commentInfoHottestMap.size() > 0) {
            commentInfoHottestSet = sortNewest(commentInfoHottestSet, commentInfoHottestMap);
        }
        return commentInfoHottestSet;
    }

    /**
     * 数据排序
     *
     * @param commentInfoHottests
     * @return
     */
    public Set<CommentInfoHottest> dataOrderHottest(List<CommentInfoHottest> commentInfoHottests) {
        Set<CommentInfoHottest> commentInfoHottestSet = new TreeSet<>();
        Map<String, Set<CommentInfoHottest>> commentInfoHottestMap = new HashMap<>();
        for (CommentInfoHottest commentInfo : commentInfoHottests) {
            if (commentInfo.getCommentFid() == null) {    //父id没的加入一级评论
                commentInfoHottestSet.add(commentInfo);
            } else {
                if (commentInfoHottestMap.get(commentInfo.getCommentFid()) == null) {           //子评论都规制到一块 根据 父 id 分开
                    Set<CommentInfoHottest> commentInfos = new TreeSet<>();
                    commentInfos.add(commentInfo);
                    commentInfoHottestMap.put(commentInfo.getCommentFid(), commentInfos);
                } else {
                    commentInfoHottestMap.get(commentInfo.getCommentFid()).add(commentInfo);
                }
            }
        }
        if (commentInfoHottestMap.size() > 0) {
            commentInfoHottestSet = sortHottest(commentInfoHottestSet, commentInfoHottestMap);
        }
        return commentInfoHottestSet;
    }


    private Set<CommentInfoNewest> sortNewest(Set<CommentInfoNewest> commentInfoHottestSet, Map<String, Set<CommentInfoNewest>> commentInfoHottestMap) {
        for (CommentInfoNewest commentInfo : commentInfoHottestSet) {
            Set<CommentInfoNewest> commentInfos = commentInfoHottestMap.get(commentInfo.getCommentId());
            commentInfo.setCommentInfos(commentInfos); //添加二级之后的评论
            if (commentInfos != null) {
                commentInfos = this.sortNewest(commentInfos, commentInfoHottestMap);
            }
        }
        return commentInfoHottestSet;
    }

    private Set<CommentInfoHottest> sortHottest(Set<CommentInfoHottest> commentInfoHottestSet, Map<String, Set<CommentInfoHottest>> commentInfoHottestMap) {
        for (CommentInfoHottest commentInfo : commentInfoHottestSet) {
            Set<CommentInfoHottest> commentInfos = commentInfoHottestMap.get(commentInfo.getCommentId());
            commentInfo.setCommentInfos(commentInfos); //添加二级之后的评论
            if (commentInfos != null) {
                commentInfos = this.sortHottest(commentInfos, commentInfoHottestMap);
            }
        }
        return commentInfoHottestSet;
    }
}
