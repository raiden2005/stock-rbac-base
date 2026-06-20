package com.kms.domain.aggregation.directorytag.entity;

import java.time.LocalDateTime;

/**
 * 目录标签关联实体
 * 表示目录与标签的关联关系
 */
public class DirectoryTag {

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
     * 用户ID（关注操作的用户）
     */
    private String userId;

    /**
     * 是否关注（true-关注，false-未关注）
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

    public DirectoryTag() {
    }

    public DirectoryTag(String id, String directoryId, String tagId, String userId, Boolean isFollow) {
        this.id = id;
        this.directoryId = directoryId;
        this.tagId = tagId;
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