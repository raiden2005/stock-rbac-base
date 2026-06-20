package com.kms.domain.aggregation.directory.cache;

import com.kms.domain.aggregation.directory.aggregation.DirectoryTree;
import com.kms.domain.common.port.Port;

/**
 * 目录树缓存端口接口
 * 定义目录树的缓存操作，属于出站端口
 * 由基础设施层实现（如Redis缓存）
 * 
 * @author kms
 */
public interface DirectoryTreeCachePort extends Port {
    
    /**
     * 获取全量缓存目录树
     *
     * @return 目录树（如果缓存不存在则返回null）
     */
    DirectoryTree getAllDirectoryTreeByCache();
    
    /**
     * 清除缓存
     */
    void removeDirectoryTreeCache();
    
    /**
     * 重建并写入缓存
     *
     * @param tree 目录树
     */
    void cacheDirectoryTree(DirectoryTree tree);
    
    /**
     * 深度拷贝目录树（防止并发修改）
     *
     * @return 目录树副本
     */
    DirectoryTree copyDirectoryTreeCache();
    
    /**
     * 读取缓存数据
     *
     * @return 目录树（如果缓存不存在则返回null）
     */
    DirectoryTree getDirectoryTreeCache();
}
