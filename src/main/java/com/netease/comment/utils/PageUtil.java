package com.netease.comment.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class PageUtil<T> {

    /**
     * 每页数量
     */
    private Integer pageSize;
    /**
     * 页码
     */
    private Integer pageNo;
    /**
     * 总记录数
     */
    private Integer pageCount;
    /**
     * 数据
     */
    private  T data;



}
