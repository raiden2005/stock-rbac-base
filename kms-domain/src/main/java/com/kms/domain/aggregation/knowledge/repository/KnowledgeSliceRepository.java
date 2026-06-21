package com.kms.domain.aggregation.knowledge.repository;

import com.kms.domain.aggregation.knowledge.entity.KnowledgeSliceEntity;
import com.kms.domain.common.port.Port;

import java.util.List;

/**
 * 知识切片仓储接口
 * 定义知识切片的持久化操作，属于出站端口
 * 由基础设施层实现
 */
public interface KnowledgeSliceRepository extends Port {

    /**
     * 保存切片实体
     *
     * @param entity 切片实体
     */
    void save(KnowledgeSliceEntity entity);

    /**
     * 批量保存切片实体
     *
     * @param entities 切片实体列表
     */
    void batchSave(List<KnowledgeSliceEntity> entities);

    /**
     * 根据知识ID查询所有切片
     *
     * @param knowledgeId 知识ID
     * @return 切片列表
     */
    List<KnowledgeSliceEntity> findByKnowledgeId(String knowledgeId);

    /**
     * 根据ID查询切片
     *
     * @param id 切片ID
     * @return 切片实体
     */
    KnowledgeSliceEntity findById(String id);

    /**
     * 根据Milvus向量ID查询切片
     *
     * @param milvusVectorId Milvus向量ID
     * @return 切片实体
     */
    KnowledgeSliceEntity findByMilvusVectorId(String milvusVectorId);

    /**
     * 根据知识ID删除所有切片(逻辑删除)
     *
     * @param knowledgeId 知识ID
     */
    void deleteByKnowledgeId(String knowledgeId);

    /**
     * 统计某知识下的切片数
     *
     * @param knowledgeId 知识ID
     * @return 切片数
     */
    long countByKnowledgeId(String knowledgeId);

    /**
     * 增加切片命中次数
     *
     * @param id 切片ID
     */
    void incrementHitCount(String id);

    /**
     * 更新切片的Milvus向量ID
     *
     * @param id             切片ID
     * @param milvusVectorId Milvus向量ID
     */
    void updateMilvusVectorId(String id, String milvusVectorId);
}
