package com.cloud.common.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.cloud.common.rpc.WaitFuture;
import lombok.Data;

@Data
public class ResponseInfo<T> {
    public static final int SUCCESS = 200;
    public static final int FAILURE = 500;
    public static final int UNAVAILABLE = 503;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_ACCEPTABLE = 406;

    private int code;
    private String message;
    private T data;

    @JSONField(serialize = false)
    private WaitFuture waitFuture;

    public ResponseInfo() {
    }

    public ResponseInfo(int code) {
        setCode(code);
    }

    public ResponseInfo(int code, T data) {
        setCode(code);
        this.data = data;
    }

    public ResponseInfo(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static ResponseInfo success() {
        return new ResponseInfo(SUCCESS);
    }

    public static ResponseInfo failure() {
        return new ResponseInfo(FAILURE);
    }

    @JSONField(serialize = false)
    public boolean successed() {
        return this.code == SUCCESS ? true : false;
    }

    public void setCode(int code) {
        this.code = code;
        if (code == SUCCESS) {
            this.message = "success";
        } else {
            this.message = "failure";
        }
    }

}