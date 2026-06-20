package com.kms.domain.aggregation.directorytag.entity;

import java.time.LocalDateTime;

/**
 * 维度标签实体
 * 表示目录分类中的维度标签
 */
public class CategoryTag {

    /**
     * 标签ID
     */
    private String tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 维度ID
     */
    private String categoryId;

    /**
     * 维度名称
     */
    private String categoryName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public CategoryTag() {
    }

    public CategoryTag(String tagId, String tagName, String categoryId, String categoryName) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}