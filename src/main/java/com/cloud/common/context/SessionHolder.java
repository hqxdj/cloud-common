package com.cloud.common.context;

import com.cloud.common.bean.SessionInfo;
import org.springframework.stereotype.Component;

@Component
public class SessionHolder {

    public static final String SESSION_INFO = "SessionInfo";

    private ThreadLocal<SessionInfo> holder = new ThreadLocal();

    public void set(SessionInfo sessionInfo) {
        holder.set(sessionInfo);
    }

    public SessionInfo get() {
        return holder.get();
    }

    public void remove() {
        holder.remove();
    }

}
