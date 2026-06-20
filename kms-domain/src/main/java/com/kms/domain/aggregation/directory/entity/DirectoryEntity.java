package com.kms.domain.aggregation.directory.entity;

import com.kms.domain.aggregation.directory.entity.enums.DirectoryStatus;
import java.time.LocalDateTime;

/**
 * 目录基础实体
 * 存储目录的基本信息，不包含业务逻辑
 * 
 * @author kms
 */
public class DirectoryEntity {
    
    /** 目录ID */
    private String id;
    
    /** 目录编码 */
    private String code;
    
    /** 父级目录ID */
    private String parentId;
    
    /** 父级目录编码 */
    private String parentCode;
    
    /** 目录名称 */
    private String name;
    
    /** 目录排序分数 */
    private Integer sortScore;
    
    /** 目录状态 */
    private DirectoryStatus status;
    
    /** 目录层级 */
    private Integer level;
    
    /** 子目录数量 */
    private Long childrenCount;
    
    /** 文档数量 */
    private Long docCount;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 创建人ID */
    private String createUserId;
    
    /** 更新人ID */
    private String updateUserId;
    
    /** 完整路径 */
    private String fullPath;
    
    public DirectoryEntity() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getParentCode() {
        return parentCode;
    }
    
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getSortScore() {
        return sortScore;
    }
    
    public void setSortScore(Integer sortScore) {
        this.sortScore = sortScore;
    }
    
    public DirectoryStatus getStatus() {
        return status;
    }
    
    public void setStatus(DirectoryStatus status) {
        this.status = status;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public Long getChildrenCount() {
        return childrenCount;
    }
    
    public void setChildrenCount(Long childrenCount) {
        this.childrenCount = childrenCount;
    }
    
    public Long getDocCount() {
        return docCount;
    }
    
    public void setDocCount(Long docCount) {
        this.docCount = docCount;
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
    
    public String getCreateUserId() {
        return createUserId;
    }
    
    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }
    
    public String getUpdateUserId() {
        return updateUserId;
    }
    
    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
