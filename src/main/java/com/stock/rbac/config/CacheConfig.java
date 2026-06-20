package com.stock.rbac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public SimpleCacheService simpleCacheService() {
        return new SimpleCacheService();
    }

    public static class SimpleCacheService {
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Long> expireTimes = new ConcurrentHashMap<>();

        public void set(String key, Object value) {
            cache.put(key, value);
        }

        public void set(String key, Object value, long timeout, TimeUnit unit) {
            cache.put(key, value);
            expireTimes.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        }

        public Object get(String key) {
            checkExpiration(key);
            return cache.get(key);
        }

        public Boolean hasKey(String key) {
            checkExpiration(key);
            return cache.containsKey(key);
        }

        public void delete(String key) {
            cache.remove(key);
            expireTimes.remove(key);
        }

        public void deleteAll(String pattern) {
            cache.keySet().removeIf(key -> key.startsWith(pattern));
            expireTimes.keySet().removeIf(key -> key.startsWith(pattern));
        }

        public Long increment(String key) {
            Object val = cache.get(key);
            long newVal = (val instanceof Long ? (Long) val : 0) + 1;
            cache.put(key, newVal);
            return newVal;
        }

        private void checkExpiration(String key) {
            Long expireAt = expireTimes.get(key);
            if (expireAt != null && expireAt < System.currentTimeMillis()) {
                cache.remove(key);
                expireTimes.remove(key);
            }
        }
    }
}
