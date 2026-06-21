package com.stock.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

/**
 * 知识切片表 MyBatis-Plus实体
 */
@TableName("stock_knowledge_slice")
public class StockKnowledgeSlice {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 关联知识ID */
    private String knowledgeId;

    /** 切片文本内容 */
    private String segmentContent;

    /** Milvus向量ID */
    private String milvusVectorId;

    /** 命中次数 */
    private Integer hitCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(String knowledgeId) { this.knowledgeId = knowledgeId; }

    public String getSegmentContent() { return segmentContent; }
    public void setSegmentContent(String segmentContent) { this.segmentContent = segmentContent; }

    public String getMilvusVectorId() { return milvusVectorId; }
    public void setMilvusVectorId(String milvusVectorId) { this.milvusVectorId = milvusVectorId; }

    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
