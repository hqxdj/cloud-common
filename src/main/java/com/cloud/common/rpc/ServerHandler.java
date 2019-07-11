package com.cloud.common.rpc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.common.bean.ResponseInfo;
import com.cloud.common.bean.SessionInfo;
import com.cloud.common.context.RequestMappingContext;
import com.cloud.common.context.RequestMapping;
import com.cloud.common.context.SessionHolder;
import com.cloud.common.context.VersionHolder;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;

@Slf4j
@Sharable
@Component
public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    @Autowired
    private RequestMappingContext requestMappingContext;

    @Autowired
    private VersionHolder versionHolder;

    @Autowired
    private SessionHolder sessionHolder;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Request request = JSON.parseObject(msg.getBodyData(), Request.class);
        RequestMapping requestMapping = requestMappingContext.getRequestMapping(request.getRequestUri());
        if (requestMapping != null) {
            Message resMsg = new Message();
            resMsg.setSequence(msg.getSequence());

            String resourceName = requestMapping.getMappingUri();
            Entry entry = null;
            try {
                // Sentinel检查
                ContextUtil.enter(resourceName);
                entry = SphU.entry(resourceName, EntryType.IN);
                Object result = invoke(request, requestMapping);
                resMsg.setBodyData(JSON.toJSONString(result));
            } catch (BlockException ex) {
                log.warn("{} blocked", resourceName);

                ResponseInfo responseInfo = new ResponseInfo(ResponseInfo.UNAVAILABLE);
                resMsg.setBodyData(JSON.toJSONString(responseInfo));
            } catch (Throwable ex) {
                log.error("invoke error", ex);

                resMsg.setBodyData(JSON.toJSONString(ResponseInfo.failure()));
                Tracer.trace(ex);
            } finally {
                ctx.channel().writeAndFlush(resMsg);
                // Sentinel清理
                if (entry != null) {
                    entry.exit();
                }
                ContextUtil.exit();
            }
        }
    }

    private Object invoke(Request request, RequestMapping requestMapping) throws Exception {
        versionHolder.set(request.getVersionInfo());
        sessionHolder.set(JSON.parseObject(request.getSessionInfo(), SessionInfo.class));

        //设置方法参数
        int index = 0;
        LinkedHashMap<String, Class> parameterMap = requestMapping.getParameterMap();
        Iterator<String> itor = parameterMap.keySet().iterator();
        Object[] params = new Object[parameterMap.size()];
        JSONObject requestParam = JSON.parseObject(request.getRequestParam());
        while (itor.hasNext()) {
            String name = itor.next();
            Class clazz = parameterMap.get(name);
            params[index] = requestParam.getObject(name, clazz);
            index++;
        }

        //反射调用方法
        Object target = requestMapping.getHandlerTarget();
        Method method = requestMapping.getHandlerMethod().getMethod();
        return method.invoke(target, params);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
