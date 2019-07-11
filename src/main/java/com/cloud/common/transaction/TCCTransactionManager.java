package com.cloud.common.transaction;

import com.cloud.common.bean.ResponseInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TCCTransactionManager {

    private ThreadLocal<List<TCCInvoke>> allHolder = new ThreadLocal();
    private ThreadLocal<List<TCCInvoke>> successHolder = new ThreadLocal();

    /**
     * 获取全局事务ID
     *
     * @return
     */
    public String getTid() {
        String dateTime = LocalDateTime.now().toString().substring(0, 19);
        dateTime = dateTime.replace("-", "").replace("T", "").replace(":", "");
        return dateTime + RandomStringUtils.randomNumeric(10);
    }

    /**
     * 执行
     *
     * @param invoke
     */
    public void execute(TCCInvoke invoke) {
        List<TCCInvoke> list = allHolder.get();
        if (list != null) {
            list.add(invoke);
        }
    }

    /**
     * 正式处理
     */
    boolean handle() {
        List<TCCInvoke> list = allHolder.get();
        for (TCCInvoke invoke : list) {
            if (!call(invoke)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 开始事务
     */
    void begin() {
        allHolder.set(new ArrayList());
        successHolder.set(new ArrayList());
    }

    /**
     * 提交事务
     */
    boolean commit() {
        List<TCCInvoke> list = allHolder.get();
        for (TCCInvoke invoke : list) {
            TCCRequest request = invoke.getTccRequest();
            request.setStatusCommit();
            request.setData(null);
            if (!call(invoke)) {
                return false;
            }
        }
        clean();
        return true;
    }

    /**
     * 取消事务
     */
    void cancel() {
        List<TCCInvoke> successList = successHolder.get();
        for (TCCInvoke invoke : successList) {
            TCCRequest request = invoke.getTccRequest();
            request.setStatusCancel();
            request.setData(null);
            invoke.run();
        }
        clean();
    }

    /**
     * 清理
     */
    void clean() {
        allHolder.remove();
        successHolder.remove();
    }

    /**
     * 调用
     *
     * @param invoke
     * @return
     */
    boolean call(TCCInvoke invoke) {
        ResponseInfo responseInfo = invoke.run();
        if (!responseInfo.successed()) {
            return false;
        }
        successHolder.get().add(invoke);
        return true;
    }

}
