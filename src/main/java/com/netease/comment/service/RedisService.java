package com.netease.comment.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.comment.dto.CommentInfoAddData;
import com.netease.comment.dto.CommentInfoQueryData;
import com.netease.comment.enums.PullTypeEnum;
import com.netease.comment.mapper.CommentQueryMapper;
import com.netease.comment.model.CommentInfo;
import com.netease.comment.model.CommentInfoHottest;
import com.netease.comment.model.CommentInfoNewest;
import com.netease.comment.utils.CacheManagerUtils;
import com.netease.comment.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RedisService {

    @Value("${comment.cache.number}")
    Integer number;

    @Autowired
    CommentCacheService commentCacheService;

    @Autowired
    CommentQueryMapper commentQueryMapper;


    CacheManagerUtils cacheManager = CacheManagerUtils.getInstance();


    /**
     * 删除用户待审数据，并更新文章审核通过的数据
     *
     * @param commentInfoAddData
     */
    public void deletCache(CommentInfoAddData commentInfoAddData) {

        String infoData = cacheManager.readMap(commentInfoAddData.getInfoId());
        Map infoJsonData = JSONArray.parseObject(infoData);
        Object object = infoJsonData.get(commentInfoAddData.getAppId() + "-" + commentInfoAddData.getUserId());
        if (object == null) {
            return;
        }
        String userCommentData = infoJsonData.get(commentInfoAddData.getAppId() + "-" + commentInfoAddData.getUserId()).toString();
        List<CommentInfo> commentInfos = JSONArray.parseArray(userCommentData, CommentInfo.class);
        for (int i = 0; i < commentInfos.size(); i++) {
            if (commentInfos.get(i).getCommentId().equals(commentInfoAddData.getCommentId())) {
                commentInfos.remove(i);
            }
        }
        infoJsonData.put(commentInfoAddData.getAppId() + "-" + commentInfoAddData.getUserId(), commentInfos);
        cacheManager.writeModel(commentInfoAddData.getInfoId(), infoJsonData);
        commentCacheService.InfoCommentNewestSave(commentInfoAddData.getInfoId(), 0);
        //保存文章总的评论数
        Integer count = commentQueryMapper.selectCommentCount(commentInfoAddData.getInfoId());
        CacheManagerUtils.getInstance().writeMap(commentInfoAddData.getInfoId() + "-count", count.toString());

    }


    /**
     * 插入数据
     *
     * @param commentInfoAddData
     */
    public void commentInsertRedis(CommentInfoAddData commentInfoAddData) {
        //获取缓存对象

        String infoId = commentInfoAddData.getInfoId();
        String userId = commentInfoAddData.getUserId();
        String infoData = cacheManager.readMap(infoId);
        String appId = commentInfoAddData.getAppId();
        if (infoData == null) {
            //文章评论没有
            Map commentData = new HashMap();
            List list = new ArrayList<>();
            list.add(commentInfoAddData.getCommentInfo());
            commentData.put(commentInfoAddData.getAppId() + "-" + commentInfoAddData.getUserId(), list);
            cacheManager.writeModel(infoId, commentData);
        } else {
            Map infoJsonData = JSONArray.parseObject(infoData);
            Object infoJsonObject = infoJsonData.get(appId + "-" + userId);
            String userCommentData = null;
            if (infoJsonObject != null) {
                userCommentData = infoJsonObject.toString();
            }
            if (userCommentData == null) {
                //文章评论里面的用户评论没有
                List list = new ArrayList<>();
                list.add(commentInfoAddData.getCommentInfo());
                infoJsonData.put(commentInfoAddData.getAppId() + "-" + commentInfoAddData.getUserId(), list);
                cacheManager.writeModel(infoId, infoJsonData);
            } else {
                List<CommentInfo> commentInfos = JSONArray.parseArray(userCommentData, CommentInfo.class);
                commentInfos.add(commentInfoAddData.getCommentInfo());
                infoJsonData.put(appId + "-" + userId, commentInfos);
                cacheManager.writeModel(infoId, infoJsonData);
            }
        }
    }

    /**
     * 查询数据
     *
     * @param commentInfoQueryData
     * @return
     */
    public PageUtil getCommentData(PageUtil pageUtil, CommentInfoQueryData commentInfoQueryData) {
        String infoId = commentInfoQueryData.getInfoId();
        String userId = commentInfoQueryData.getUserId();
        String pullType;
        if (commentInfoQueryData.getPullTypeEnum() == 0) {
            pullType = PullTypeEnum.HOTTEST.getType();
        } else {
            pullType = PullTypeEnum.NEWEST.getType();
        }
        String appId = commentInfoQueryData.getAppId();
        Integer pageNo = commentInfoQueryData.getPageNo();
        Integer pageSize = commentInfoQueryData.getPageSize();
        //获取缓存对象
        CacheManagerUtils cacheManager = CacheManagerUtils.getInstance();
        //获得该文章评论通过的总数
        Integer count = cacheManager.readMap(infoId + "-count") == null ? 0 : Integer.valueOf(cacheManager.readMap(infoId + "-count"));
        Set data = new TreeSet();
        String infoData = cacheManager.readMap(infoId);

        //获取用户对该文章的评论
        List<CommentInfoNewest> commentInfos = null;
        if (infoData != null) {
            Map infoJsonData = JSONArray.parseObject(infoData);
            Object infouserData = infoJsonData.get(appId + "-" + userId);
            String userCommentData = infouserData == null ? null : infouserData.toString();
            if (userCommentData != null) {
                commentInfos = JSONArray.parseArray(userCommentData, CommentInfoNewest.class);
            }
        }

        //计算redis拉取页码
        Integer x = new Double(Math.ceil(pageNo * pageSize / number.doubleValue())).intValue();
        //获取文章缓存评论数据
        List<CommentInfoNewest> listNewest = null;
        if (pullType.equals(PullTypeEnum.HOTTEST.getType())) {
            //暂时不提供最热拉取
            pageUtil.setData(data);
            pageUtil.setPageCount(count);
            return pageUtil;
        } else if (pullType.equals(PullTypeEnum.NEWEST.getType())) {
            listNewest = getRedisData(cacheManager, infoId, x, pullType);
            Integer i = (pageNo - 1) * pageSize - (x - 1) * number;
            if (listNewest.size() < (i + pageSize)) {
                List<CommentInfoNewest> list2 = getRedisData(cacheManager, infoId, x + 1, pullType);
                if (list2 != null) {
                    listNewest.addAll(list2);
                }
            }
            if (listNewest.size() > (i + pageSize)) {
                listNewest.subList(i, pageSize);
            }
        }

        if (commentInfos != null) {
            if (listNewest == null) {
                data.addAll(commentInfos);
            } else {
                for (CommentInfo commentInfo : commentInfos) {
                    if (commentInfo.getCommentFid() == null) {
                        if (x == 1) {
                            //根据时间排序
                            data.addAll(commentInfos);
                        }
                    } else {
                        //插入到父评论下面
                        listNewest = insertFComment(listNewest, commentInfo);
                    }
                }
                data.addAll(listNewest);
            }
        }
        if (listNewest != null) {
            data.addAll(listNewest);
        }
        List list = new ArrayList<>();
        List list1 = new ArrayList<>(data);
        for (int i = data.size() - 1; i >= 0; i--) {
            list.add(list1.get(i));
        }
        pageUtil.setData(list);
        pageUtil.setPageCount(count);
        return pageUtil;
    }

    private List<CommentInfoNewest> insertFComment(List<CommentInfoNewest> listNewest, CommentInfo commentInfo) {

        for (int i = 0; i < listNewest.size(); i++) {
            CommentInfoNewest commentInfoNewest = listNewest.get(i);
            if (commentInfoNewest.getCommentId().equals(commentInfo.getCommentMid())) {
                commentInfoNewest = cyclicDispose(commentInfo, commentInfoNewest);
            }

        }
        return listNewest;
    }

    private CommentInfoNewest cyclicDispose(CommentInfo commentInfo, CommentInfoNewest commentInfoNewest) {
        if (commentInfoNewest.getCommentId().equals(commentInfo.getCommentFid())) {
            List<CommentInfoNewest> commentInfoNewestList = JSONArray.parseArray(commentInfoNewest.getCommentInfos().toString(), CommentInfoNewest.class);
            Set<CommentInfo> commentInfoNewestSet = new TreeSet<>();
            if (commentInfoNewestList != null) {
                commentInfoNewestSet.addAll(commentInfoNewestList);
            }
            if (commentInfoNewestSet == null) {
                commentInfoNewestSet = new TreeSet<>();
                commentInfoNewestSet.add(commentInfo);
                commentInfoNewest.setCommentInfos(commentInfoNewestSet);
            } else {
                commentInfoNewestSet.add(commentInfo);
            }
            commentInfoNewest.setCommentInfos(commentInfoNewestSet);
        } else {
            if (commentInfoNewest.getCommentInfos() != null) {
                Set commentInfoNewests = commentInfoNewest.getCommentInfos();
                Set<CommentInfoNewest> newCommonSet = new TreeSet<>();
                for (Object comment : commentInfoNewests) {
                    CommentInfoNewest commentInfoN = JSONObject.parseObject(comment.toString(), CommentInfoNewest.class);
                    commentInfoN = cyclicDispose(commentInfo, commentInfoN);
                    newCommonSet.add(commentInfoN);
                }
                commentInfoNewest.setCommentInfos(newCommonSet);
            }
        }
        return commentInfoNewest;
    }


    public List<CommentInfoNewest> getRedisData(CacheManagerUtils cacheManager, String infoId, Integer x,
                                                String pullType) {
        String newestInfodata = cacheManager.readMap(infoId + "-" + x + "-" + pullType);
        Map newestInfoJsondata = JSONArray.parseObject(newestInfodata);
        if (newestInfoJsondata == null || newestInfoJsondata.size() == 0) {
            return null;
        }
        Set<CommentInfoNewest> set = new TreeSet<>();
        for (Object value : newestInfoJsondata.values()) {
            set.add(JSONObject.parseObject(value.toString(), CommentInfoNewest.class));
        }
        return new ArrayList(set);
    }


}
