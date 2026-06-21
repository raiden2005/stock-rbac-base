package com.stock.rbac.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库列表展示VO
 */
public class KnowledgeListVO {

    private String id;
    private String title;
    private String category;
    private String sourceType;
    private String originalFileUrl;
    private Integer totalSliceNum;
    private Integer hitCount;
    private BigDecimal weight;
    private Integer status;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getOriginalFileUrl() { return originalFileUrl; }
    public void setOriginalFileUrl(String originalFileUrl) { this.originalFileUrl = originalFileUrl; }

    public Integer getTotalSliceNum() { return totalSliceNum; }
    public void setTotalSliceNum(Integer totalSliceNum) { this.totalSliceNum = totalSliceNum; }

    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
