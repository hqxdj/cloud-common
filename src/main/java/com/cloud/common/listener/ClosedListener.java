package com.cloud.common.listener;

import com.cloud.common.rpc.RPCClient;
import com.cloud.common.rpc.RPCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ClosedListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private RPCServer rpcServer;

    @Autowired
    private RPCClient rpcClient;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        rpcServer.close();
        rpcClient.close();
    }
}
