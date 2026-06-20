package com.kms.domain.aggregation.directory.repository;

import com.kms.domain.aggregation.directory.aggregation.Directory;
import com.kms.domain.aggregation.directory.entity.DirectoryEntity;
import com.kms.domain.common.port.Port;
import java.util.List;

/**
 * 目录仓储端口接口
 * 定义目录的持久化操作，属于出站端口
 * 由基础设施层实现
 * 
 * @author kms
 */
public interface DirectoryRepository extends Port {
    
    /**
     * 新建目录
     *
     * @param directory 目录聚合根
     * @return 创建后的目录聚合根
     */
    Directory createDirectory(Directory directory);
    
    /**
     * 更新目录基础实体
     *
     * @param directory 目录聚合根
     */
    void updateDirectory(Directory directory);
    
    /**
     * 删除目录
     *
     * @param directoryId 目录ID
     */
    void deleteDirectoryById(String directoryId);
    
    /**
     * 根据ID列表批量查询目录基础信息
     *
     * @param directoryIds 目录ID列表
     * @return 目录基础实体列表
     */
    List<DirectoryEntity> queryDocDirectoryByIdList(List<String> directoryIds);
    
    /**
     * 根据编码递归查询子目录
     *
     * @param parentCode 父级目录编码
     * @return 子目录列表
     */
    List<Directory> queryDirectoryChildren(String parentCode);
    
    /**
     * 根据ID查询目录
     *
     * @param directoryId 目录ID
     * @return 目录基础实体
     */
    DirectoryEntity findById(String directoryId);
    
    /**
     * 查询子目录列表
     *
     * @param parentId 父级目录ID
     * @return 子目录基础实体列表
     */
    List<DirectoryEntity> findChildren(String parentId);
    
    /**
     * 批量更新排序
     *
     * @param directories 目录列表
     */
    void batchUpdateSort(List<Directory> directories);
}
