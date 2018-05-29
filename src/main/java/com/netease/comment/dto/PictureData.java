package com.netease.comment.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PictureData {

    private long height;

    private long size;

    private String type;

    private String url;

    private String desc;

    private long width;

    public PictureData() {}

    public PictureData(long height, long size, String type, String url, long width) {
        this.height = height;
        this.size = size;
        this.type = type;
        this.url = url;
        this.width = width;
    }

}
