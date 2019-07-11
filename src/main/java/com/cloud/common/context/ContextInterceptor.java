package com.cloud.common.context;

import com.alibaba.fastjson.JSON;
import com.cloud.common.bean.SessionInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ContextInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private VersionHolder versionHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        sessionHolder.remove();
        versionHolder.remove();

        String sessionStr = request.getHeader(SessionHolder.SESSION_INFO);
        if (StringUtils.isNotEmpty(sessionStr)) {
            SessionInfo sessionInfo = JSON.parseObject(sessionStr, SessionInfo.class);
            sessionHolder.set(sessionInfo);
        }

        String version = request.getHeader(VersionHolder.VERSION_INFO);
        if (StringUtils.isNotEmpty(version)) {
            versionHolder.set(version);
        }
        return true;
    }

}
