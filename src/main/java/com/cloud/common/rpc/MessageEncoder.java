package com.cloud.common.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] bytes = msg.getBodyData().getBytes(CharsetUtil.UTF_8);
        out.writeInt(msg.getSequence());
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
