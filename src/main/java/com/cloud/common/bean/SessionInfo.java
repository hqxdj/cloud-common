package com.cloud.common.bean;

import lombok.Data;

@Data
public class SessionInfo {

    private String token;
    private String userId;
    private String userName;

}
