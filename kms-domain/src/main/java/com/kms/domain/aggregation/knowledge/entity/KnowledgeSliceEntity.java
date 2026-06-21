package com.kms.domain.aggregation.knowledge.entity;

import java.time.LocalDateTime;

/**
 * 知识切片实体
 * 知识库按字数切片后的每一段文本
 */
public class KnowledgeSliceEntity {

    /** 切片ID */
    private String id;

    /** 关联知识ID */
    private String knowledgeId;

    /** 切片文本内容 */
    private String segmentContent;

    /** Milvus向量ID(已入库时填写) */
    private String milvusVectorId;

    /** 命中次数 */
    private Integer hitCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    public KnowledgeSliceEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getSegmentContent() {
        return segmentContent;
    }

    public void setSegmentContent(String segmentContent) {
        this.segmentContent = segmentContent;
    }

    public String getMilvusVectorId() {
        return milvusVectorId;
    }

    public void setMilvusVectorId(String milvusVectorId) {
        this.milvusVectorId = milvusVectorId;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
