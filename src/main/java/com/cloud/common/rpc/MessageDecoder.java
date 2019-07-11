package com.cloud.common.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private enum State {
        HEADER,
        BODY
    }

    //当前读取状态
    private State state = State.HEADER;

    //序号
    private int sequence;
    //数据体大小
    private int bodySize;
    //数据体的值
    private String bodyData;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (state == State.HEADER) {
            if (in.readableBytes() < 8) {
                return;
            }
            sequence = in.readInt();
            bodySize = in.readInt();
            state = State.BODY;
        }
        if (state == State.BODY) {
            if (in.readableBytes() < bodySize) {
                return;
            }
            readBody(in);
            out.add(new Message(sequence, bodySize, bodyData));
            state = State.HEADER;
        }
    }

    private void readBody(ByteBuf in) {
        ByteBuf buf = in.readBytes(bodySize);
        bodyData = buf.toString(CharsetUtil.UTF_8);
        buf.release();
    }

}
