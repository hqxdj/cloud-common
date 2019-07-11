package com.cloud.common.rpc;

import lombok.Data;

@Data
public class Request {

    private String versionInfo;
    private String sessionInfo;
    private String requestParam;
    private String requestUri;

}
