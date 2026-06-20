package com.kms.infrastructure.adapter.persistence;

import com.kms.domain.model.entity.Directory;
import com.kms.domain.repository.DirectoryRepository;
import com.kms.infrastructure.adapter.persistence.mapper.DirectoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档目录仓储实现类
 * 
 * 实现 kms-domain 模块的 DirectoryRepository 接口
 * 使用 MyBatis 实现数据库操作
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@Repository
public class DirectoryRepositoryImpl implements DirectoryRepository {

    private final DirectoryMapper directoryMapper;

    public DirectoryRepositoryImpl(final DirectoryMapper directoryMapper) {
        this.directoryMapper = directoryMapper;
    }

    /**
     * 创建文档目录
     *
     * @param directory 目录实体
     */
    @Override
    public void createDirectory(final Directory directory) {
        directoryMapper.insert(directory);
    }

    /**
     * 更新文档目录
     *
     * @param directory 目录实体
     */
    @Override
    public void updateDirectory(final Directory directory) {
        directoryMapper.update(directory);
    }

    /**
     * 根据ID删除文档目录
     *
     * @param directoryId 目录ID
     */
    @Override
    public void deleteDirectoryById(final String directoryId) {
        directoryMapper.deleteById(directoryId);
    }

    /**
     * 根据ID列表批量查询文档目录
     *
     * @param directoryIds 目录ID列表
     * @return 目录列表
     */
    @Override
    public List<Directory> queryDocDirectoryByIdList(final List<String> directoryIds) {
        return directoryMapper.selectByIdList(directoryIds);
    }

    /**
     * 根据父级编码查询子目录
     *
     * @param parentCode 父级编码
     * @return 子目录列表
     */
    @Override
    public List<Directory> queryDirectoryChildren(final String parentCode) {
        return directoryMapper.selectChildrenByParentCode(parentCode);
    }

    /**
     * 根据ID查询目录
     *
     * @param directoryId 目录ID
     * @return 目录实体
     */
    @Override
    public Directory findById(final String directoryId) {
        return directoryMapper.selectById(directoryId);
    }

    /**
     * 根据父级ID查询子目录
     *
     * @param parentId 父级ID
     * @return 子目录列表
     */
    @Override
    public List<Directory> findChildren(final String parentId) {
        return directoryMapper.selectChildrenByParentId(parentId);
    }

    /**
     * 批量更新目录排序
     *
     * @param directories 目录列表
     */
    @Override
    public void batchUpdateSort(final List<Directory> directories) {
        directoryMapper.batchUpdateSort(directories);
    }
}