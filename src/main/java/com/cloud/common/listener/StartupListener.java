package com.cloud.common.listener;

import com.cloud.common.context.RequestMappingContext;
import com.cloud.common.rpc.RPCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationRunner {

    @Autowired
    private RequestMappingContext requestMappingContext;

    @Autowired
    private RPCServer rpcServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        requestMappingContext.load();
        rpcServer.start();
    }
}
