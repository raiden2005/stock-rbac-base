package com.kms.domain.aggregation.directorytag.vo;

import java.time.LocalDateTime;

/**
 * 目录标签视图对象
 * 用于展示目录与标签的关联信息
 */
public class DirectoryTagVO {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 目录ID
     */
    private String directoryId;

    /**
     * 标签ID
     */
    private String tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 是否关注
     */
    private Boolean isFollow;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public DirectoryTagVO() {
    }

    public DirectoryTagVO(String id, String directoryId, String tagId, String tagName, String userId, Boolean isFollow) {
        this.id = id;
        this.directoryId = directoryId;
        this.tagId = tagId;
        this.tagName = tagName;
        this.userId = userId;
        this.isFollow = isFollow;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(Boolean isFollow) {
        this.isFollow = isFollow;
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