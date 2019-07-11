package org.springframework.data.redis.cache;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Slf4j
public class DefaultRedisCache extends DefaultRedisCacheWriter {

    private static final String RESOURCE_NAME_PREFIX = "DefaultRedisCache/";

    public DefaultRedisCache(RedisConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        String resourceName = RESOURCE_NAME_PREFIX + "put";
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);

            super.put(name, key, value, ttl);
        } catch (Throwable ex) {
            handleThrowable(ex, resourceName);
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        String resourceName = RESOURCE_NAME_PREFIX + "putIfAbsent";
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);

            return super.putIfAbsent(name, key, value, ttl);
        } catch (Throwable ex) {
            handleThrowable(ex, resourceName);
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
        return null;
    }

    @Override
    public byte[] get(String name, byte[] key) {
        String resourceName = RESOURCE_NAME_PREFIX + "get";
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);

            return super.get(name, key);
        } catch (Throwable ex) {
            handleThrowable(ex, resourceName);
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
        return null;
    }

    @Override
    public void remove(String name, byte[] key) {
        String resourceName = RESOURCE_NAME_PREFIX + "remove";
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);

            super.remove(name, key);
        } catch (Throwable ex) {
            handleThrowable(ex, resourceName);
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public void clean(String name, byte[] pattern) {
        String resourceName = RESOURCE_NAME_PREFIX + "clean";
        Entry entry = null;
        try {
            // Sentinel检查
            entry = SphU.entry(resourceName, EntryType.OUT);

            super.clean(name, pattern);
        } catch (Throwable ex) {
            handleThrowable(ex, resourceName);
        } finally {
            // Sentinel清理
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void handleThrowable(Throwable ex, String resourceName) {
        if (BlockException.isBlockException(ex)) {
            log.warn("{} execute blocked", resourceName);
        } else {
            log.error("execute error", ex);
            Tracer.trace(ex);
        }
        throw new RuntimeException(ex);
    }
}
