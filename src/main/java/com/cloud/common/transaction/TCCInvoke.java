package com.cloud.common.transaction;

import com.cloud.common.bean.ResponseInfo;

public abstract class TCCInvoke {

    private TCCRequest request;

    public TCCInvoke(TCCRequest request) {
        this.request = request;
    }

    public TCCRequest getTccRequest() {
        return request;
    }

    public abstract ResponseInfo run();

}
