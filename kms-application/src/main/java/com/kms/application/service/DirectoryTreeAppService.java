package com.kms.application.service;

import com.kms.application.dto.DirectoryTreeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 树形查询专用编排服务
 * 负责目录树的查询编排
 */
@Service
public class DirectoryTreeAppService {

    /**
     * 优先读取缓存获取完整目录树
     * @return 完整目录树
     */
    public List<DirectoryTreeDTO> getAllDirectoryTreeByCache() {
        // 1. 尝试从缓存获取
        // 2. 缓存未命中则调用领域服务获取
        // 3. 存入缓存并返回
        return List.of();
    }

    /**
     * 组合调用权限领域服务与目录树服务获取可见目录树
     * @param userId 用户ID
     * @return 用户可见的目录树
     */
    public List<DirectoryTreeDTO> getVisibleDirectoryTree(String userId) {
        // 1. 调用权限领域服务获取用户可见的目录ID列表
        // 2. 调用目录树领域服务获取目录树
        // 3. 过滤出用户可见的目录
        return List.of();
    }
}
