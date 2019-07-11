package com.cloud.common.context;

import com.cloud.common.bean.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class RequestMappingContext {

    private Map<String, RequestMapping> requestMappings = new HashMap<>();
    private boolean loaded;

    public RequestMapping getRequestMapping(String uri) {
        return requestMappings.get(uri);
    }

    public void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        try {
            Map<String, RequestMapping> mappings = new HashMap<>();
            Map<String, HandlerMapping> handlerMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(AppContext.getApplicationContext(), HandlerMapping.class, true, false);
            for (HandlerMapping handlerMapping : handlerMappings.values()) {
                if (handlerMapping instanceof RequestMappingHandlerMapping) {
                    RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;
                    Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
                    for (Map.Entry<RequestMappingInfo, HandlerMethod> handlerMethodEntry : handlerMethods.entrySet()) {
                        HandlerMethod handlerMethod = handlerMethodEntry.getValue();
                        Method method = handlerMethod.getMethod();
                        if (method.getReturnType() != ResponseInfo.class) {
                            continue;
                        }

                        RequestMappingInfo mappingInfo = handlerMethodEntry.getKey();
                        PatternsRequestCondition patternsCondition = mappingInfo.getPatternsCondition();
                        String mappingUri = patternsCondition.getPatterns().iterator().next();

                        LinkedHashMap<String, Class> parameterMap = new LinkedHashMap();
                        Class[] parameterTypes = method.getParameterTypes();
                        Parameter[] parameters = method.getParameters();
                        for (int i = 0; i < parameters.length; i++) {
                            String parameterName = parameters[i].getName();
                            Class parameterType = parameterTypes[i];
                            parameterMap.put(parameterName, parameterType);
                        }
                        RequestMapping requestMapping = new RequestMapping();
                        requestMapping.setMappingUri(mappingUri);
                        requestMapping.setHandlerTarget(AppContext.getBean(handlerMethod.getBeanType()));
                        requestMapping.setHandlerMethod(handlerMethod);
                        requestMapping.setParameterMap(parameterMap);

                        mappings.put(mappingUri, requestMapping);
                    }
                }
            }
            requestMappings = mappings;
        } catch (Exception e) {
            log.error("load HandlerMapping error", e);
        }
    }
}
