package com.kms.infrastructure.adapter.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.aggregation.directory.aggregation.DirectoryTree;
import com.kms.domain.aggregation.directory.cache.DirectoryTreeCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存适配器 - 实现目录树缓存端口
 * <p>
 * 负责将目录树数据进行Redis缓存管理，内部私有方法包含JSON序列化、反序列化、树拷贝逻辑
 * 仅适配器内部使用，上层业务无感知
 */
@Component
public class DirectoryRedisCacheAdapter implements DirectoryTreeCachePort {

    private static final Logger log = LoggerFactory.getLogger(DirectoryRedisCacheAdapter.class);

    private static final String CACHE_KEY = "kms:directory:tree";
    private static final long CACHE_EXPIRE_HOURS = 24;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DirectoryRedisCacheAdapter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public DirectoryTree getAllDirectoryTreeByCache() {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return deserialize(json);
        } catch (Exception e) {
            log.error("获取目录树缓存失败", e);
            return null;
        }
    }

    @Override
    public void removeDirectoryTreeCache() {
        try {
            redisTemplate.delete(CACHE_KEY);
            log.info("目录树缓存已清除");
        } catch (Exception e) {
            log.error("清除目录树缓存失败", e);
        }
    }

    @Override
    public void cacheDirectoryTree(DirectoryTree tree) {
        if (tree == null) {
            log.warn("目录树为空，跳过缓存");
            return;
        }
        try {
            String json = serialize(tree);
            redisTemplate.opsForValue().set(CACHE_KEY, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("目录树已缓存");
        } catch (Exception e) {
            log.error("缓存目录树失败", e);
        }
    }

    @Override
    public DirectoryTree copyDirectoryTreeCache() {
        DirectoryTree original = getDirectoryTreeCache();
        if (original == null) {
            return null;
        }
        try {
            // 通过序列化实现深度拷贝，防止并发修改
            String json = serialize(original);
            return deserialize(json);
        } catch (Exception e) {
            log.error("深度拷贝目录树失败", e);
            return null;
        }
    }

    @Override
    public DirectoryTree getDirectoryTreeCache() {
        return getAllDirectoryTreeByCache();
    }

    // ==================== 内部私有方法 ====================

    /**
     * 序列化目录树为JSON字符串
     */
    private String serialize(DirectoryTree tree) throws JsonProcessingException {
        return objectMapper.writeValueAsString(tree);
    }

    /**
     * 从JSON字符串反序列化目录树
     */
    private DirectoryTree deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, DirectoryTree.class);
    }
}
