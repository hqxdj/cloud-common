package com.cloud.common.feign;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.cloud.common.bean.ResponseInfo;
import com.cloud.common.context.SessionHolder;
import com.cloud.common.context.VersionHolder;
import com.cloud.common.ribbon.RibbonServerSelector;
import com.cloud.common.rpc.*;
import com.cloud.common.support.AopHandler;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

@Slf4j
@Aspect
@Component
public class FeignInvokeInterceptor implements RequestInterceptor {

    @Autowired
    private RPCClient rpcClient;

    @Autowired
    private RibbonServerSelector ribbonServerSelector;

    @Autowired
    private VersionHolder versionHolder;

    @Autowired
    private SessionHolder sessionHolder;

    @Autowired
    private AopHandler aopHandler;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestTemplate.header(SessionHolder.SESSION_INFO, request.getHeader(SessionHolder.SESSION_INFO));
            requestTemplate.header(VersionHolder.VERSION_INFO, request.getHeader(VersionHolder.VERSION_INFO));
        }
    }

    @Around("@annotation(com.cloud.common.feign.FeignInvoke)")
    public Object doAround(ProceedingJoinPoint point) {
        // 获取调用属性
        InvokeAttribute invokeAttribute = getInvokeAttribute(point);
        String resourceName = invokeAttribute.buildResourceName();

        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);
            // 判断是否RPC调用
            if (invokeAttribute.isRpc) {
                return aopHandler.execute(point, () -> rpcInvoke(point, invokeAttribute));
            }
            return aopHandler.execute(point, null);
        } catch (BlockException ex) {
            log.warn("{} blocked", resourceName);
            return new ResponseInfo(ResponseInfo.UNAVAILABLE);
        } catch (Throwable ex) {
            log.error("invoke error", ex);
            Tracer.trace(ex);
            return ResponseInfo.failure();
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * 获取调用属性
     *
     * @param point
     * @return InvokeAttribute
     */
    private InvokeAttribute getInvokeAttribute(ProceedingJoinPoint point) {
        //获取调用属性
        String requestUri = null;
        FeignInvoke feignInvoke = null;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Annotation[] annotations = methodSignature.getMethod().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof FeignInvoke) {
                feignInvoke = (FeignInvoke) annotation;
            } else if (annotation instanceof GetMapping) {
                requestUri = ((GetMapping) annotation).value()[0];
            } else if (annotation instanceof PostMapping) {
                requestUri = ((PostMapping) annotation).value()[0];
            } else if (annotation instanceof PutMapping) {
                requestUri = ((PutMapping) annotation).value()[0];
            } else if (annotation instanceof DeleteMapping) {
                requestUri = ((DeleteMapping) annotation).value()[0];
            } else if (annotation instanceof RequestMapping) {
                requestUri = ((RequestMapping) annotation).value()[0];
            }
        }
        if (requestUri == null) {
            throw new RuntimeException("feign client not found request uri");
        }
        if (feignInvoke == null) {
            throw new RuntimeException("feign client not found FeignInvoke annotation");
        }
        boolean isRpc = feignInvoke.isRpc();
        boolean isAsync = feignInvoke.isAsync();
        int timeout = feignInvoke.timeout();

        //获取调用服务名
        Class<?> classTarget = point.getTarget().getClass().getInterfaces()[0];
        String serviceName = classTarget.getAnnotation(FeignClient.class).name();
        return new InvokeAttribute(serviceName, requestUri, isRpc, isAsync, timeout);
    }

    /**
     * RPC调用
     *
     * @param point
     * @param invokeAttribute
     * @return Object
     * @throws Exception
     */
    private Object rpcInvoke(ProceedingJoinPoint point, InvokeAttribute invokeAttribute) throws Exception {
        String serviceId = getServiceId(point);
        ServiceInstance instance = ribbonServerSelector.select(serviceId);
        if (instance != null) {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            String[] parameterNames = methodSignature.getParameterNames();
            Object[] parameterValues = point.getArgs();
            Message message = createMessage(invokeAttribute.requestUri, parameterNames, parameterValues);

            WaitFuture future = new WaitFuture(message.getSequence(), invokeAttribute.timeout, methodSignature.getMethod().getGenericReturnType());
            WaitFutureManager.add(future);
            try {
                int port = Integer.parseInt(instance.getMetadata().get("rpcServerPort"));
                rpcClient.send(instance.getHost(), port, message);
                if (invokeAttribute.isAsync) {
                    ResponseInfo responseInfo = new ResponseInfo();
                    responseInfo.setWaitFuture(future);
                    return responseInfo;
                }
                Object result = future.get();
                if (result == null) {
                    throw new TimeoutException();
                }
                return result;
            } catch (Exception e) {
                WaitFutureManager.remove(future.getSequence());
                throw e;
            }
        }
        throw new RuntimeException(serviceId + " service unavailable");
    }

    /**
     * 获取服务ID
     *
     * @param point
     * @return String
     */
    private String getServiceId(ProceedingJoinPoint point) {
        String serviceId = null;
        String target = point.getTarget().toString();
        String[] arr = target.split(",");
        for (String s : arr) {
            if (s.contains("name=")) {
                serviceId = s.split("=")[1].trim();
                break;
            }
        }
        return serviceId;
    }

    /**
     * 创建发生消息
     *
     * @param uri
     * @param parameterNames
     * @param parameterValues
     * @return Message
     */
    private Message createMessage(String uri, String[] parameterNames, Object[] parameterValues) {
        //请求参数
        HashMap<String, Object> parameterMap = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            parameterMap.put(parameterNames[i], parameterValues[i]);
        }
        //请求对象
        Request request = new Request();
        request.setVersionInfo(versionHolder.get());
        request.setSessionInfo(JSON.toJSONString(sessionHolder.get()));
        request.setRequestParam(JSON.toJSONString(parameterMap));
        request.setRequestUri(uri);
        return new Message(JSON.toJSONString(request));
    }

    /**
     * 调用属性内部类
     */
    static class InvokeAttribute {
        String serviceName;
        String requestUri;
        boolean isRpc;
        boolean isAsync;
        int timeout;

        public InvokeAttribute(String serviceName, String requestUri, boolean isRpc, boolean isAsync, int timeout) {
            this.serviceName = serviceName;
            this.requestUri = requestUri;
            this.isRpc = isRpc;
            this.isAsync = isAsync;
            this.timeout = timeout;
        }

        public String buildResourceName() {
            StringBuilder resourceName = new StringBuilder();
            if (isRpc) {
                resourceName.append("rpc");
            } else {
                resourceName.append("http");
            }
            resourceName.append("://");
            resourceName.append(serviceName);
            resourceName.append(requestUri);
            return resourceName.toString();
        }
    }

}
