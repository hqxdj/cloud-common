package com.cloud.common.rpc;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Sharable
@Component
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    @Autowired
    private RPCClient rpcClient;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        int sequence = msg.getSequence();
        WaitFuture waitFuture = WaitFutureManager.get(sequence);
        if (waitFuture != null) {
            WaitFutureManager.remove(sequence);
            Type returnType = waitFuture.getReturnType();
            Object result = JSON.parseObject(msg.getBodyData(), returnType);
            waitFuture.setResult(result);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  throws Exception {
        rpcClient.remove(ctx.channel());
        ctx.close();
    }
}
