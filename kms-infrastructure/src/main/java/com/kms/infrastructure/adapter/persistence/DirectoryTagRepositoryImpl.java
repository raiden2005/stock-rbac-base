package com.kms.infrastructure.adapter.persistence;

import com.kms.infrastructure.adapter.persistence.mapper.CategoryTagMapper;
import com.kms.infrastructure.adapter.persistence.mapper.DirectoryTagMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 目录标签仓储实现类
 * 
 * 实现标签相关的数据库CRUD操作
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@Repository
public class DirectoryTagRepositoryImpl {

    private final DirectoryTagMapper directoryTagMapper;
    private final CategoryTagMapper categoryTagMapper;

    public DirectoryTagRepositoryImpl(final DirectoryTagMapper directoryTagMapper,
                                       final CategoryTagMapper categoryTagMapper) {
        this.directoryTagMapper = directoryTagMapper;
        this.categoryTagMapper = categoryTagMapper;
    }

    /**
     * 插入目录与标签的关联关系
     *
     * @param directoryId 目录ID
     * @param tagIds 标签ID列表
     */
    public void insertCategoryTag(final String directoryId, final List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        directoryTagMapper.batchInsert(directoryId, tagIds);
    }

    /**
     * 根据目录ID删除标签关联关系
     *
     * @param directoryId 目录ID
     */
    public void deleteCategoryTagByDirectoryId(final String directoryId) {
        directoryTagMapper.deleteByDirectoryId(directoryId);
    }

    /**
     * 根据目录ID查询关联的标签ID列表
     *
     * @param directoryId 目录ID
     * @return 标签ID列表
     */
    public List<String> queryCategoryTagByDirectoryId(final String directoryId) {
        return directoryTagMapper.selectTagIdsByDirectoryId(directoryId);
    }

    /**
     * 根据标签ID列表查询标签信息
     *
     * @param tagIds 标签ID列表
     * @return 标签信息列表
     */
    public List<Object> queryTagByIds(final List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return categoryTagMapper.selectByIds(tagIds);
    }
}