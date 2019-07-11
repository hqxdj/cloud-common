package com.cloud.common.rpc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("rpc.server")
public class RPCServerProperties {

    private int port;
    private int maxThreads;
    private boolean enabled = false;

}
