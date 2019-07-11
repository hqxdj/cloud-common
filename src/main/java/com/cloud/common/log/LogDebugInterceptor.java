package com.cloud.common.log;

import com.cloud.common.support.AopHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogDebugInterceptor {

    @Autowired
    private AopHandler aopHandler;

    @Around("@within(com.cloud.common.log.LogDebug)")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        return aopHandler.execute(point, null);
    }

}
