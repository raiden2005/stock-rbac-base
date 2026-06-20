package com.kms.infrastructure.adapter.persistence.mapper;

import com.kms.domain.model.entity.Directory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档目录MyBatis Mapper接口
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@Mapper
public interface DirectoryMapper {

    /**
     * 插入文档目录
     *
     * @param directory 目录实体
     */
    void insert(Directory directory);

    /**
     * 更新文档目录
     *
     * @param directory 目录实体
     */
    void update(Directory directory);

    /**
     * 根据ID删除文档目录
     *
     * @param directoryId 目录ID
     */
    void deleteById(@Param("directoryId") String directoryId);

    /**
     * 根据ID列表批量查询文档目录
     *
     * @param directoryIds 目录ID列表
     * @return 目录列表
     */
    List<Directory> selectByIdList(@Param("directoryIds") List<String> directoryIds);

    /**
     * 根据父级编码查询子目录
     *
     * @param parentCode 父级编码
     * @return 子目录列表
     */
    List<Directory> selectChildrenByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据ID查询目录
     *
     * @param directoryId 目录ID
     * @return 目录实体
     */
    Directory selectById(@Param("directoryId") String directoryId);

    /**
     * 根据父级ID查询子目录
     *
     * @param parentId 父级ID
     * @return 子目录列表
     */
    List<Directory> selectChildrenByParentId(@Param("parentId") String parentId);

    /**
     * 批量更新目录排序
     *
     * @param directories 目录列表
     */
    void batchUpdateSort(@Param("directories") List<Directory> directories);
}