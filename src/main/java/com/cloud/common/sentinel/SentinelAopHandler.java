package com.cloud.common.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cloud.common.support.AopHandler;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SentinelAopHandler {

    @Autowired
    private AopHandler aopHandler;

    public Object handle(ProceedingJoinPoint point) throws Throwable {
        String resourceName = getResourceName(point);
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);
            return aopHandler.execute(point, null);
        } catch (BlockException ex) {
            log.warn("{} blocked", resourceName);
            throw ex;
        } catch (Throwable ex) {
            log.error("execute error", ex);
            Tracer.trace(ex);
            throw ex;
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private String getResourceName(ProceedingJoinPoint point) {
        // 获取类名
        String className;
        Class<?> classTarget = point.getTarget().getClass();
        Class<?>[] interfaces = classTarget.getInterfaces();
        if (interfaces.length != 0) {
            className = interfaces[0].getSimpleName();
        } else {
            className = classTarget.getSimpleName();
        }

        // 获取方法名
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String methodName = methodSignature.getMethod().getName();
        return className + "/" + methodName;
    }

}
