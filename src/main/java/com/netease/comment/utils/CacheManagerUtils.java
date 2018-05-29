package com.netease.comment.utils;


import com.alibaba.fastjson.JSON;
import com.netease.comment.model.CommentInfoNewest;
import com.netease.comment.model.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @Description
 * @Author hzlixingxing
 * @Date 2017/7/24
 */
public class CacheManagerUtils {
    private final static Logger logger = LoggerFactory.getLogger(CacheManagerUtils.class);

    private static CacheManagerUtils cacheManager = new CacheManagerUtils();

    private CacheManagerUtils() {
    }

    public static CacheManagerUtils getInstance() {
        return cacheManager;
    }


    /**
     * 向缓存中写入映射关系
     *
     * @param key   缓存的键
     * @param value 缓存的值
     */
    public void writeMap(String key, String value) {
        if (null == key || null == value) {
            return;
        }
        try {
            RedisUtils.setStringEx(key, value, Constant.NEWS_TTL);
        } catch (Exception e) {
            logger.error("write redis error, key={}, value={}, e={}", key, value, e.toString());
        }
    }


    /**
     * 从缓存中读取映射关系
     *
     * @param key 缓存的键
     * @return 缓存的值
     */
    public String readMap(String key) {
        if (null == key) {
            return null;
        }
        try {
            return RedisUtils.getString(key);
        } catch (Exception e) {
            logger.error("read redis error: key={}, e={}", key, e.toString());
            return null;
        }
    }


    /**
     * 向缓存写入数据库Model对象
     *
     * @param key
     * @param model
     * @param <T>   model类型
     */
    public <T> void writeModel(String key, T model) {
        if (null == key || null == model) {
            return;
        }
        try {
            String value = JSON.toJSONString(model);
            RedisUtils.setStringEx(key, value, Constant.NEWS_TTL);
        } catch (Exception e) {
            logger.error("write redis error, key={}, object={}", key, model.toString(), e);
        }
    }

    /**
     * 从缓存读取json串并反序列
     *
     * @param key
     * @param clazz 类对象
     * @param <T>   反序列化对象类型
     * @return
     */
    public <T> T readModel(String key, Class<T> clazz) {
        if (null == key || null == clazz) {
            return null;
        }
        try {
            String value = RedisUtils.getString(key);
            if (StringUtils.isBlank(value)) {
                return null;
            } else {
                return JSON.parseObject(value, clazz);
            }
        } catch (Exception e) {
            logger.error("read {} error: key={}", clazz, key, e);
            return null;
        }
    }

    /**
     * 从缓存读取json串并反序列成List
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> LinkedList<T> readModelLinkedList(String key, Type type) {
        if (null == key) {
            return null;
        }
        try {
            String value = RedisUtils.getString(key);
            if (StringUtils.isBlank(value)) {
                return null;
            } else {
                LinkedList<T> t = JSON.parseObject(value, type);
                return t;
            }
        } catch (Exception e) {
            logger.error("read model list error: key={}", key, e);
            return null;
        }
    }




    /**
     * 向缓存写入InfoModel列表
     *
     * @param commentInfos
     */
    public void writeInfoModels(List<CommentInfoNewest> commentInfos, String producer) {
        if (null == commentInfos || commentInfos.isEmpty()) {
            return;
        }
        for (CommentInfoNewest commentInfo : commentInfos) {
            if (null != commentInfo) {
                String key = producer + ":info:id:" + commentInfo.getInfoId();
                writeModel(key, commentInfo);
            }
        }
    }

    /**
     * 删除key对应的value
     *
     * @param key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        if (null == key) {
            return false;
        }
        return RedisUtils.delString(key);
    }

    /**
     * 更新用户轮播图、置顶新闻浏览历史
     * @param key 轮播图、置顶新闻浏览历史对应的key
     * @param userNewsList 当次浏览的轮播图、置顶新闻
     * @param requestId 当次请求ID
     */
  /*  public void updateBannerOrTopsHistory (String key, List<UserNews> userNewsList, String requestId) {
        if (null == key || null == userNewsList || userNewsList.isEmpty()) {
            return;
        }
        try {
            Map<String, String> history = new LinkedHashMap<>();
            for (UserNews userNews : userNewsList) {
                if (null != userNews && !Strings.isNullOrEmpty(userNews.getInfoId())) {
                    history.put(userNews.getInfoId(), JSON.toJSONString(userNews));
                }
            }
            // 如果轮播图、置顶文章存在就覆盖记录，否则新增记录
            if (history.size() > 0) {
                RedisUtils.hmset(key, Constant.USER_BROWSING_HISTORY_TTL, history);
            }
        } catch (Exception e) {
            logger.error("requestId={}, update banners or tops history error, key={}, userNewsList={}", requestId, key,
                    userNewsList, e);
        }
    }*/

