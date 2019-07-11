package com.cloud.common.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultCacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.DefaultRedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EnableCaching
@Configuration
public class CacheManagerConfig extends CachingConfigurerSupport {

    private static Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
    private static RedisCacheConfiguration cacheConfiguration;
    private static Set<String> cacheNames = new HashSet<>();

    static {
        cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .computePrefixWith(new DefaultCacheKeyPrefix())
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
    }

    public static void setTimeout(String key, int seconds) {
        configMap.put(key, cacheConfiguration.entryTtl(Duration.ofSeconds(seconds)));
        cacheNames.add(key);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        DefaultRedisCache redisCache = new DefaultRedisCache(factory);
        return RedisCacheManager.builder(redisCache).initialCacheNames(cacheNames).withInitialCacheConfigurations(configMap).build();
    }

}
