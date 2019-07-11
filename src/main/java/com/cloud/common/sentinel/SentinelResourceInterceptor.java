package com.cloud.common.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SentinelResourceInterceptor {

    @Autowired
    private SentinelAopHandler sentinelAopHandler;

    @Around("@within(com.cloud.common.sentinel.SentinelResource)")
    public Object doSentinelResource(ProceedingJoinPoint point) throws Throwable {
        return sentinelAopHandler.handle(point);
    }

    @Around("target(com.baomidou.mybatisplus.core.mapper.BaseMapper)")
    public Object doBaseMapper(ProceedingJoinPoint point) throws Throwable {
        return sentinelAopHandler.handle(point);
    }

}
