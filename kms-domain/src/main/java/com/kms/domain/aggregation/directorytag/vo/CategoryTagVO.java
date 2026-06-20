package com.kms.domain.aggregation.directorytag.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 维度标签视图对象
 * 用于展示维度及其关联的标签信息
 */
public class CategoryTagVO {

    /**
     * 维度ID
     */
    private String categoryId;

    /**
     * 维度名称
     */
    private String categoryName;

    /**
     * 标签列表
     */
    private List<TagInfo> tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public CategoryTagVO() {
    }

    public CategoryTagVO(String categoryId, String categoryName, List<TagInfo> tags) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tags = tags;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
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

    public List<TagInfo> getTags() {
        return tags;
    }

    public void setTags(List<TagInfo> tags) {
        this.tags = tags;
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

    /**
     * 标签信息内部类
     */
    public static class TagInfo {

        /**
         * 标签ID
         */
        private String tagId;

        /**
         * 标签名称
         */
        private String tagName;

        public TagInfo() {
        }

        public TagInfo(String tagId, String tagName) {
            this.tagId = tagId;
            this.tagName = tagName;
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
    }
}