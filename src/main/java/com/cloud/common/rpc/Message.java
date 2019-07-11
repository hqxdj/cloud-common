package com.cloud.common.rpc;

import lombok.Data;

@Data
public class Message {

    //序号
    private int sequence;
    //数据体大小
    private int bodySize;
    //数据体的值
    private String bodyData;

    public Message() {

    }

    public Message(String bodyData) {
        this.bodyData = bodyData;
        this.sequence = Sequence.next();
    }

    public Message(int sequence, int bodySize, String bodyData) {
        this.sequence = sequence;
        this.bodySize = bodySize;
        this.bodyData = bodyData;
    }

}
