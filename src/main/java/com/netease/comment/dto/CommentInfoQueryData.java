package com.netease.comment.dto;


import com.netease.comment.enums.PullTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Setter
@Getter
@ToString
public class CommentInfoQueryData {

    @NotNull(message = "文章id不能为空")
    private String infoId;

    /**
     * 拉取类型（0：最热，1：最新）
     */
    @Min(0)
    @Max(1)
    private Integer pullTypeEnum ;

    /**
     * 设备id
     */
    @NotNull(message = "设备id不能为空")
    private String deviceId;

    /**
     * app iD
     */
    @NotNull(message = "应用id不能为空")
    private String appId;

    /**
     * 用户id
     */
    private String userId;

    private Integer pageNo = 1;

    private Integer pageSize = 10;

}
