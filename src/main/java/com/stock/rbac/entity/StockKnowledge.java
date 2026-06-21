package com.stock.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库主表 MyBatis-Plus实体
 */
@TableName("stock_knowledge")
public class StockKnowledge {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 知识标题 */
    private String title;

    /** 知识分类 */
    private String category;

    /** 来源类型: text文本 / file文件 */
    private String sourceType;

    /** 原始文件URL */
    private String originalFileUrl;

    /** 切片总数 */
    private Integer totalSliceNum;

    /** 命中次数 */
    private Integer hitCount;

    /** 权重(0.01~10.00) */
    private BigDecimal weight;

    /** 状态: 1上架 0下架 */
    private Integer status;

    /** 创建人 */
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

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

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
