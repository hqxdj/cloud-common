package org.springframework.data.redis.cache;

public class DefaultCacheKeyPrefix implements CacheKeyPrefix {

    @Override
    public String compute(String cacheName) {
        return cacheName;
    }
}
