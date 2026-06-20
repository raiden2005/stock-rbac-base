package com.stock.rbac.vo;

import com.stock.rbac.constant.SsoConstants;

public class Result<T> {

    private Integer code;

    private String msg;

    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SsoConstants.HTTP_200, "成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SsoConstants.HTTP_200, "成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SsoConstants.HTTP_200, msg, data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error401(String msg) {
        return new Result<>(SsoConstants.HTTP_401, msg, null);
    }

    public static <T> Result<T> error403(String msg) {
        return new Result<>(SsoConstants.HTTP_403, msg, null);
    }

    public static <T> Result<T> error500(String msg) {
        return new Result<>(SsoConstants.HTTP_500, msg, null);
    }
}
