package com.kms.infrastructure.adapter.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类标签MyBatis Mapper接口
 * 
 * @author kms
 * @version 1.0
 * @since Java 21
 */
@Mapper
public interface CategoryTagMapper {

    /**
     * 根据标签ID列表查询标签信息
     *
     * @param tagIds 标签ID列表
     * @return 标签信息列表
     */
    List<Object> selectByIds(@Param("tagIds") List<String> tagIds);

    /**
     * 根据标签ID查询标签信息
     *
     * @param tagId 标签ID
     * @return 标签信息
     */
    Object selectById(@Param("tagId") String tagId);

    /**
     * 根据分类ID查询标签列表
     *
     * @param categoryId 分类ID
     * @return 标签信息列表
     */
    List<Object> selectByCategoryId(@Param("categoryId") String categoryId);
}