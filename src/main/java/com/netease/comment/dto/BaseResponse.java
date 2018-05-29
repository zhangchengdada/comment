package com.netease.comment.dto;


import com.netease.comment.enums.ResponseCodeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 基本响应
 *
 * @author
 * @create 2018-02-03 21:42
 */
@Setter
@Getter
public class BaseResponse<T> {
    /**
     * 请求id
     */
    private String requestId;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 携带的消息
     */
    private String message;

    /**
     * 携带的数据
     */
    private T data;

    public BaseResponse(String requestId, int status, String msg, T data) {
        this.code = status;
        this.message = msg;
        this.requestId = requestId;
        this.data = data;
    }

    public BaseResponse() {
    }



    public static  <T>  BaseResponse createError(ResponseCodeEnum code, String requestId) {
        BaseResponse response = new BaseResponse<>();
        response.code = code.getCode();
        response.message = code.getMsg();
        response.requestId = requestId;
        return response;
    }


    public static  <T>  BaseResponse createSuccess( String requestId) {
        BaseResponse response = new BaseResponse<>();
        response.code = ResponseCodeEnum.SUCCESS_CODE.getCode();
        response.message = ResponseCodeEnum.SUCCESS_CODE.getMsg();
        response.requestId = requestId;
        return response;
    }

    public static  <T>  BaseResponse createSuccess( T data , String requestId) {
        BaseResponse response = new BaseResponse<>();
        response.code = ResponseCodeEnum.SUCCESS_CODE.getCode();
        response.message = ResponseCodeEnum.SUCCESS_CODE.getMsg();
        response.data = data;
        response.requestId = requestId;
        return response;
    }


}
