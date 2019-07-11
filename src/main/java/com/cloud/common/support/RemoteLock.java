package com.cloud.common.support;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RemoteLock {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static String createLockValue() {
        return new StringBuilder().append(System.currentTimeMillis()).append("@").append(RandomStringUtils.randomNumeric(6)).toString();
    }

    /**
     * 获取锁
     *
     * @param key
     * @param timeOutMillis
     * @param tryCount
     * @param tryIntervalMillis
     * @return boolean
     */
    public boolean acquire(String key, long timeOutMillis, int tryCount, long tryIntervalMillis) {
        String lockKey = key;
        for (int i = 0; i < tryCount; i++) {
            if (redisTemplate.opsForValue().setIfAbsent(lockKey, createLockValue())) {
                redisTemplate.expire(lockKey, timeOutMillis, TimeUnit.MILLISECONDS);
                return true;
            }
            if (tryCount > 1) {
                try {
                    Thread.sleep(tryIntervalMillis);
                } catch (InterruptedException e) {
                }
            }
        }
        // 防止死锁处理
        String lockValue = (String) redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            long lockTime = Long.parseLong(lockValue.substring(0, lockValue.indexOf("@")));
            if (System.currentTimeMillis() - lockTime > timeOutMillis) {
                //如果并发设置的时候，判断哪个是锁的真正获得者
                String oldValue = (String) redisTemplate.opsForValue().getAndSet(lockKey, createLockValue());
                if (lockValue.equals(oldValue)) {
                    redisTemplate.expire(lockKey, timeOutMillis, TimeUnit.MILLISECONDS);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void release(String key) {
        redisTemplate.delete(key);
    }

}
