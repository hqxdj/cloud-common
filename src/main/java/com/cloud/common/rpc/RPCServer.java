package com.cloud.common.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(RPCServerProperties.class)
public class RPCServer {

    @Autowired
    private RPCServerProperties rpcServerProperties;

    @Autowired
    private ServerHandler serverHandler;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private EventExecutorGroup handlerGroup;
    private boolean started;

    public void start() {
        if (started || rpcServerProperties.isEnabled() == false) {
            return;
        }
        started = true;

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        handlerGroup = new DefaultEventExecutorGroup(rpcServerProperties.getMaxThreads());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1000);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new LoggingHandler(LogLevel.INFO));
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new MessageEncoder());
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(handlerGroup, serverHandler);
                }
            });

            //绑定端口启动
            bootstrap.bind(rpcServerProperties.getPort()).sync();
            log.info("rpc server bind port {}", rpcServerProperties.getPort());
        } catch (Exception e) {
            log.error("RPCServer start error", e);
        }
    }

    public void close() {
        if (bossGroup != null && workerGroup != null && handlerGroup != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            handlerGroup.shutdownGracefully();

            bossGroup = null;
            workerGroup = null;
            handlerGroup = null;
            started = false;
        }
    }

}
