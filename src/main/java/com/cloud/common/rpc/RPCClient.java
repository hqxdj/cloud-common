package com.cloud.common.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class RPCClient {

    private Map<String, Channel> channelMap = new HashMap<>();

    @Autowired
    private ClientHandler clientHandler;

    private NioEventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    public void close() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();

            eventLoopGroup = null;
            bootstrap = null;
        }
    }

    public void send(String host, int port, Message message) throws Exception {
        String key = getKey(host, port);
        Channel channel = channelMap.get(key);
        if (channel == null) {
            synchronized (this) {
                channel = channelMap.get(key);
                if (channel == null) {
                    channel = connect(host, port);
                }
            }
        }
        if (channel == null) {
            throw new SocketException("connect " + key + " failure");
        }
        if (!channel.isActive()) {
            channelMap.remove(key);
            throw new SocketException(key + " inactive");
        }
        channel.writeAndFlush(message);
    }

    public void remove(Channel channel) {
        Iterator<String> itor = channelMap.keySet().iterator();
        while (itor.hasNext()) {
            String key = itor.next();
            if (channelMap.get(key) == channel) {
                channelMap.remove(key);
                break;
            }
        }
    }

    private void init() {
        if (bootstrap == null) {
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new MessageEncoder());
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(clientHandler);
                }
            });
        }
    }

    private Channel connect(String host, int port) throws Exception {
        init();

        Channel channel = null;
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        if (channelFuture.isSuccess()) {
            channel = channelFuture.channel();
            channelMap.put(getKey(host, port), channel);
        }
        return channel;
    }

    private String getKey(String host, int port) {
        return host + ":" + port;
    }

}