    /**
     * 读取一个用户轮播图、置顶新闻浏览历史
     * @param key 轮播图、置顶新闻浏览历史对应的key
     * @param requestId 当次请求ID
     * @return 用户浏览过的轮播图、置顶新闻
     */
   /* public HashSet<UserNews> readUserBannersOrTopsHistory (String key, String requestId) {
        if (null == key) {
            return null;
        }
        try {
            List<String> histories = RedisUtils.hvals(key);
            if (null == histories || histories.isEmpty()) {
                return null;
            }
            HashSet<UserNews> bannersOrTops = new HashSet<>();
            List<String> overdueIds = new LinkedList<>();
            LocalDateTime now = LocalDateTime.now();
            for (String history : histories) {
                if (Strings.isNullOrEmpty(history))
                    continue;
                UserNews userNews = JSON.parseObject(history, UserNews.class);
                bannersOrTops.add(userNews);
                if (now.isAfter(userNews.getExpireTime())){
                    overdueIds.add(userNews.getInfoId());
                }
            }
            if (overdueIds.size() > 0) {
                // 删除轮播图置顶记录中过期的数据
                logger.debug("requestId={}, delete overdue banner or top history, ids={}, redis key={}", requestId,
                        overdueIds, key);
                RedisUtils.hdel(key, overdueIds.toArray(new String[overdueIds.size()]));
            }
            return bannersOrTops;
        } catch (Exception e) {
            logger.error("requestId={}, read banners or tops history error, key={}", requestId, key, e);
            return null;
        }
    }*/

    /**
     * 更新普通用户新闻浏览历史
     * @param key 普通用户新闻浏览历史对应的key
     * @param userNewsList 当次浏览的普通新闻
     * @param requestId 当次请求ID
     */
   /* public void updateUserNewsHistory (String key, List<UserNews> userNewsList, String requestId) {
        if (null == key || null == userNewsList || userNewsList.isEmpty()) {
            return;
        }
        try {
            LinkedList<String> history = new LinkedList<>();
            for (UserNews userNews : userNewsList) {
                if (null != userNews) {
                    history.add(JSON.toJSONString(userNews));
                }
            }
            if (history.size() > 0) {
                RedisUtils.lpush(key, Constant.USER_BROWSING_HISTORY_TTL, history.toArray(new String[history.size()]));
            }
        } catch (Exception e) {
            logger.error("requestId={}, update user normal news history error, key={}, userNewsList={}", requestId,
                    key, userNewsList, e);
        }
    }*/

    /**
     * 读取一个用户普通新闻浏览历史
     * @param key 普通新闻浏览历史对应的key
     * @param requestId 当次请求ID
     * @return 用户浏览过的普通新闻
     */
   /* public HashSet<UserNews> readUserNewsHistory (String key, String requestId) {
        if (null == key) {
            return null;
        }
        try {
            List<String> histories = RedisUtils.lrange(key, 0, -1);
            if (null == histories || histories.isEmpty()) {
                return null;
            }
            HashSet<UserNews> userNewsHistory = new HashSet<>();
            List<String> overdueUserNewsList = new LinkedList<>();
            LocalDateTime now = LocalDateTime.now();
            for (String history : histories) {
                if (Strings.isNullOrEmpty(history))
                    continue;
                UserNews userNews = JSON.parseObject(history, UserNews.class);
                userNewsHistory.add(userNews);
                if (now.isAfter(userNews.getExpireTime())) {
                    overdueUserNewsList.add(history);
                }
            }
//            if (overdueUserNewsList.size() > 0) {
//                // 删除用户浏览记录中过期的数据
//                logger.info("requestId={}, delete overdue user browsing history={}, redis key={}, ", requestId,
//                        overdueUserNewsList, key);
//                RedisUtils.lrem(key, 0, overdueUserNewsList);
//            }
            return userNewsHistory;
        } catch (Exception e) {
            logger.error("requestId={}, read user browsing history error, key={}", requestId, key, e);
            return null;
        }
    }*/

    /**
     * 更新用户新闻历史bf
     * @param key
     * @param bf
     */
//    public void updateUserNewsHistoryBloomFilter(String key , BloomFilter bf) {
//        if (null == key || null == bf) {
//            return;
//        }
//        try {
//            RedisUtils.setStringEx(key, bf.toString(), 5);
//        } catch (Exception e) {
//            logger.error("requestId={}, update user normal news history bloom filter error, key={}", key, e);
//        }
//    }
}
