package com.kms.domain.aggregation.directorytag.domain;

import com.kms.domain.aggregation.directorytag.vo.CategoryTagVO;

import java.util.List;
import java.util.Map;

/**
 * 标签领域服务接口
 * 定义目录标签聚合的领域服务行为
 */
public interface IDirectoryTagDomainService {

    /**
     * 组装维度与标签信息
     *
     * @param directoryId 目录ID
     * @return 维度标签视图列表
     */
    List<CategoryTagVO> queryDirectoryCategoryTag(String directoryId);

    /**
     * 批量新增目录标签关联
     *
     * @param directoryId 目录ID
     * @param tagIds 标签ID列表
     */
    void insertCategoryTag(String directoryId, List<String> tagIds);

    /**
     * 删除目录绑定标签
     *
     * @param directoryId 目录ID
     */
    void deleteCategoryTagByDirectoryId(String directoryId);

    /**
     * 查询标签ID-名称映射
     *
     * @param tagIds 标签ID列表
     * @return 标签ID与名称的映射关系
     */
    Map<String, String> queryDirectoryCategoryTagId(List<String> tagIds);

    /**
     * 校验是否已关注
     *
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 是否已关注
     */
    boolean checkIsAlreadyFollow(String directoryId, String userId);
}