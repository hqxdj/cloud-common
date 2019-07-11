package com.cloud.common.transaction;

import lombok.Data;

@Data
public class TCCRequest<T> {

    private static final int STATUS_TRY = 0;
    private static final int STATUS_COMMIT = 1;
    private static final int STATUS_CANCEL = 2;

    /**
     * 全局事务ID
     */
    private String tid;

    /**
     * 当前状态，0表示尝试，1表示提交，2表示取消
     */
    private int status = 0;

    /**
     * 业务数据
     */
    private T data;

    public TCCRequest(String tid) {
        this.tid = tid;
    }

    public boolean isTry() {
        if (this.status == STATUS_TRY) {
            return true;
        }
        return false;
    }

    public void setStatusTry() {
        this.status = STATUS_TRY;
    }

    public void setStatusCommit() {
        this.status = STATUS_COMMIT;
    }

    public void setStatusCancel() {
        this.status = STATUS_CANCEL;
    }

}
