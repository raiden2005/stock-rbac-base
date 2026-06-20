package com.stock.rbac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class LocalCacheConfig {

    @Bean
    public LocalCacheService localCacheService() {
        return new LocalCacheService();
    }

    @Bean
    @Primary
    public RedisTemplateFallback redisTemplateFallback() {
        return new RedisTemplateFallback();
    }

    public static class LocalCacheService {
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Long> expireTimes = new ConcurrentHashMap<>();

        public void set(String key, Object value) {
            cache.put(key, value);
        }

        public void setWithExpire(String key, Object value, long timeout, TimeUnit unit) {
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

    public static class RedisTemplateFallback {
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Long> expireTimes = new ConcurrentHashMap<>();

        public Boolean hasKey(String key) {
            checkExpiration(key);
            return cache.containsKey(key);
        }

        public void set(String key, Object value) {
            cache.put(key, value);
        }

        public void set(String key, Object value, long timeout, TimeUnit unit) {
            cache.put(key, value);
            expireTimes.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        }

        public Boolean setIfAbsent(String key, Object value) {
            return cache.putIfAbsent(key, value) == null;
        }

        public Object get(String key) {
            checkExpiration(key);
            return cache.get(key);
        }

        public void delete(String key) {
            cache.remove(key);
            expireTimes.remove(key);
        }

        public Long increment(String key) {
            Object val = cache.get(key);
            long newVal = (val instanceof Long ? (Long) val : 0) + 1;
            cache.put(key, newVal);
            return newVal;
        }

        public java.util.Set<String> keys(String pattern) {
            String regex = pattern.replace("*", ".*");
            java.util.Set<String> result = new java.util.HashSet<>();
            for (String key : cache.keySet()) {
                if (key.matches(regex)) {
                    result.add(key);
                }
            }
            return result;
        }

        private void checkExpiration(String key) {
            Long expireAt = expireTimes.get(key);
            if (expireAt != null && expireAt < System.currentTimeMillis()) {
                cache.remove(key);
                expireTimes.remove(key);
            }
        }

        public RedisOperationsFallback opsForValue() {
            return new RedisOperationsFallback(this);
        }

        public RedisOperationsFallback getOperations() {
            return opsForValue();
        }
    }

    public static class RedisOperationsFallback {
        private final RedisTemplateFallback template;

        public RedisOperationsFallback(RedisTemplateFallback template) {
            this.template = template;
        }

        public Object get(Object key) {
            return template.get((String) key);
        }

        public void set(String key, Object value) {
            template.set(key, value);
        }

        public void set(String key, Object value, long timeout, TimeUnit unit) {
            template.set(key, value, timeout, unit);
        }

        public Boolean setIfAbsent(String key, Object value) {
            return template.setIfAbsent(key, value);
        }

        public Boolean setIfPresent(String key, Object value) {
            if (template.hasKey(key)) {
                template.set(key, value);
                return true;
            }
            return false;
        }

        public Long increment(String key) {
            return template.increment(key);
        }

        public Long increment(String key, long delta) {
            Object val = template.get(key);
            long newVal = (val instanceof Long ? (Long) val : 0) + delta;
            template.set(key, newVal);
            return newVal;
        }

        public Long decrement(String key) {
            return increment(key, -1);
        }

        public Long decrement(String key, long delta) {
            return increment(key, -delta);
        }

        public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
            if (template.hasKey(key)) return false;
            template.set(key, value, timeout, unit);
            return true;
        }

        public Object getAndSet(String key, Object value) {
            Object old = get(key);
            set(key, value);
            return old;
        }

        public Boolean hasKey(String key) {
            return template.hasKey(key);
        }

        public void delete(String key) {
            template.delete(key);
        }

        public RedisTemplateFallback getOperations() {
            return template;
        }
    }
}
