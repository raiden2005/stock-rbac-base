package com.kms.infrastructure.adapter.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 目录标签关联MyBatis Mapper接口
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@Mapper
public interface DirectoryTagMapper {

    /**
     * 插入目录标签关联记录
     *
     * @param directoryId 目录ID
     * @param tagId 标签ID
     */
    void insert(@Param("directoryId") String directoryId, @Param("tagId") String tagId);

    /**
     * 批量插入目录标签关联记录
     *
     * @param directoryId 目录ID
     * @param tagIds 标签ID列表
     */
    void batchInsert(@Param("directoryId") String directoryId, @Param("tagIds") List<String> tagIds);

    /**
     * 根据目录ID删除标签关联记录
     *
     * @param directoryId 目录ID
     */
    void deleteByDirectoryId(@Param("directoryId") String directoryId);

    /**
     * 根据目录ID查询标签ID列表
     *
     * @param directoryId 目录ID
     * @return 标签ID列表
     */
    List<String> selectTagIdsByDirectoryId(@Param("directoryId") String directoryId);

    /**
     * 根据目录ID列表批量查询标签ID
     *
     * @param directoryIds 目录ID列表
     * @return 标签ID列表
     */
    List<String> selectTagIdsByDirectoryIds(@Param("directoryIds") List<String> directoryIds);
}