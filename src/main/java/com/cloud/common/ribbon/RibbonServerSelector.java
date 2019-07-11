package com.cloud.common.ribbon;

import com.cloud.common.context.VersionHolder;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RibbonServerSelector extends RoundRobinRule {

    private ThreadLocal<String> serverHolder = new ThreadLocal<>();

    private Map<String, Integer> selectIndexMap = new HashMap<>();

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private VersionHolder versionHolder;

    @Override
    public Server choose(Object key) {
        List<Server> allServers = getLoadBalancer().getAllServers();
        String serviceId = allServers.get(0).getMetaInfo().getAppName();
        if (serviceId.contains("@@")) {
            serviceId = serviceId.split("@@")[1];
        }
        ServiceInstance instance = select(serviceId);
        if (instance != null) {
            for (Server server : allServers) {
                if (server.getHost().equals(instance.getHost()) && server.getPort() == instance.getPort()) {
                    return server;
                }
            }
        }
        return super.choose(key);
    }

    /**
     * 选择服务器实例
     *
     * @param serviceId
     * @return ServiceInstance
     */
    public ServiceInstance select(String serviceId) {
        List<ServiceInstance> matchInstanceList = new ArrayList<>();
        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);

        String version = versionHolder.get();
        if (version != null) {
            for (ServiceInstance instance : instanceList) {
                String instanceVersion = instance.getMetadata().get("version");
                if (StringUtils.isEmpty(instanceVersion) || instanceVersion.equals("undefined") || !instanceVersion.equals(version)) {
                    continue;
                }
                matchInstanceList.add(instance);
            }
        } else {
            matchInstanceList = instanceList;
        }
        if (matchInstanceList.size() > 0) {
            return selectInstance(serviceId, matchInstanceList);
        }
        return null;
    }

    /**
     * 指定选择的服务器
     *
     * @param server
     */
    public void selectServer(String server) {
        serverHolder.set(server);
    }

    /**
     * 轮询选择服务器
     *
     * @param serviceId
     * @param matchInstanceList
     * @return ServiceInstance
     */
    private synchronized ServiceInstance selectInstance(String serviceId, List<ServiceInstance> matchInstanceList) {
        try {
            //获取环境变量设置的服务器，用于调试
            String debugServer = System.getenv(serviceId);
            if (!StringUtils.isEmpty(debugServer)) {
                for (ServiceInstance serviceInstance : matchInstanceList) {
                    if (serviceInstance.getHost().equals(debugServer)) {
                        return serviceInstance;
                    }
                }
                return null;
            }

            //获取外部指定服务器
            String forceServer = serverHolder.get();
            if (!StringUtils.isEmpty(forceServer)) {
                for (ServiceInstance serviceInstance : matchInstanceList) {
                    if (serviceInstance.getHost().equals(forceServer)) {
                        return serviceInstance;
                    }
                }
                return null;
            }

            //轮询选择服务器
            Integer selectIndex = selectIndexMap.get(serviceId);
            if (selectIndex == null || selectIndex == Short.MAX_VALUE) {
                selectIndex = 0;
            }
            selectIndexMap.put(serviceId, ++selectIndex);
            return matchInstanceList.get(selectIndex % matchInstanceList.size());
        } finally {
            serverHolder.remove();
        }
    }

}
