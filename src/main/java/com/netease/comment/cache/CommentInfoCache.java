package com.netease.comment.cache;

import com.netease.comment.mapper.CommentQueryMapper;
import com.netease.comment.mapper.InfoPraiseStatisticsMapper;
import com.netease.comment.utils.CacheManagerUtils;
import com.netease.comment.service.CommentCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class CommentInfoCache {

    @Autowired
    InfoPraiseStatisticsMapper infoPraiseStatisticsMapper;

    @Autowired
    CommentQueryMapper commentQueryMapper;

    @Autowired
    CommentCacheService commentCacheService;


    /**
     * 存入redis 规则
     * <p>(评论数据)
     * 文章id + 页码（1开始 每页数据30条 ） + 拉取类型 （ hottest 热度 /  newest 创建时间） 中间用 '-' 分开
     * 例子            II2TICDVPTXDQDY-1-newest
     * <p>
     * <p>（文章对应的总评论数）
     * 文章id-count
     * 例子   II2TICDVPTXDQDY-count
     * <p>
     */
    @PostConstruct
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 10 * 60 * 1000) //10分钟更新一次
    public void CommentInfoRedis() {
        List<String> infoIds = commentQueryMapper.selectAllInfoId();
        for (String infoId : infoIds) {
            //保存文章总的评论数
            Integer count = commentQueryMapper.selectCommentCount(infoId);
            CacheManagerUtils.getInstance().writeMap(infoId + "-count", count.toString());
            //暂时不提供最热
          /*  if(count >= 50 ){
                //保存热度文章
                commentCacheService.InfoCommentHottestSave(infoId, 0);
            }*/
            //保存最新文章
            commentCacheService.InfoCommentNewestSave(infoId, 0);
            commentCacheService.InfoCommentNTrialSave(infoId);
        }
    }


}
