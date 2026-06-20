package com.kms.domain.aggregation.directory.domain;

import com.kms.domain.aggregation.directory.aggregation.Directory;
import com.kms.domain.common.port.Port;
import java.util.List;
import java.util.Map;

/**
 * 目录领域服务接口
 * 定义目录的领域业务逻辑，属于应用层调用的领域服务
 * 
 * @author kms
 */
public interface IDirectoryDomainService extends Port {
    
    /**
     * 计算排序分数
     * 根据同级目录数量和目标位置计算排序分数
     *
     * @param siblingCount 同级目录数量
     * @param targetIndex  目标索引位置
     * @return 排序分数
     */
    Integer calculateSortScore(Integer siblingCount, Integer targetIndex);
    
    /**
     * 递归累加父级文档与子目录数量
     * 统计每个目录及其子目录的文档数量
     *
     * @param directoryIds 目录ID列表
     * @return 目录ID到数量的映射
     */
    Map<String, Long> accumulateParentCount(List<String> directoryIds);
    
    /**
     * 获取单条目录完整路径
     *
     * @param directoryId 目录ID
     * @return 完整路径字符串
     */
    String getFullPath(String directoryId);
    
    /**
     * 批量获取路径映射
     *
     * @param directoryIds 目录ID列表
     * @return 目录ID到完整路径的映射
     */
    Map<String, String> getFullPath(List<String> directoryIds);
    
    /**
     * 填充子目录文档数量
     * 为目录列表填充子目录数量和文档数量
     *
     * @param directories 目录列表
     */
    void fillChildrenDirectoryInfo(List<Directory> directories);
    
    /**
     * 填充排序参考目录信息
     * 填充同级最后一个目录的信息用于排序计算
     *
     * @param directory 目录
     */
    void fillLastDirInfo(Directory directory);
}
