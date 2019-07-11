package com.cloud.common.support;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Slf4j
@Component
public class AopHandler {

    public Object execute(ProceedingJoinPoint point, Callable callable) throws Throwable {
        if (log.isDebugEnabled()) {
            //打印参数
            logArgs(point);
            //调用方法
            long startTime = System.currentTimeMillis();
            Object result;
            if (callable == null) {
                result = point.proceed();
            } else {
                result = callable.call();
            }
            long costTime = System.currentTimeMillis() - startTime;
            //打印结果
            logResult(point, result, costTime);

            return result;
        } else {
            Object result;
            if (callable == null) {
                result = point.proceed();
            } else {
                result = callable.call();
            }
            return result;
        }
    }

    private void logArgs(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args.length > 0) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                params.append(args[i].toString() + "   ");
            }
            log.debug(point.getSignature().toShortString() + " params >>> {}", params.toString());
        }
    }

    private void logResult(ProceedingJoinPoint point, Object result, long costTime) {
        log.debug(point.getSignature().toShortString() + " result >>> {} costTime >>> {}", JSON.toJSONString(result), costTime);
    }

}
