package com.netease.comment.enums;

import lombok.Getter;

@Getter
public enum PullTypeEnum {

    HOTTEST("hottest"), NEWEST("newest");

    String type;

    PullTypeEnum(String msg) {
        this.type = msg;
    }
}
