package com.jc.gateway.enums;

/**
 * 
 * @author lxs
 *
 */
public enum ResultCodes {

    /**
     * SUCCESS
     */
    SUCCESS(10000, "成功"),

    /**
     * BAD_REQUEST
     */
    BAD_REQUEST(40000, "参数错误"),

    /**
     * UNAUTHORIZED
     */
    UNAUTHORIZED(40001, "未授权"),

    /**
     * TOKEN_INVALID
     */
    TOKEN_INVALID(40003, "token失效"),

    /**
     * no login
     */
    NO_LOGIN(40005, "未登录"),

    /**
     * NOT_FOUND
     */
    NOT_FOUND(40404, "记录不存在"),

    /**
     * INTERNAL_ERROR
     */
    INTERNAL_ERROR(50000, "内部错误");

    private int code;
    private String msg;

    ResultCodes(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
