package com.netease.comment.enums;

import lombok.Getter;


@Getter
public enum ResponseCodeEnum {

    SUCCESS_CODE(0, "success"),
    SERVER_EXCEPTION(100, "未知异常"),
    DATABASE_ERROR(1000, "数据库异常"),
    COMMENT_QUERY_ERROR(2000, "评论信息拉取异常"),
    COMMENT_PAGE_ERROR(2001, "评论信息拉取超出评论总数"),
    PARAMETER_ERROR(-1, "参数异常");

    private int code;

    private String msg;

    ResponseCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
