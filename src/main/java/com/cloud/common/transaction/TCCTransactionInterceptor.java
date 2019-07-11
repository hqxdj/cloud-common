package com.cloud.common.transaction;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TCCTransactionInterceptor {

    @Autowired
    private TCCTransactionManager transactionManager;

    @Around("@within(com.cloud.common.transaction.TCCTransactional)")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        transactionManager.begin();
        Object result = point.proceed();
        if (transactionManager.handle()) {
            if (transactionManager.commit()) {
                return result;
            }
        }
        transactionManager.cancel();
        throw new RuntimeException("TCCTransaction handle failure");
    }

}
