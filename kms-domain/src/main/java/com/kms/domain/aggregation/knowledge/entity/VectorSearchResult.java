package com.kms.domain.aggregation.knowledge.entity;

import java.math.BigDecimal;

/**
 * 向量检索结果
 * 从Milvus检索后组装的结果对象
 */
public class VectorSearchResult {

    /** 知识ID */
    private String knowledgeId;

    /** 知识标题 */
    private String title;

    /** 知识分类 */
    private String category;

    /** 切片文本内容 */
    private String segmentContent;

    /** 相似度分数(0~1) */
    private BigDecimal score;

    /** 知识权重 */
    private BigDecimal weight;

    /** 加权后综合得分 */
    private BigDecimal weightedScore;

    public VectorSearchResult() {
    }

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSegmentContent() {
        return segmentContent;
    }

    public void setSegmentContent(String segmentContent) {
        this.segmentContent = segmentContent;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getWeightedScore() {
        return weightedScore;
    }

    public void setWeightedScore(BigDecimal weightedScore) {
        this.weightedScore = weightedScore;
    }

    /**
     * 计算加权得分: score * weight
     */
    public void calculateWeightedScore() {
        if (this.score != null && this.weight != null) {
            this.weightedScore = this.score.multiply(this.weight);
        } else {
            this.weightedScore = this.score != null ? this.score : BigDecimal.ZERO;
        }
    }
}
