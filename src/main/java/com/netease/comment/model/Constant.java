package com.netease.comment.model;



import java.time.format.DateTimeFormatter;


/**
 * Created by liznzn on 2017/7/4.
 */
public class Constant {



    // 用户浏览记录缓存时间
    public static final int USER_BROWSING_HISTORY_TTL = 60 * 24 * 60 * 60;
    //信息过期时间
    public static final int NEWS_TTL = 3 * 24 * 60 * 60;

    // 日期格式
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // top algInfo
    public static final String topAlgInfo = "|F_TOP|";

    // banner algInfo
    public static final String bannerAlgInfo = "|F_PIC|";

    // user news algInfo
    public static final String userNewsAlgInfo = "|F_FRC|";


    // 统计日志名常量
    public static final String LOG_STAT_NAME = "logStatName";           // 日志统计名
    public static final String LOG_STAT_FLAG = "logStatFlag";           // 日志统计标识

    // 打点属性
    public static final String ATTR_START_TIME = "attrStartTime";       // 请求的开始时间
    public static final String ATTR_END_TIME = "attrEndTime";           // 请求的结束时间
}
