package com.cloud.common.util;

import com.cloud.common.bean.ResponseInfo;

/**
 * 返回结果工具类
 */

public class ResultUtils {

    public static ResponseInfo success(Object data) {
        ResponseInfo response = new ResponseInfo();
        response.setCode(0000);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static ResponseInfo success() {
        ResponseInfo response = new ResponseInfo();
        response.setCode(0000);
        response.setMessage("success");
        response.setData(new Object());
        return response;
    }

    public static ResponseInfo failure() {
        ResponseInfo response = new ResponseInfo();
        response.setCode(9999);
        response.setMessage("failure");
        response.setData(new Object());
        return response;
    }

    public static ResponseInfo error(int code, String message) {
        ResponseInfo response = new ResponseInfo();
        response.setCode(code);
        response.setMessage(message);
        response.setData(new Object());
        return response;
    }
}
