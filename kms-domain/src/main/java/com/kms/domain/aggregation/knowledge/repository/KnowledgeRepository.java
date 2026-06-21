package com.kms.domain.aggregation.knowledge.repository;

import com.kms.domain.aggregation.knowledge.entity.KnowledgeEntity;
import com.kms.domain.common.port.Port;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 知识库仓储接口
 * 定义知识库的持久化操作，属于出站端口
 * 由基础设施层实现
 */
public interface KnowledgeRepository extends Port {

    /**
     * 保存知识实体
     *
     * @param entity 知识实体
     */
    void save(KnowledgeEntity entity);

    /**
     * 根据ID查询知识实体
     *
     * @param id 知识ID
     * @return 知识实体
     */
    KnowledgeEntity findById(String id);

    /**
     * 分页查询上架知识
     *
     * @param pageable 分页参数
     * @param category 分类(可选)
     * @param keyword  关键词(可选)
     * @return 分页结果
     */
    Page<KnowledgeEntity> findPage(Pageable pageable, String category, String keyword);

    /**
     * 查询所有上架知识
     *
     * @return 上架知识列表
     */
    List<KnowledgeEntity> findAllOnline();

    /**
     * 更新知识实体
     *
     * @param entity 知识实体
     */
    void update(KnowledgeEntity entity);

    /**
     * 删除知识(逻辑删除)
     *
     * @param id 知识ID
     */
    void deleteById(String id);

    /**
     * 统计知识总数
     *
     * @return 总数
     */
    long count();

    /**
     * 统计上架知识总数
     *
     * @return 上架总数
     */
    long countOnline();

    /**
     * 统计总切片数
     *
     * @return 总切片数
     */
    long countTotalSlices();

    /**
     * 统计总命中次数
     *
     * @return 总命中次数
     */
    long countTotalHits();

    /**
     * 增加命中次数
     *
     * @param id 知识ID
     */
    void incrementHitCount(String id);

    /**
     * 批量查询知识(根据ID列表)
     *
     * @param ids 知识ID列表
     * @return 知识实体列表
     */
    List<KnowledgeEntity> findByIds(List<String> ids);
}
