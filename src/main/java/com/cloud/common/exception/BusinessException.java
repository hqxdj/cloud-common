package com.cloud.common.exception;

import lombok.Data;

/**
 * @author xdj
 * @version V1.0
 * @ProjectName: cloud-common
 * @Description: 业务异常
 * @Date 2019/7/24 9:36
 * Copyright (c)   xdj
 */
@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
