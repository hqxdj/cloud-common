package com.cloud.common.context;

import lombok.Data;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;

@Data
public class RequestMapping {

    private String mappingUri;
    private Object handlerTarget;
    private HandlerMethod handlerMethod;
    private LinkedHashMap<String, Class> parameterMap;

}
